package maryk.core.properties.definitions

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.exceptions.DefNotFoundException
import maryk.core.models.AbstractValuesDataModel
import maryk.core.models.ContextualDataModel
import maryk.core.models.IsValuesDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.IsValuesPropertyDefinitions
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.PropertyDefinitionType.Embed
import maryk.core.properties.definitions.contextual.ContextualModelReferenceDefinition
import maryk.core.properties.definitions.contextual.DataModelReference
import maryk.core.properties.definitions.contextual.IsDataModelReference
import maryk.core.properties.definitions.contextual.ModelContext
import maryk.core.properties.definitions.contextual.embedContextual
import maryk.core.properties.definitions.wrapper.DefinitionWrapperDelegateLoader
import maryk.core.properties.definitions.wrapper.EmbeddedValuesDefinitionWrapper
import maryk.core.properties.definitions.wrapper.IsDefinitionWrapper
import maryk.core.properties.definitions.wrapper.ObjectDefinitionWrapperDelegateLoader
import maryk.core.properties.definitions.wrapper.contextual
import maryk.core.properties.references.IsPropertyReference
import maryk.core.protobuf.WireType.LENGTH_DELIMITED
import maryk.core.protobuf.WriteCacheReader
import maryk.core.protobuf.WriteCacheWriter
import maryk.core.query.ContainsDefinitionsContext
import maryk.core.values.ObjectValues
import maryk.core.values.Values
import maryk.json.IsJsonLikeReader
import maryk.json.IsJsonLikeWriter
import maryk.json.JsonReader
import maryk.json.JsonWriter
import maryk.lib.safeLazy

/** Definition for embedded object properties [P] to [dataModel] of type [DM] */
class EmbeddedValuesDefinition<DM : IsValuesDataModel<P>, P : IsValuesPropertyDefinitions>(
    override val required: Boolean = true,
    override val final: Boolean = false,
    dataModel: Unit.() -> DM,
    override val default: Values<P>? = null
) :
    IsEmbeddedValuesDefinition<DM, P, IsPropertyContext>,
    IsTransportablePropertyDefinitionType<Values<P>> {
    override val propertyDefinitionType = Embed
    override val wireType = LENGTH_DELIMITED

    private val internalDataModel = safeLazy(dataModel)
    override val dataModel: DM get() = internalDataModel.value

    @Suppress("UNCHECKED_CAST")
    // internal strong typed version so type system is not in a loop when creating EmbeddedValuesDefinition
    internal val typedDataModel get() =
        internalDataModel.value as AbstractValuesDataModel<DM, P, IsPropertyContext>

    override fun asString(value: Values<P>, context: IsPropertyContext?): String {
        var string = ""
        this.writeJsonValue(value, JsonWriter {
            string += it
        }, context)
        return string
    }

    override fun fromString(string: String, context: IsPropertyContext?): Values<P> {
        val stringIterator = string.iterator()
        return this.readJson(JsonReader { stringIterator.nextChar() }, context)
    }

    override fun getEmbeddedByName(name: String): IsDefinitionWrapper<*, *, *, *>? = dataModel.properties[name]

    override fun getEmbeddedByIndex(index: UInt): IsDefinitionWrapper<*, *, *, *>? = dataModel.properties[index]

    override fun validateWithRef(
        previousValue: Values<P>?,
        newValue: Values<P>?,
        refGetter: () -> IsPropertyReference<Values<P>, IsPropertyDefinition<Values<P>>, *>?
    ) {
        super<IsEmbeddedValuesDefinition>.validateWithRef(previousValue, newValue, refGetter)
        if (newValue != null) {
            this.typedDataModel.validate(
                values = newValue,
                refGetter = refGetter
            )
        }
    }

    override fun writeJsonValue(value: Values<P>, writer: IsJsonLikeWriter, context: IsPropertyContext?) =
        this.typedDataModel.writeJson(
            value,
            writer,
            context
        )

    override fun readJson(reader: IsJsonLikeReader, context: IsPropertyContext?) =
        this.typedDataModel.readJson(reader, context)

    override fun calculateTransportByteLength(
        value: Values<P>,
        cacher: WriteCacheWriter,
        context: IsPropertyContext?
    ) =
        this.typedDataModel.calculateProtoBufLength(
            value,
            cacher,
            context
        )

    override fun writeTransportBytes(
        value: Values<P>,
        cacheGetter: WriteCacheReader,
        writer: (byte: Byte) -> Unit,
        context: IsPropertyContext?
    ) {
        this.typedDataModel.writeProtoBuf(
            value,
            cacheGetter,
            writer,
            context
        )
    }

    override fun readTransportBytes(
        length: Int,
        reader: () -> Byte,
        context: IsPropertyContext?,
        earlierValue: Values<P>?
    ) =
        this.typedDataModel.readProtoBuf(length, reader, context)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmbeddedValuesDefinition<*, *>) return false

        if (required != other.required) return false
        if (final != other.final) return false
        if (internalDataModel.value != other.internalDataModel.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = required.hashCode()
        result = 31 * result + final.hashCode()
        result = 31 * result + internalDataModel.value.hashCode()
        return result
    }

    @Suppress("unused")
    object Model :
        ContextualDataModel<EmbeddedValuesDefinition<*, *>, ObjectPropertyDefinitions<EmbeddedValuesDefinition<*, *>>, ContainsDefinitionsContext, ModelContext>(
            contextTransformer = { ModelContext(it) },
            properties = object : ObjectPropertyDefinitions<EmbeddedValuesDefinition<*, *>>() {
                val required by boolean(1u, EmbeddedValuesDefinition<*, *>::required, default = true)
                val final by boolean(2u, EmbeddedValuesDefinition<*, *>::final, default = false)
                val dataModel by contextual(
                    index = 3u,
                    definition = ContextualModelReferenceDefinition(
                        contextTransformer = { context: ModelContext? ->
                            context?.definitionsContext
                        },
                        contextualResolver = { context: ContainsDefinitionsContext?, name ->
                            context?.let {
                                @Suppress("UNCHECKED_CAST")
                                it.dataModels[name] as? Unit.() -> IsValuesDataModel<*>
                                    ?: throw DefNotFoundException("ObjectDataModel of name $name not found on dataModels")
                            } ?: throw ContextNotFoundException()
                        }
                    ),
                    getter = {
                        { it.dataModel }
                    },
                    toSerializable = { value: (Unit.() -> IsValuesDataModel<*>)?, _ ->
                        value?.invoke(Unit)?.let { model ->
                            DataModelReference(model.name, value)
                        }
                    },
                    fromSerializable = { it?.get },
                    capturer = { context: ModelContext, dataModel: IsDataModelReference<IsValuesDataModel<*>> ->
                        context.definitionsContext?.let {
                            if (!it.dataModels.containsKey(dataModel.name)) {
                                it.dataModels[dataModel.name] = dataModel.get
                            }
                        } ?: throw ContextNotFoundException()

                        context.model = dataModel.get
                    }
                )

                @Suppress("UNCHECKED_CAST")
                val default by embedContextual(
                    index = 4u,
                    getter = EmbeddedValuesDefinition<*, *>::default,
                    contextualResolver = { context: ModelContext? ->
                        context?.model?.invoke(Unit) as? AbstractValuesDataModel<IsValuesDataModel<IsValuesPropertyDefinitions>, IsValuesPropertyDefinitions, ModelContext>?
                            ?: throw ContextNotFoundException()
                    }
                )
            }
        ) {
        override fun invoke(values: ObjectValues<EmbeddedValuesDefinition<*, *>, ObjectPropertyDefinitions<EmbeddedValuesDefinition<*, *>>>) =
            EmbeddedValuesDefinition<IsValuesDataModel<IsValuesPropertyDefinitions>, IsValuesPropertyDefinitions>(
                required = values(1u),
                final = values(2u),
                dataModel = values(3u),
                default = values(4u)
            )
    }
}

