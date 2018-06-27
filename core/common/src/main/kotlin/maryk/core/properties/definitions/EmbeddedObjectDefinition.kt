package maryk.core.properties.definitions

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.exceptions.DefNotFoundException
import maryk.core.models.AbstractDataModel
import maryk.core.models.ContextualDataModel
import maryk.core.models.DataModel
import maryk.core.objects.ValueMap
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.contextual.ContextualEmbeddedObjectDefinition
import maryk.core.properties.definitions.contextual.ContextualModelReferenceDefinition
import maryk.core.properties.definitions.contextual.DataModelReference
import maryk.core.properties.definitions.contextual.IsDataModelReference
import maryk.core.properties.definitions.contextual.ModelContext
import maryk.core.properties.definitions.wrapper.IsPropertyDefinitionWrapper
import maryk.core.properties.references.IsPropertyReference
import maryk.core.protobuf.WireType
import maryk.core.protobuf.WriteCacheReader
import maryk.core.protobuf.WriteCacheWriter
import maryk.core.query.DataModelContext
import maryk.json.IsJsonLikeReader
import maryk.json.IsJsonLikeWriter
import maryk.json.JsonReader
import maryk.json.JsonWriter

/** Definition for embedded object properties to [dataModel] of type [DM] returning dataObject of [DO] */
class EmbeddedObjectDefinition<DO : Any, P: PropertyDefinitions<DO>, out DM : AbstractDataModel<DO, P, CXI, CX>, CXI: IsPropertyContext, CX: IsPropertyContext>(
    override val indexed: Boolean = false,
    override val required: Boolean = true,
    override val final: Boolean = false,
    dataModel: () -> DM,
    override val default: DO? = null
) :
    IsValueDefinition<DO, CXI>,
    IsEmbeddedObjectDefinition<DO, P, DM, CXI, CX>,
    IsTransportablePropertyDefinitionType<DO>,
    HasDefaultValueDefinition<DO>
{
    override val propertyDefinitionType = PropertyDefinitionType.Embed
    override val wireType = WireType.LENGTH_DELIMITED

    private val internalDataModel = lazy(dataModel)
    override val dataModel: DM get() = internalDataModel.value

    override fun asString(value: DO, context: CXI?): String {
        var string = ""
        this.writeJsonValue(value, JsonWriter {
            string += it
        }, context)
        return string
    }

    override fun fromString(string: String, context: CXI?): DO {
        val stringIterator = string.iterator()
        return this.readJson(JsonReader { stringIterator.nextChar() }, context)
    }

    override fun getEmbeddedByName(name: String): IsPropertyDefinitionWrapper<*, *, *, *>? = dataModel.properties.getDefinition(name)

    override fun getEmbeddedByIndex(index: Int): IsPropertyDefinitionWrapper<*, *, *, *>? = dataModel.properties.getDefinition(index)

    override fun validateWithRef(previousValue: DO?, newValue: DO?, refGetter: () -> IsPropertyReference<DO, IsPropertyDefinition<DO>>?) {
        super<IsValueDefinition>.validateWithRef(previousValue, newValue, refGetter)
        if (newValue != null) {
            this.dataModel.validate(
                refGetter = refGetter,
                dataObject = newValue
            )
        }
    }

    override fun writeJsonValue(value: DO, writer: IsJsonLikeWriter, context: CXI?) = this.dataModel.writeJson(
        value,
        writer,
        this.dataModel.transformContext(context)
    )

    override fun readJson(reader: IsJsonLikeReader, context: CXI?) =
        this.dataModel.readJson(reader, this.dataModel.transformContext(context)).toDataObject()

    override fun calculateTransportByteLength(value: DO, cacher: WriteCacheWriter, context: CXI?): Int {
        var totalByteLength = 0
        val newContext = if (this.dataModel is ContextualDataModel<*, *, *, *>) {
            this.dataModel.transformContext(context)?.apply {
                cacher.addContextToCache(this)
            }
        } else {
            @Suppress("UNCHECKED_CAST")
            context as CX?
        }
        totalByteLength += this.dataModel.calculateProtoBufLength(value, cacher, newContext)
        return totalByteLength
    }

    override fun writeTransportBytes(value: DO, cacheGetter: WriteCacheReader, writer: (byte: Byte) -> Unit, context: CXI?) {
        @Suppress("UNCHECKED_CAST")
        val newContext = if (this.dataModel is ContextualDataModel<*, *, *, *>) {
            cacheGetter.nextContextFromCache() as CX?
        } else {
            context as CX?
        }

        this.dataModel.writeProtoBuf(value, cacheGetter, writer, newContext)
    }

    override fun readTransportBytes(length: Int, reader: () -> Byte, context: CXI?) =
        this.dataModel.readProtoBuf(length, reader, this.dataModel.transformContext(context)).toDataObject()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmbeddedObjectDefinition<*, *, *, *, *>) return false

        if (indexed != other.indexed) return false
        if (required != other.required) return false
        if (final != other.final) return false
        if (internalDataModel.value != other.internalDataModel.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = indexed.hashCode()
        result = 31 * result + required.hashCode()
        result = 31 * result + final.hashCode()
        result = 31 * result + internalDataModel.value.hashCode()
        return result
    }

    object Model : ContextualDataModel<EmbeddedObjectDefinition<*, *, *, *, *>, PropertyDefinitions<EmbeddedObjectDefinition<*, *, *, *, *>>, DataModelContext, ModelContext>(
        contextTransformer = { ModelContext(it) },
        properties = object : PropertyDefinitions<EmbeddedObjectDefinition<*, *, *, *, *>>() {
            init {
                IsPropertyDefinition.addIndexed(this, EmbeddedObjectDefinition<*, *, *, *, *>::indexed)
                IsPropertyDefinition.addRequired(this, EmbeddedObjectDefinition<*, *, *, *, *>::required)
                IsPropertyDefinition.addFinal(this, EmbeddedObjectDefinition<*, *, *, *, *>::final)
                add(3, "dataModel",
                    ContextualModelReferenceDefinition(
                        contextTransformer = {context: ModelContext? ->
                            context?.dataModelContext
                        },
                        contextualResolver = { context: DataModelContext?, name ->
                            context?.let{
                                it.dataModels[name]
                                        ?: throw DefNotFoundException("DataModel of name $name not found on dataModels")
                            } ?: throw ContextNotFoundException()
                        }
                    ),
                    getter = { it: EmbeddedObjectDefinition<*, *, *, *, *> ->
                        { it.dataModel as DataModel<*, *> }
                    },
                    toSerializable = { value: (() -> DataModel<*, *>)?, _ ->
                        value?.invoke()?.let{ model ->
                            DataModelReference(model.name, value)
                        }
                    },
                    fromSerializable = { it: IsDataModelReference<DataModel<*, *>>? -> it?.get },
                    capturer = { context: ModelContext, dataModel: IsDataModelReference<DataModel<*, *>> ->
                        context.dataModelContext?.let {
                            if (!it.dataModels.containsKey(dataModel.name)) {
                                it.dataModels[dataModel.name] = dataModel.get
                            }
                        } ?: throw ContextNotFoundException()

                        @Suppress("UNCHECKED_CAST")
                        context.model = dataModel.get as () -> AbstractDataModel<Any, PropertyDefinitions<Any>, IsPropertyContext, IsPropertyContext>
                    }
                )

                add(4, "default",
                    ContextualEmbeddedObjectDefinition(
                        contextualResolver = { context: ModelContext? ->
                            context?.model?.invoke() ?: throw ContextNotFoundException()
                        }
                    ),
                    EmbeddedObjectDefinition<*, *, *, *, *>::default
                )
            }
        }
    ) {
        override fun invoke(map: ValueMap<EmbeddedObjectDefinition<*, *, *, *, *>, PropertyDefinitions<EmbeddedObjectDefinition<*, *, *, *, *>>>) = EmbeddedObjectDefinition(
            indexed = map(0),
            required = map(1),
            final = map(2),
            dataModel = map<() -> DataModel<Any, PropertyDefinitions<Any>>>(3),
            default = map(4)
        )
    }
}