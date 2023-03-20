package maryk.core.models

import maryk.core.properties.IsObjectPropertyDefinitions
import maryk.core.properties.IsSimpleBaseModel
import maryk.core.properties.IsValuesPropertyDefinitions
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.IsEmbeddedObjectDefinition
import maryk.core.properties.definitions.IsPropertyDefinition
import maryk.core.properties.definitions.wrapper.ObjectListDefinitionWrapper
import maryk.core.properties.exceptions.ValidationUmbrellaException
import maryk.core.properties.references.IsPropertyReference
import maryk.core.query.RequestContext
import maryk.core.values.IsValueItems
import maryk.core.values.MutableValueItems
import maryk.core.values.ObjectValues

/** A DataModel which holds properties and can be validated */
interface IsObjectDataModel<DO : Any, DM : IsObjectPropertyDefinitions<DO>> :
    IsDataModelWithValues<DO, DM, ObjectValues<DO, DM>> {
    /**
     * Validate a [dataObject] and get reference from [refGetter] if exception needs to be thrown
     * @throws ValidationUmbrellaException if input was invalid
     */
    fun validate(dataObject: DO, refGetter: () -> IsPropertyReference<DO, IsPropertyDefinition<DO>, *>? = { null })

    /** Create a ObjectValues with given [createValues] function */
    override fun values(context: RequestContext?, createValues: DM.() -> IsValueItems) =
        ObjectValues(this.properties, createValues(this.properties), context)
}

/**
 * Converts a DataObject back to ObjectValues
 */
fun <DO : Any, DM : ObjectPropertyDefinitions<DO>> DM.asValues(
    dataObject: DO,
    context: RequestContext? = null
): ObjectValues<DO, DM> {
    val mutableMap = MutableValueItems()

    @Suppress("UNCHECKED_CAST")
    for (property in this) {
        when (property) {
            is ObjectListDefinitionWrapper<out Any, *, *, *, DO> -> {
                val dataModel =
                    (property.definition.valueDefinition as EmbeddedObjectDefinition<Any, IsSimpleBaseModel<Any, *, *>, *, *>).dataModel as ObjectPropertyDefinitions<Any>
                property.getter(dataObject)?.let { list ->
                    mutableMap[property.index] = list.map {
                        dataModel.asValues(it, context)
                    }.toList()
                }
            }
            is IsEmbeddedObjectDefinition<*, *, *, *> -> {
                val dataModel = property.dataModel as ObjectPropertyDefinitions<Any>
                property.getter(dataObject)?.let {
                    mutableMap[property.index] = dataModel.asValues(it, context)
                }
            }
            else -> property.getter(dataObject)?.let {
                mutableMap[property.index] = it
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    return ((this as IsObjectPropertyDefinitions<DO>).Model as IsObjectDataModel<DO, DM>).values(context) {
        mutableMap
    }
}
