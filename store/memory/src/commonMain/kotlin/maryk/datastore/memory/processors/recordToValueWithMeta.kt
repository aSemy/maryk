@file:Suppress("EXPERIMENTAL_API_USAGE")

package maryk.datastore.memory.processors

import maryk.core.models.IsRootValuesDataModel
import maryk.core.processors.datastore.convertStorageToValues
import maryk.core.properties.PropertyDefinitions
import maryk.core.query.ValuesWithMetaData
import maryk.datastore.memory.records.DataRecord
import maryk.datastore.memory.records.DataRecordHistoricValues
import maryk.datastore.memory.records.DataRecordValue
import maryk.datastore.memory.records.DeletedValue
import maryk.datastore.memory.records.DataRecordNode

/**
 * Processes [record] values to a ValuesWithMeta object
 */
internal fun <DM : IsRootValuesDataModel<P>, P : PropertyDefinitions> DM.recordToValueWithMeta(
    record: DataRecord<DM, P>
): ValuesWithMetaData<DM, P> {
    var valueIndex = -1
    var maxVersion = record.firstVersion

    val values = this.convertStorageToValues(
        getQualifier = {
            valueIndex++

            // skip deleted values
            while (valueIndex < record.values.size  && isDeletedNode(record.values[valueIndex])) {
                valueIndex++
            }

            if (valueIndex < record.values.size) {
                record.values[valueIndex].reference
            } else null
        },
        processValue = { _, _ ->
            val node = record.values[valueIndex]
            when (node) {
                is DataRecordValue<*> -> {
                    if (node.version > maxVersion) {
                        maxVersion = node.version
                    }
                    node.value
                }
                is DataRecordHistoricValues<*> -> {
                    when (val latest = node.history.last()) {
                        is DataRecordValue<*> -> {
                            if (latest.version > maxVersion) {
                                maxVersion = latest.version
                            }
                            latest.value
                        }
                        is DeletedValue<*> -> {} // skip deleted
                        else -> throw Exception("Unknown value type")
                    }
                }
                is DeletedValue<*> -> {} // Skip deleted
            }
        }
    )

    return ValuesWithMetaData(
        key = record.key,
        values = values,
        isDeleted = false,
        firstVersion = record.firstVersion,
        lastVersion = maxVersion
    )
}

/** Check if [node] is deleted */
private fun isDeletedNode(node: DataRecordNode) =
    node is DeletedValue<*> || (node is DataRecordHistoricValues<*> && node.history.last() is DeletedValue<*>)
