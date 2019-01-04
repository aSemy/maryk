@file:Suppress("EXPERIMENTAL_API_USAGE")

package maryk.core.processors.datastore

import maryk.core.extensions.bytes.initIntByVarWithExtraInfo
import maryk.core.extensions.bytes.initUInt
import maryk.core.models.IsDataModel
import maryk.core.models.IsRootValuesDataModel
import maryk.core.processors.datastore.ChangeType.CHANGE
import maryk.core.processors.datastore.StorageTypeEnum.Value
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.PropertyDefinitions
import maryk.core.properties.definitions.IsListDefinition
import maryk.core.properties.definitions.IsMapDefinition
import maryk.core.properties.definitions.IsPropertyDefinition
import maryk.core.properties.definitions.IsSetDefinition
import maryk.core.properties.definitions.IsSimpleValueDefinition
import maryk.core.properties.definitions.IsSubDefinition
import maryk.core.properties.definitions.wrapper.IsValuePropertyDefinitionWrapper
import maryk.core.properties.definitions.wrapper.MapPropertyDefinitionWrapper
import maryk.core.properties.graph.IsPropRefGraph
import maryk.core.properties.graph.RootPropRefGraph
import maryk.core.properties.references.AnyValuePropertyReference
import maryk.core.properties.references.CompleteReferenceType.DELETE
import maryk.core.properties.references.CompleteReferenceType.MAP_KEY
import maryk.core.properties.references.CompleteReferenceType.TYPE
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.references.ListItemReference
import maryk.core.properties.references.ListReference
import maryk.core.properties.references.MapReference
import maryk.core.properties.references.MapValueReference
import maryk.core.properties.references.ReferenceType.LIST
import maryk.core.properties.references.ReferenceType.MAP
import maryk.core.properties.references.ReferenceType.SET
import maryk.core.properties.references.ReferenceType.SPECIAL
import maryk.core.properties.references.ReferenceType.VALUE
import maryk.core.properties.references.SetItemReference
import maryk.core.properties.references.SetReference
import maryk.core.properties.references.completeReferenceTypeOf
import maryk.core.properties.references.referenceStorageTypeOf
import maryk.core.query.changes.Change
import maryk.core.query.changes.Delete
import maryk.core.query.changes.IsChange
import maryk.core.query.changes.ListChange
import maryk.core.query.changes.ListValueChanges
import maryk.core.query.changes.MapChange
import maryk.core.query.changes.MapValueChanges
import maryk.core.query.changes.SetChange
import maryk.core.query.changes.SetValueChanges
import maryk.core.query.changes.VersionedChanges
import maryk.core.query.pairs.ReferenceValuePair

typealias ValueWithVersionReader = (StorageTypeEnum<IsPropertyDefinition<Any>>, IsPropertyDefinition<Any>?, (ULong, Any?) -> Unit) -> Unit
private typealias ChangeAdder = (ULong, ChangeType, Any) -> Unit

private enum class ChangeType {
    CHANGE, DELETE, MAPADD, MAPDELETE, LISTADD, LISTDELETE, SETADD, SETDELETE
}

/**
 * Convert storage bytes to values.
 * [getQualifier] gets a qualifier until none is available and returns null
 * [processValue] processes the storage value with given type and definition
 */
fun <DM: IsRootValuesDataModel<P>, P: PropertyDefinitions> DM.convertStorageToChanges(
    getQualifier: () -> ByteArray?,
    select: RootPropRefGraph<P>?,
    processValue: ValueWithVersionReader
): List<VersionedChanges> {
    // Used to collect all found ValueItems
    val mutableVersionedChanges = mutableListOf<VersionedChanges>()

    // Adds changes to versionedChangesCollection
    val changeAdder: ChangeAdder = { version: ULong, changeType: ChangeType, changePart: Any ->
        val index = mutableVersionedChanges.binarySearch { it.version.compareTo(version) }

        if (index < 0) {
            mutableVersionedChanges.add(
                (index * -1) - 1,
                VersionedChanges(version, mutableListOf(createChange(changeType, changePart)))
            )
        } else {
            (mutableVersionedChanges[index].changes as MutableList<IsChange>).addChange(changeType, changePart)
        }
    }

    processQualifiers(getQualifier) { qualifier, addToCache ->
        // Otherwise try to get a new qualifier processor from DataModel
        (this as IsDataModel<P>).readQualifier(qualifier, 0, select, changeAdder, processValue, addToCache)
    }

    // Create Values
    return mutableVersionedChanges
}

