package maryk.datastore.rocksdb

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import maryk.core.exceptions.DefNotFoundException
import maryk.core.extensions.bytes.calculateVarIntWithExtraInfoByteSize
import maryk.core.extensions.bytes.writeVarIntWithExtraInfo
import maryk.core.models.IsRootValuesDataModel
import maryk.core.models.RootDataModel
import maryk.core.properties.PropertyDefinitions
import maryk.datastore.rocksdb.TableType.Index
import maryk.datastore.rocksdb.TableType.Table
import maryk.datastore.rocksdb.TableType.Unique
import maryk.datastore.shared.AbstractDataStore
import maryk.datastore.shared.StoreAction
import maryk.datastore.shared.StoreActor
import maryk.rocksdb.ColumnFamilyDescriptor
import maryk.rocksdb.ColumnFamilyHandle
import maryk.rocksdb.Options
import maryk.rocksdb.TransactionDB
import maryk.rocksdb.TransactionDBOptions
import maryk.rocksdb.openTransactionDB

internal typealias StoreExecutor = Unit.(StoreAction<*, *, *, *>, RocksDBDataStore) -> Unit
internal typealias StoreActor = SendChannel<StoreAction<*, *, *, *>>

internal expect fun CoroutineScope.storeActor(
    store: RocksDBDataStore,
    executor: StoreExecutor
): StoreActor<*, *>

class RocksDBDataStore(
    val keepAllVersions: Boolean = true,
    private val rocksDBOptions: Options = Options(),
    relativePath: String,
    dataModelsById: Map<UInt, RootDataModel<*, *>>
) : AbstractDataStore(dataModelsById) {
    override val coroutineContext = GlobalScope.coroutineContext

    private val columnFamilyHandlesByDataModelIndex = mutableMapOf<UInt, TableColumnFamilies>()

    private val transactionDBOptions = TransactionDBOptions()

    internal val db: TransactionDB = openTransactionDB(rocksDBOptions, transactionDBOptions, relativePath)

    private val storeActor = this.storeActor(this, storeExecutor)

    init {
        for (index in dataModelsById.keys) {
            columnFamilyHandlesByDataModelIndex[index] = createColumnFamilyHandles(index)
        }
    }

    private fun createColumnFamilyHandles(tableIndex: UInt) : TableColumnFamilies {
        val columnFamilyNameSize = tableIndex.calculateVarIntWithExtraInfoByteSize()

        var index = 0
        val tableTypes = if (keepAllVersions) TableType.values() else arrayOf(Table, Index, Unique)

        val handles = mutableListOf<ColumnFamilyHandle>()

        for (tableType in tableTypes) {
            val name = ByteArray(columnFamilyNameSize)
            tableIndex.writeVarIntWithExtraInfo(tableType.byte) { name[index++] = it }
            index = 0
            handles += db.createColumnFamily(ColumnFamilyDescriptor(name))
        }

        return if (keepAllVersions) {
            HistoricTableColumnFamilies(
                handles[0],
                handles[1],
                handles[2],
                TableColumnFamilies(handles[3], handles[4], handles[5])
            )
        } else {
            TableColumnFamilies(handles[0], handles[1], handles[2])
        }
    }

    override fun <DM : IsRootValuesDataModel<P>, P : PropertyDefinitions> getStoreActor(dataModel: DM) =
        this.storeActor

    override fun close() {
        db.close()
        transactionDBOptions.close()
        rocksDBOptions.close()

        columnFamilyHandlesByDataModelIndex.values.forEach {
            it.close()
        }
    }

    fun getColumnFamilies(dbIndex: UInt) =
        columnFamilyHandlesByDataModelIndex[dbIndex]
            ?: throw DefNotFoundException("DataModel definition not found for $dbIndex")
}
