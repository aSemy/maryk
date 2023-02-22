package maryk.datastore.memory.processors

import maryk.core.clock.HLC
import maryk.core.models.IsRootValuesDataModel
import maryk.core.processors.datastore.scanRange.KeyScanRanges
import maryk.core.processors.datastore.scanRange.createScanRange
import maryk.core.properties.IsValuesPropertyDefinitions
import maryk.core.properties.types.Key
import maryk.core.query.orders.Direction.ASC
import maryk.core.query.orders.Direction.DESC
import maryk.core.query.requests.IsScanRequest
import maryk.datastore.memory.records.DataRecord
import maryk.datastore.memory.records.DataStore
import maryk.datastore.shared.ScanType.IndexScan
import maryk.lib.extensions.compare.compareTo
import kotlin.math.min

internal fun <DM : IsRootValuesDataModel<P>, P : IsValuesPropertyDefinitions> scanIndex(
    dataStore: DataStore<DM, P>,
    scanRequest: IsScanRequest<DM, P, *>,
    recordFetcher: (IsRootValuesDataModel<*>, Key<*>) -> DataRecord<*, *>?,
    indexScan: IndexScan,
    keyScanRange: KeyScanRanges,
    processStoreValue: (DataRecord<DM, P>, ByteArray?) -> Unit
) {
    val indexReference = indexScan.index.referenceStorageByteArray.bytes
    val index = dataStore.getOrCreateIndex(indexReference)

    val startKey = scanRequest.startKey?.let { startKey ->
        recordFetcher(scanRequest.dataModel, scanRequest.startKey as Key<*>)?.let { startRecord ->
            indexScan.index.toStorageByteArrayForIndex(startRecord, startKey.bytes)
        }
    }

    val indexScanRange = indexScan.index.createScanRange(scanRequest.where, keyScanRange)

    val toVersion = scanRequest.toVersion?.let { HLC(it) }

    when (indexScan.direction) {
        ASC -> {
            for (indexRange in indexScanRange.ranges) {
                val indexStartKey = indexRange.getAscendingStartKey(startKey, keyScanRange.includeStart)

                val startIndex = indexStartKey.let { startRange ->
                    if (!indexRange.startInclusive && indexRange.start === startRange) {
                        // If start range was not highered it was not possible so scan to lastIndex
                        index.indexValues.lastIndex
                    } else {
                        index.indexValues.binarySearch { it.value compareTo indexStartKey }.let { valueIndex ->
                            when {
                                valueIndex < 0 -> valueIndex * -1 - 1 // If negative start at first entry point
                                else -> valueIndex
                            }
                        }
                    }
                }

                var currentSize: UInt = 0u

                for (i in startIndex until index.indexValues.size) {
                    val indexRecord = index.indexValues[i]
                    val dataRecord = indexRecord.record ?: continue

                    if (indexRange.keyOutOfRange(indexRecord.value)) {
                        break
                    }

                    if (!indexScanRange.matchesPartials(indexRecord.value)) {
                        continue
                    }

                    if (scanRequest.shouldBeFiltered(dataRecord, toVersion, recordFetcher)) {
                        continue
                    }

                    processStoreValue(dataRecord, indexRecord.value)

                    // Break when limit is found
                    if (++currentSize == scanRequest.limit) break
                }
            }
        }
        DESC -> {
            for (indexRange in indexScanRange.ranges.reversed()) {
                val lastKey = indexRange.getDescendingStartKey(startKey, scanRequest.includeStart)?.let {
                    if (indexRange.endInclusive && indexRange.end === it) byteArrayOf() else it
                }

                val startIndex = lastKey?.let { endRange ->
                    if (endRange.isEmpty()) {
                        index.indexValues.lastIndex
                    } else {
                        index.indexValues.binarySearch { it.value compareTo endRange }.let { valueIndex ->
                            when {
                                valueIndex < 0 -> valueIndex * -1 - 2 // If negative start at before first entry point because it should be before match
                                else -> valueIndex
                            }
                        }
                    }
                } ?: index.indexValues.lastIndex

                var currentSize: UInt = 0u

                for (i in min(startIndex, index.indexValues.lastIndex) downTo 0) {
                    val indexRecord = index.indexValues[i]
                    val dataRecord = indexRecord.record ?: continue

                    if (indexRange.keyBeforeStart(indexRecord.value)) {
                        break
                    }

                    if (!indexScanRange.matchesPartials(indexRecord.value)) {
                        continue
                    }

                    if (scanRequest.shouldBeFiltered(dataRecord, toVersion, recordFetcher)) {
                        continue
                    }

                    processStoreValue(dataRecord, indexRecord.value)

                    // Break when limit is found
                    if (++currentSize == scanRequest.limit) break
                }
            }
        }
    }
}