/** Adds change to existing list or creates a new change*/
private fun MutableList<IsChange>.addChange(changeType: ChangeType, changePart: Any) {
    @Suppress("UNCHECKED_CAST")
    when(changeType) {
        ChangeType.CHANGE -> this.find { it is Change }?.also { ((it as Change).referenceValuePairs as MutableList<ReferenceValuePair<*>>).add(changePart as ReferenceValuePair<*>) }
        ChangeType.DELETE -> this.find { it is Delete }?.also { ((it as Delete).references as MutableList<AnyValuePropertyReference>).add(changePart as AnyValuePropertyReference) }
        ChangeType.LISTADD -> {
            this.find { it is ListChange }?.also { change ->
                val refToValue = changePart as Pair<ListItemReference<*, *>, Any>
                (((change as ListChange).listValueChanges as MutableList<ListValueChanges<*>>).find { it.reference == refToValue.first.parentReference }?.addValuesAtIndex as MutableMap<Int, Any>)[refToValue.first.index] = refToValue.second
            }
        }
        ChangeType.LISTDELETE -> {
            this.find { it is ListChange }?.also { change ->
                val ref = changePart as ListItemReference<*, *>
                (((change as ListChange).listValueChanges as MutableList<ListValueChanges<*>>).find { it.reference == ref.parentReference }?.deleteAtIndex as MutableSet<Any>).add(ref.index)
            }
        }
        ChangeType.MAPADD -> {
            this.find { it is MapChange }?.also { change ->
                val refAndPair = changePart as Pair<IsPropertyReference<Map<Any, Any>, MapPropertyDefinitionWrapper<Any, Any, *, *, *>, *>, Pair<Any, Any>>
                (((change as MapChange).mapValueChanges as MutableList<MapValueChanges<*, *>>).find { it.reference == refAndPair.first }?.valuesToAdd as MutableMap<Any, Any>)[refAndPair.second.first] = refAndPair.second.second
            }
        }
        ChangeType.MAPDELETE -> {
            this.find { it is MapChange }?.also { change ->
                val ref = changePart as MapValueReference<*, *, *>
                (((change as MapChange).mapValueChanges as MutableList<MapValueChanges<*, *>>).find { it.reference == ref.parentReference }?.keysToDelete as MutableSet<Any>).add(ref.key)
            }
        }
        ChangeType.SETADD -> {
            this.find { it is SetChange }?.also { change ->
                val ref = changePart as SetItemReference<*, *>
                (((change as SetChange).setValueChanges as MutableList<SetValueChanges<*>>).find { it.reference == ref.parentReference }?.addValues as MutableSet<Any>).add(ref.value)
            }
        }
        ChangeType.SETDELETE -> {
            this.find { it is SetChange }?.also { change ->
                val ref = changePart as SetItemReference<*, *>
                (((change as SetChange).setValueChanges as MutableList<SetValueChanges<*>>).find { it.reference == ref.parentReference }?.deleteValues as MutableSet<Any>).add(ref.value)
            }
        }
    } ?: this.add(createChange(changeType, changePart))
}

