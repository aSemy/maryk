package maryk.datastore.rocksdb

import maryk.core.clock.HLC
import maryk.core.properties.definitions.index.IsIndexable
import maryk.datastore.rocksdb.processors.HistoricStoreIndexValuesWalker
import maryk.datastore.rocksdb.processors.StoreValuesGetter
import maryk.datastore.rocksdb.processors.TRUE_ARRAY
import maryk.datastore.rocksdb.processors.helpers.setIndexValue
import maryk.rocksdb.use

/**
 * Walks all existing data records for [columnFamilies] of model in [dataStore]
 * Will index any [indicesToIndex] with relevant values
 */
internal fun walkDataRecordsAndFillIndex(
    dataStore: RocksDBDataStore,
    columnFamilies: TableColumnFamilies,
    indicesToIndex: List<IsIndexable>
) {
    Transaction(dataStore).use { transaction ->
        transaction.getIterator(dataStore.defaultReadOptions, columnFamilies.keys).use { iterator ->
            val storeGetter = StoreValuesGetter(null, dataStore.db, columnFamilies, dataStore.defaultReadOptions)
            val historicStoreIndexValuesWalker = if (columnFamilies is HistoricTableColumnFamilies) {
                HistoricStoreIndexValuesWalker(
                    columnFamilies,
                    dataStore.defaultReadOptions
                )
            } else null

            while (iterator.isValid()) {
                iterator.next()
                val key = iterator.key()

                storeGetter.moveToKey(key)

                for (index in indicesToIndex) {
                    storeGetter.lastVersion = null
                    // Store non historic value
                    index.toStorageByteArrayForIndex(storeGetter, key)?.let { indexValue ->
                        setIndexValue(
                            transaction,
                            columnFamilies,
                            index.referenceStorageByteArray.bytes,
                            indexValue,
                            HLC.toStorageBytes(HLC(storeGetter.lastVersion!!))
                        )
                    }

                    // Process historical values for historical index
                    if (columnFamilies is HistoricTableColumnFamilies) {
                        historicStoreIndexValuesWalker?.walkHistoricalValuesForIndexKeys(
                            key,
                            transaction,
                            index
                        ) { historicReference ->
                            transaction.put(
                                columnFamilies.historic.index,
                                historicReference,
                                TRUE_ARRAY
                            )
                        }
                    }
                }
            }
        }
    }
}
