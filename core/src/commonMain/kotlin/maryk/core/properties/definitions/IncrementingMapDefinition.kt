package maryk.core.properties.definitions

import maryk.core.exceptions.RequestException
import maryk.core.models.ContextualDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextTransformerDefinition
import maryk.core.properties.types.TypedValue
import maryk.core.properties.types.numeric.NumberDescriptor
import maryk.core.properties.types.numeric.NumberType
import maryk.core.query.ContainsDefinitionsContext
import maryk.core.values.SimpleObjectValues

/** Definition for Map property in which the key auto increments */
data class IncrementingMapDefinition<K : Comparable<K>, V : Any, CX : IsPropertyContext> internal constructor(
    override val required: Boolean = true,
    override val final: Boolean = false,
    override val minSize: UInt? = null,
    override val maxSize: UInt? = null,
    override val keyDefinition: NumberDefinition<K>,
    override val valueDefinition: IsSubDefinition<V, CX>
) :
    IsUsableInMapValue<Map<K, V>, CX>,
    IsUsableInMultiType<Map<K, V>, CX>,
    IsMapDefinition<K, V, CX>,
    IsTransportablePropertyDefinitionType<Map<K, V>> {
    override val propertyDefinitionType = PropertyDefinitionType.IncMap

    val keyNumberDescriptor get() = keyDefinition.type

    init {
        require(keyDefinition.required) { "Definition for key should be required on map" }
        require(valueDefinition.required) { "Definition for value should be required on map" }
    }

    constructor(
        required: Boolean = true,
        final: Boolean = false,
        minSize: UInt? = null,
        maxSize: UInt? = null,
        keyNumberDescriptor: NumberDescriptor<K>,
        valueDefinition: IsUsableInMapValue<V, CX>
    ) : this(
        required,
        final,
        minSize,
        maxSize,
        NumberDefinition(type = keyNumberDescriptor, reversedStorage = true),
        valueDefinition as IsSubDefinition<V, CX>
    )

    object Model :
        ContextualDataModel<IncrementingMapDefinition<*, *, *>, ObjectPropertyDefinitions<IncrementingMapDefinition<*, *, *>>, ContainsDefinitionsContext, KeyValueDefinitionContext>(
            contextTransformer = { KeyValueDefinitionContext(it) },
            properties = object : ObjectPropertyDefinitions<IncrementingMapDefinition<*, *, *>>() {
                init {
                    IsPropertyDefinition.addRequired(this, IncrementingMapDefinition<*, *, *>::required)
                    IsPropertyDefinition.addFinal(this, IncrementingMapDefinition<*, *, *>::final)
                    HasSizeDefinition.addMinSize(3u, this, IncrementingMapDefinition<*, *, *>::minSize)
                    HasSizeDefinition.addMaxSize(4u, this, IncrementingMapDefinition<*, *, *>::maxSize)

                    @Suppress("UNCHECKED_CAST")
                    add(5u, "keyNumberDescriptor",
                        definition = EnumDefinition(enum = NumberType),
                        getter = IncrementingMapDefinition<*, *, *>::keyNumberDescriptor as (IncrementingMapDefinition<*, *, *>) -> NumberDescriptor<Comparable<Any>>?,
                        capturer = { context: KeyValueDefinitionContext, value: NumberType ->
                            context.keyDefinition = value as IsSimpleValueDefinition<Any, IsPropertyContext>
                        },
                        fromSerializable = { value: NumberType? ->
                            value?.let {
                                it.descriptor() as NumberDescriptor<Comparable<Any>>
                            }
                        },
                        toSerializable = { value: NumberDescriptor<Comparable<Any>>?, _: KeyValueDefinitionContext? ->
                            value?.type
                        }
                    )

                    add(6u, "valueDefinition",
                        ContextTransformerDefinition(
                            contextTransformer = { it?.definitionsContext },
                            definition = InternalMultiTypeDefinition(
                                typeEnum = PropertyDefinitionType,
                                definitionMap = mapOfPropertyDefEmbeddedObjectDefinitions
                            )
                        ),
                        getter = IncrementingMapDefinition<*, *, *>::valueDefinition,
                        toSerializable = { value, _ ->
                            val defType = value as? IsTransportablePropertyDefinitionType<*>
                                ?: throw RequestException("$value is not transportable")
                            TypedValue(defType.propertyDefinitionType, value)
                        },
                        fromSerializable = {
                            it?.value as IsSubDefinition<*, *>?
                        },
                        capturer = { context: KeyValueDefinitionContext, value ->
                            @Suppress("UNCHECKED_CAST")
                            context.valueDefinition = value.value as IsSubDefinition<Any, IsPropertyContext>
                        }
                    )
                }
            }
        ) {
        override fun invoke(values: SimpleObjectValues<IncrementingMapDefinition<*, *, *>>) = IncrementingMapDefinition<Comparable<Any>, Any, IsPropertyContext>(
            required = values(1u),
            final = values(2u),
            minSize = values(3u),
            maxSize = values(4u),
            keyDefinition = NumberDefinition(
                type = values(5u),
                reversedStorage = true
            ),
            valueDefinition = values(6u)
        )
    }
}