@Suppress("UNCHECKED_CAST")
private fun createChange(changeType: ChangeType, changePart: Any) = when(changeType) {
    ChangeType.CHANGE -> Change(mutableListOf(changePart as ReferenceValuePair<Any>))
    ChangeType.DELETE -> Delete(mutableListOf(changePart as IsPropertyReference<*, IsValuePropertyDefinitionWrapper<*, *, IsPropertyContext, *>, *>))
    ChangeType.LISTADD-> {
        val refToValue = changePart as Pair<ListItemReference<*, *>, Any>
        ListChange(mutableListOf(
            ListValueChanges(
                refToValue.first.parentReference as IsPropertyReference<List<Any>, IsPropertyDefinition<List<Any>>, *>,
                addValuesAtIndex = mutableMapOf(refToValue.first.index to refToValue.second),
                deleteAtIndex = mutableSetOf()
            )
        ))
    }
    ChangeType.LISTDELETE-> {
        val ref = changePart as ListItemReference<*, *>
        ListChange(mutableListOf(
            ListValueChanges(
                ref.parentReference as IsPropertyReference<List<Any>, IsPropertyDefinition<List<Any>>, *>,
                addValuesAtIndex = mutableMapOf(),
                deleteAtIndex = mutableSetOf(ref.index)
            )
        ))
    }
    ChangeType.MAPADD -> {
        val refAndPair = changePart as Pair<IsPropertyReference<Map<Any, Any>, MapPropertyDefinitionWrapper<Any, Any, *, *, *>, *>, Pair<Any, Any>>
        MapChange(mutableListOf(
            MapValueChanges(
                refAndPair.first,
                valuesToAdd = mutableMapOf(refAndPair.second),
                keysToDelete = mutableSetOf()
            )
        ))
    }
    ChangeType.MAPDELETE -> {
        val ref = changePart as MapValueReference<*, *, *>
        MapChange(mutableListOf(
            MapValueChanges(
                ref.parentReference as IsPropertyReference<Map<Any, Any>, MapPropertyDefinitionWrapper<Any, Any, *, *, *>, *>,
                valuesToAdd = mutableMapOf(),
                keysToDelete = mutableSetOf(ref.key)
            )
        ))
    }
    ChangeType.SETADD-> {
        val ref = changePart as SetItemReference<*, *>
        SetChange(mutableListOf(
            SetValueChanges(
                ref.parentReference as IsPropertyReference<Set<Any>, IsPropertyDefinition<Set<Any>>, *>,
                addValues = mutableSetOf(ref.value),
                deleteValues = mutableSetOf()
            )
        ))
    }
    ChangeType.SETDELETE-> {
        val ref = changePart as SetItemReference<*, *>
        SetChange(mutableListOf(
            SetValueChanges(
                ref.parentReference as IsPropertyReference<Set<Any>, IsPropertyDefinition<Set<Any>>, *>,
                addValues = mutableSetOf(),
                deleteValues = mutableSetOf(ref.value)
            )
        ))
    }
}

/**
 * Read specific [qualifier] from [offset].
 * [addChangeToOutput] is used to add changes to output
 * [readValueFromStorage] is used to fetch actual value from storage layer
 * [addToCache] is used to add a sub reader to cache so it does not need to reprocess qualifier from start
 */