fun <P : IsValuesPropertyDefinitions, DM : IsValuesDataModel<P>> IsValuesPropertyDefinitions.embed(
    index: UInt,
    dataModel: Unit.() -> DM,
    name: String? = null,
    required: Boolean = true,
    final: Boolean = false,
    default: Values<P>? = null,
    alternativeNames: Set<String>? = null
) = DefinitionWrapperDelegateLoader(this) { propName ->
    EmbeddedValuesDefinitionWrapper(
        index,
        name ?: propName,
        EmbeddedValuesDefinition(required, final, dataModel, default),
        alternativeNames
    )
}

fun <P : IsValuesPropertyDefinitions, DM : IsValuesDataModel<P>> ObjectPropertyDefinitions<Any>.embed(
    index: UInt,
    getter: (Any) -> Values<P>? = { null },
    dataModel: Unit.() -> DM,
    name: String? = null,
    required: Boolean = true,
    final: Boolean = false,
    default: Values<P>? = null,
    alternativeNames: Set<String>? = null,
    toSerializable: (Unit.(Values<P>?, IsPropertyContext?) -> Values<P>?)? = null,
    fromSerializable: (Unit.(Values<P>?) -> Values<P>?)? = null,
    shouldSerialize: (Unit.(Any) -> Boolean)? = null,
    capturer: (Unit.(IsPropertyContext, Values<P>) -> Unit)? = null
) = ObjectDefinitionWrapperDelegateLoader(this) { propName ->
    EmbeddedValuesDefinitionWrapper(
        index,
        name ?: propName,
        EmbeddedValuesDefinition(required, final, dataModel, default),
        alternativeNames,
        getter = getter,
        capturer = capturer,
        toSerializable = toSerializable,
        fromSerializable = fromSerializable,
        shouldSerialize = shouldSerialize
    )
}
