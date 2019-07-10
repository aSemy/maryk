package maryk.datastore.rocksdb.processors.helpers

import maryk.core.clock.HLC
import maryk.core.exceptions.RequestException
import maryk.core.models.IsDataModelWithValues
import maryk.core.properties.definitions.IsStorageBytesEncodable
import maryk.core.properties.references.EmbeddedValuesPropertyRef
import maryk.core.properties.references.ListItemReference
import maryk.core.properties.references.ListReference
import maryk.core.properties.references.MapReference
import maryk.core.properties.references.MapValueReference
import maryk.core.properties.references.SetItemReference
import maryk.core.properties.references.SetReference
import maryk.core.properties.references.TypedPropertyReference
import maryk.core.properties.references.TypedValueReference
import maryk.core.values.EmptyValueItems
import maryk.datastore.rocksdb.TableColumnFamilies
import maryk.rocksdb.ReadOptions
import maryk.rocksdb.Transaction
import maryk.rocksdb.WriteOptions

@Suppress("UNUSED_PARAMETER")
fun <T : Any> deleteByReference(
    transaction: Transaction,
    columnFamilies: TableColumnFamilies,
    readOptions: ReadOptions,
    writeOptions: WriteOptions,
    reference: TypedPropertyReference<T>,
    version: HLC,
    handlePreviousValue: ((ByteArray, T?) -> Unit)?
): Boolean {
    if (reference is TypedValueReference<*, *, *>) {
        throw RequestException("Type Reference not allowed for deletes. Use the multi type parent.")
    }

    val referenceToCompareTo = reference.toStorageByteArray()
//    var referenceOfParent: ByteArray? = null
//    var toShiftListCount = 0u

    var shouldHandlePrevValue = true

    val value = transaction.getValue(columnFamilies, readOptions, null, referenceToCompareTo) { b, o, l ->
        @Suppress("UNCHECKED_CAST")
        (reference as IsStorageBytesEncodable<T>).fromStorageBytes(b, o, l)
    }

    // Get previous value and convert if of complex type
    @Suppress("UNCHECKED_CAST")
    val prevValue: T = value.let {
        if (it == null) {
            // does not exist so nothing to delete
            return false
        } else {
            // With delete the prev value for complex types needs to be set to check final and required states
            // Only current values are checked on content
            when (reference) {
                is MapReference<*, *, *> -> mapOf<Any, Any>() as T
                is ListReference<*, *> -> listOf<Any>() as T
                is SetReference<*, *> -> setOf<Any>() as T
                is EmbeddedValuesPropertyRef<*, *, *> -> (reference.propertyDefinition.definition.dataModel as IsDataModelWithValues<*, *, *>).values { EmptyValueItems } as T
                is MapValueReference<*, *, *> -> {
//                    val mapReference = reference.parentReference as IsMapReference<Any, Any, IsPropertyContext, IsMapDefinitionWrapper<Any, Any, Any, IsPropertyContext, *>>
//                    createCountUpdater(
//                        values,
//                        mapReference as IsPropertyReference<Map<*, *>, IsPropertyDefinition<Map<*, *>>, out Any>,
//                        version,
//                        -1,
//                        keepAllVersions
//                    ) { newCount ->
//                        mapReference.propertyDefinition.definition.validateSize(newCount) { mapReference }
//                    }
                    // Map values can be set to null to be deleted.
                    shouldHandlePrevValue = false
//                    it
                    TODO("CHANGE DELETE MAP ITEM REFERENCE")
                }
                is ListItemReference<*, *> -> {
//                    val listReference = reference.parentReference as ListReference<Any, IsPropertyContext>
//                    val listDefinition = listReference.propertyDefinition.definition
//                    createCountUpdater(
//                        values,
//                        listReference as IsPropertyReference<List<*>, IsPropertyDefinition<List<*>>, out Any>,
//                        version,
//                        -1,
//                        keepAllVersions
//                    ) { newCount ->
//                        toShiftListCount = newCount - reference.index.toUInt()
//                        listDefinition.validateSize(newCount.toUInt()) { listReference }
//                    }
//                    referenceOfParent = listReference.toStorageByteArray()
                    // Map values can be set to null to be deleted.
                    shouldHandlePrevValue = false
//                    it
                    TODO("CHANGE DELETE LIST ITEM REFERENCE")
                }
                is SetItemReference<*, *> -> {
//                    val setReference = reference.parentReference as SetReference<Any, IsPropertyContext>
//                    createCountUpdater(
//                        values,
//                        setReference as IsPropertyReference<Set<*>, IsPropertyDefinition<Set<*>>, out Any>,
//                        version,
//                        -1,
//                        keepAllVersions
//                    ) { newCount ->
//                        setReference.propertyDefinition.definition.validateSize(newCount) { setReference }
//                    }
                    // Map values can be set to null to be deleted.
                    shouldHandlePrevValue = false
//                    it
                    TODO("CHANGE DELETE SET ITEM REFERENCE")
                }
                else -> it
            }
        }
    }

    if (shouldHandlePrevValue) {
        // Primarily for validations
        handlePreviousValue?.invoke(referenceToCompareTo, prevValue)
    }

    var isDeleted = false

    // Delete value and complex sub parts below same reference
//    for (index in valueIndex until values.size) {
//        val value = values[index]
//        val refOfParent = referenceOfParent
//
//        if (value.reference.matchPart(0, referenceToCompareTo)) {
//            if (toShiftListCount <= 0u) {
//                // Delete if not a list or no further list items
//                isDeleted = deleteByIndex<T>(values, index, value.reference, version) != null
//            }
//        } else if (refOfParent != null && value.reference.matchPart(0, refOfParent)) {
//            // To handle list shifting
//            if (toShiftListCount > 0u) {
//                @Suppress("UNCHECKED_CAST")
//                setValueAtIndex(
//                    values,
//                    index - 1,
//                    values[index - 1].reference,
//                    (value as DataRecordValue<Any>).value,
//                    version,
//                    keepAllVersions
//                )
//                toShiftListCount--
//            }
//
//            if (toShiftListCount <= 0u) {
//                isDeleted = deleteByIndex<T>(values, index, value.reference, version) != null
//            }
//        } else {
//            break
//        }
//    }

    return isDeleted
}
