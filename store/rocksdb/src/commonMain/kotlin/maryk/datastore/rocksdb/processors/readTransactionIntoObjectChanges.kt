package maryk.datastore.rocksdb.processors

import maryk.core.exceptions.RequestException
import maryk.core.extensions.bytes.initIntByVar
import maryk.core.extensions.bytes.initULong
import maryk.core.models.IsRootValuesDataModel
import maryk.core.processors.datastore.StorageTypeEnum.Embed
import maryk.core.processors.datastore.StorageTypeEnum.ListSize
import maryk.core.processors.datastore.StorageTypeEnum.MapSize
import maryk.core.processors.datastore.StorageTypeEnum.ObjectDelete
import maryk.core.processors.datastore.StorageTypeEnum.SetSize
import maryk.core.processors.datastore.StorageTypeEnum.TypeValue
import maryk.core.processors.datastore.StorageTypeEnum.Value
import maryk.core.processors.datastore.readStorageToChanges
import maryk.core.properties.PropertyDefinitions
import maryk.core.properties.definitions.IsSimpleValueDefinition
import maryk.core.properties.graph.RootPropRefGraph
import maryk.core.properties.types.Key
import maryk.core.properties.types.TypedValue
import maryk.core.query.changes.DataObjectVersionedChange
import maryk.core.query.changes.VersionedChanges
import maryk.datastore.rocksdb.HistoricTableColumnFamilies
import maryk.datastore.rocksdb.TableColumnFamilies
import maryk.datastore.rocksdb.processors.helpers.checkExistence
import maryk.datastore.rocksdb.processors.helpers.historicQualifierRetriever
import maryk.datastore.rocksdb.processors.helpers.nonHistoricQualifierRetriever
import maryk.rocksdb.ReadOptions
import maryk.rocksdb.Transaction

/** Processes values for [key] from transaction to a DataObjectWithChanges object */
internal fun <DM : IsRootValuesDataModel<P>, P : PropertyDefinitions> DM.readTransactionIntoObjectChanges(
    transaction: Transaction,
    readOptions: ReadOptions,
    creationVersion: ULong,
    columnFamilies: TableColumnFamilies,
    key: Key<DM>,
    select: RootPropRefGraph<P>?,
    fromVersion: ULong,
    toVersion: ULong?
): DataObjectVersionedChange<DM>? {
    val changes: List<VersionedChanges>

    if (toVersion == null) {
        val iterator = transaction.getIterator(readOptions, columnFamilies.table)

        checkExistence(iterator, key)

        // Will start by going to next key so will miss the creation timestamp
        val getQualifier = iterator.nonHistoricQualifierRetriever(key)

        var index: Int
        changes = this.readStorageToChanges(
            getQualifier = getQualifier,
            select = select,
            processValue = { storageType, definition, valueWithVersionReader ->
                val currentVersion: ULong
                val value = when (storageType) {
                    ObjectDelete -> {
                        val valueBytes = iterator.value()
                        val value = if (iterator.key()[key.size] == 0.toByte()) {
                            valueBytes.last() == TRUE
                        } else null
                        index = 0
                        currentVersion = initULong({ valueBytes[index++] })
                        value
                    }
                    Value -> {
                        val valueBytes = iterator.value()
                        index = 0
                        currentVersion = initULong({ valueBytes[index++] })
                        Value.castDefinition(definition).readStorageBytes(valueBytes.size - index) {
                            valueBytes[index++]
                        }
                    }
                    ListSize -> {
                        val valueBytes = iterator.value()
                        index = 0
                        currentVersion = initULong({ valueBytes[index++] })
                        initIntByVar { valueBytes[index++] }
                    }
                    SetSize -> {
                        val valueBytes = iterator.value()
                        index = 0
                        currentVersion = initULong({ valueBytes[index++] })
                        initIntByVar { valueBytes[index++] }
                    }
                    MapSize -> {
                        val valueBytes = iterator.value()
                        index = 0
                        currentVersion = initULong({ valueBytes[index++] })
                        initIntByVar { valueBytes[index++] }
                    }
                    TypeValue -> {
                        val valueBytes = iterator.value()
                        index = 0
                        val reader = { valueBytes[index++] }

                        currentVersion = initULong(reader)

                        val typeDefinition = TypeValue.castDefinition(definition)

                        val indicatorByte = reader()
                        val type = typeDefinition.typeEnum.readStorageBytes(typeDefinition.typeEnum.byteSize, reader)

                        if (indicatorByte == COMPLEX_TYPE_INDICATOR) {
                            TypedValue(type, Unit)
                        } else {
                            val valueDefinition = typeDefinition.definition(type) as IsSimpleValueDefinition<*, *>
                            valueDefinition.readStorageBytes(valueBytes.size - index, reader)
                        }
                    }
                    Embed -> {
                        val valueBytes = iterator.value()
                        index = 0
                        val reader = { valueBytes[index++] }

                        currentVersion = initULong(reader)
                    }
                }
                if (currentVersion >= fromVersion) {
                    valueWithVersionReader(currentVersion, value)
                }
            }
        )
    } else {
        if (columnFamilies !is HistoricTableColumnFamilies) {
            throw RequestException("No historic table present so cannot use `toVersion` on get changes")
        }

        val iterator = transaction.getIterator(readOptions, columnFamilies.historic.table)

        checkExistence(iterator, key)

        var currentVersion: ULong = creationVersion

        // Will start by going to next key so will miss the creation timestamp
        val getQualifier = iterator.historicQualifierRetriever(key, toVersion) { version ->
            currentVersion = version
        }

        var index: Int
        changes = this.readStorageToChanges(
            getQualifier = getQualifier,
            select = select,
            processValue = { storageType, definition, valueWithVersionReader ->
                val value = when (storageType) {
                    ObjectDelete -> {
                        val value = if (iterator.key().last() == 0.toByte()) {
                            val value = iterator.value()
                            value[0] == TRUE
                        } else null
                        value
                    }
                    Value -> {
                        val valueBytes = iterator.value()
                        index = 0
                        Value.castDefinition(definition).readStorageBytes(valueBytes.size) {
                            valueBytes[index++]
                        }
                    }
                    ListSize -> {
                        val valueBytes = iterator.value()
                        index = 0
                        initIntByVar { valueBytes[index++] }
                    }
                    SetSize -> {
                        val valueBytes = iterator.value()
                        index = 0
                        initIntByVar { valueBytes[index++] }
                    }
                    MapSize -> {
                        val valueBytes = iterator.value()
                        index = 0
                        initIntByVar { valueBytes[index++] }
                    }
                    TypeValue -> {
                        val valueBytes = iterator.value()
                        index = 0
                        val reader = { valueBytes[index++] }

                        val typeDefinition = TypeValue.castDefinition(definition)

                        val indicatorByte = reader()
                        val type = typeDefinition.typeEnum.readStorageBytes(typeDefinition.typeEnum.byteSize, reader)

                        if (indicatorByte == COMPLEX_TYPE_INDICATOR) {
                            TypedValue(type, Unit)
                        } else {
                            val valueDefinition = typeDefinition.definition(type) as IsSimpleValueDefinition<*, *>
                            valueDefinition.readStorageBytes(valueBytes.size - index, reader)
                        }
                    }
                    Embed -> { Unit }
                }
                if (currentVersion >= fromVersion) {
                    valueWithVersionReader(currentVersion, value)
                }
            }
        )
    }

    if (changes.isEmpty()) {
        // Return null if no ValueItems were found
        return null
    }
    return DataObjectVersionedChange(
        key = key,
        changes = changes
    )
}
