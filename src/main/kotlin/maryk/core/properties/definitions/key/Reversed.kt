package maryk.core.properties.definitions.key

import maryk.core.extensions.bytes.MAX_BYTE
import maryk.core.objects.AbstractDataModel
import maryk.core.objects.IsDataModel
import maryk.core.objects.PropertyDefinitionsContext
import maryk.core.properties.definitions.IsFixedBytesProperty
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextualPropertyReferenceDefinition
import maryk.core.properties.definitions.wrapper.FixedBytesPropertyDefinitionWrapper
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.references.ValueWithFixedBytesPropertyReference
import kotlin.experimental.xor

/** Class to reverse key parts of type [T] by [reference] in key. */
data class Reversed<T: Any>(
        val reference: ValueWithFixedBytesPropertyReference<T, FixedBytesPropertyDefinitionWrapper<T, *, *, *>, *>
) : IsFixedBytesProperty<T> {
    override val byteSize = this.reference.propertyDefinition.byteSize
    override fun <DO : Any> getValue(dataModel: IsDataModel<DO>, dataObject: DO) = this.reference.propertyDefinition.getValue(dataModel, dataObject)

    /** Convenience constructor to pass [definition] */
    constructor(definition: FixedBytesPropertyDefinitionWrapper<T, *, *, *>) : this(definition.getRef())

    override fun writeStorageBytes(value: T, writer: (byte: Byte) -> Unit) {
        this.reference.propertyDefinition.writeStorageBytes(value, {
            writer(MAX_BYTE xor it)
        })
    }

    override fun readStorageBytes(length: Int, reader: () -> Byte): T {
        return this.reference.propertyDefinition.readStorageBytes(byteSize, {
            MAX_BYTE xor reader()
        })
    }

    @Suppress("UNCHECKED_CAST")
    object Model : AbstractDataModel<Reversed<*>, PropertyDefinitions<Reversed<*>>, PropertyDefinitionsContext, PropertyDefinitionsContext>(
            properties = object : PropertyDefinitions<Reversed<*>>() {
                init {
                    add(0, "multiTypeDefinition", ContextualPropertyReferenceDefinition<PropertyDefinitionsContext>(
                            contextualResolver = { it!!.propertyDefinitions!! }
                    )) {
                        it.reference as IsPropertyReference<Any, *>
                    }
                }
            }
    ) {
        override fun invoke(map: Map<Int, *>) = Reversed(
            reference = map[0] as ValueWithFixedBytesPropertyReference<Any, FixedBytesPropertyDefinitionWrapper<Any, *, *, *>, *>
        )
    }
}