private fun <P: PropertyDefinitions> IsDataModel<P>.readQualifier(
    qualifier: ByteArray,
    offset: Int,
    select: IsPropRefGraph<P>?,
    addChangeToOutput: ChangeAdder,
    readValueFromStorage: ValueWithVersionReader,
    addToCache: CacheProcessor
) {
    var qIndex = offset

    initIntByVarWithExtraInfo({ qualifier[qIndex++] }) { index, type ->
        val subSelect = select?.selectNodeOrNull(index)

        if (select != null && subSelect == null) {
            // Return null if not selected within select
            null
        } else {
            val isAtEnd = qualifier.size <= qIndex
            when (referenceStorageTypeOf(type)) {
                SPECIAL -> when (val specialType = completeReferenceTypeOf(qualifier[offset])) {
                    DELETE -> TODO("Implement object delete")
                    TYPE, MAP_KEY -> throw Exception("Cannot handle Special type $specialType in qualifier")
                    else -> throw Exception("Not recognized special type $specialType")
                }
                VALUE -> readValue(isAtEnd, index, qualifier, select, qIndex, addChangeToOutput, readValueFromStorage, addToCache)
                LIST -> if (isAtEnd) {
                    // Used for the list size. Is Ignored for changes
                } else {
                    val definition = this.properties[index]!!
                    @Suppress("UNCHECKED_CAST")
                    val listDefinition = definition as IsListDefinition<Any, IsPropertyContext>
                    @Suppress("UNCHECKED_CAST")
                    val reference = definition.getRef() as ListReference<Any, IsPropertyContext>

                    // Read set contents. Always a simple value for set since it is in qualifier
                    val valueDefinition = ((definition as IsListDefinition<*, *>).valueDefinition as IsSimpleValueDefinition<*, *>)
                    var listItemIndex = qIndex

                    val listIndex = initUInt(reader = { qualifier[listItemIndex++] }).toInt()

                    @Suppress("UNCHECKED_CAST")
                    readValueFromStorage(Value as StorageTypeEnum<IsPropertyDefinition<Any>>, valueDefinition as IsPropertyDefinition<Any>) { version, value ->
                        if (value != null) {
                            addChangeToOutput(version, ChangeType.LISTADD, listDefinition.getItemRef(listIndex, reference) to value)
                        } else {
                            addChangeToOutput(version, ChangeType.LISTDELETE, listDefinition.getItemRef(listIndex, reference))
                        }
                    }
                }
                SET -> if (isAtEnd) {
                    // Used for the set size. Is Ignored for changes
                } else {
                    val definition = this.properties[index]!!
                    @Suppress("UNCHECKED_CAST")
                    val setDefinition = definition as IsSetDefinition<Any, IsPropertyContext>
                    @Suppress("UNCHECKED_CAST")
                    val reference = definition.getRef() as SetReference<Any, IsPropertyContext>

                    // Read set contents. Always a simple value for set since it is in qualifier
                    val valueDefinition = ((definition as IsSetDefinition<*, *>).valueDefinition as IsSimpleValueDefinition<*, *>)
                    var setItemIndex = qIndex

                    val key = valueDefinition.readStorageBytes(qualifier.size - qIndex) { qualifier[setItemIndex++] }

                    @Suppress("UNCHECKED_CAST")
                    readValueFromStorage(Value as StorageTypeEnum<IsPropertyDefinition<Any>>, valueDefinition as IsPropertyDefinition<Any>) { version, value ->
                        if (value != null) {
                            addChangeToOutput(version, ChangeType.SETADD, setDefinition.getItemRef(key, reference))
                        } else {
                            addChangeToOutput(version, ChangeType.SETDELETE, setDefinition.getItemRef(key, reference))
                        }
                    }
                }
                MAP -> if (isAtEnd) {
                    // Used for the map size. Is Ignored for changes
                } else {
                    val definition = this.properties[index]!!
                    @Suppress("UNCHECKED_CAST")
                    val mapDefinition = definition as IsMapDefinition<Any, Any, IsPropertyContext>
                    @Suppress("UNCHECKED_CAST")
                    val reference = definition.getRef() as MapReference<Any, Any, IsPropertyContext>

                    // Read set contents. Always a simple value for set since it is in qualifier
                    val keyDefinition = ((definition as IsMapDefinition<*, *, *>).keyDefinition as IsSimpleValueDefinition<*, *>)
                    val valueDefinition = ((definition as IsMapDefinition<*, *, *>).valueDefinition as IsSubDefinition<*, *>)
                    var mapItemIndex = qIndex

                    val key = keyDefinition.readStorageBytes(qualifier.size - qIndex) { qualifier[mapItemIndex++] }

                    @Suppress("UNCHECKED_CAST")
                    readValueFromStorage(Value as StorageTypeEnum<IsPropertyDefinition<Any>>, valueDefinition as IsPropertyDefinition<Any>) { version, value ->
                        if (value != null) {
                            addChangeToOutput(version, ChangeType.MAPADD, Pair(reference, Pair(key, value)))
                        } else {
                            addChangeToOutput(version, ChangeType.MAPDELETE, mapDefinition.getValueRef(key, reference))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reads a specific value type
 * [isAtEnd] if qualifier is at the end
 * [index] of the item to be injected in output
 * [qualifier] in storage of current value
 * [select] to only process selected values
 * [offset] in bytes from where qualifier
 * [addChangeToOutput] / [readValueFromStorage]
 * [addToCache] so next qualifiers do not need to reprocess qualifier
 */
@Suppress("UNUSED_PARAMETER")
private fun <P : PropertyDefinitions> IsDataModel<P>.readValue(
    isAtEnd: Boolean,
    index: Int,
    qualifier: ByteArray,
    select: IsPropRefGraph<P>?,
    offset: Int,
    addChangeToOutput: ChangeAdder,
    readValueFromStorage: ValueWithVersionReader,
    addToCache: CacheProcessor
) {
    if (isAtEnd) {
        val propDefinition = this.properties[index]!!

        @Suppress("UNCHECKED_CAST")
        readValueFromStorage(
            Value as StorageTypeEnum<IsPropertyDefinition<Any>>,
            propDefinition
        ) { version, value ->
            val ref = propDefinition.getRef() as IsPropertyReference<Any, IsValuePropertyDefinitionWrapper<Any, *, IsPropertyContext, *>, *>
            if (value != null) {
                addChangeToOutput(version, CHANGE, ReferenceValuePair(ref, value))
            } else {
                addChangeToOutput(version, ChangeType.DELETE,ref)
            }
        }
    } else {
        TODO("Implement complex")
    }
}
