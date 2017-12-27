package maryk.core.properties.definitions

import maryk.core.extensions.bytes.writeBytes
import maryk.core.json.JsonReader
import maryk.core.json.JsonWriter
import maryk.core.objects.DefinitionDataModel
import maryk.core.objects.ValueDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.contextual.ContextCaptureDefinition
import maryk.core.properties.definitions.contextual.ContextualModelReferenceDefinition
import maryk.core.properties.definitions.wrapper.IsPropertyDefinitionWrapper
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.types.Bytes
import maryk.core.properties.types.ValueDataObject
import maryk.core.protobuf.WireType
import maryk.core.query.DataModelContext

/** Definition for value model properties
 * @param dataModel definition of the DataObject
 * @param <DM>  Type of model for this definition
 * @param <DO> DataModel which is contained within SubModel
 */
data class ValueModelDefinition<DO: ValueDataObject, out DM : ValueDataModel<DO, PropertyDefinitions<DO>>>(
        override val indexed: Boolean = false,
        override val searchable: Boolean = true,
        override val required: Boolean = true,
        override val final: Boolean = false,
        override val unique: Boolean = false,
        override val minValue: DO? = null,
        override val maxValue: DO? = null,
        val dataModel: DM
) :
        IsComparableDefinition<DO, IsPropertyContext>,
        IsSerializableFixedBytesEncodable<DO, IsPropertyContext>,
        IsTransportablePropertyDefinitionType
{
    override val propertyDefinitionType = PropertyDefinitionType.ValueModel
    override val wireType = WireType.LENGTH_DELIMITED
    override val byteSize = dataModel.byteSize

    override fun calculateStorageByteLength(value: DO) = this.byteSize

    override fun writeStorageBytes(value: DO, writer: (byte: Byte) -> Unit) = value._bytes.writeBytes(writer)

    override fun readStorageBytes(length: Int, reader: () -> Byte)
            = this.dataModel.readFromBytes(reader)

    override fun calculateTransportByteLength(value: DO) = this.dataModel.byteSize

    override fun asString(value: DO) = value.toBase64()

    override fun fromString(string: String) = this.dataModel.fromString(string)

    override fun getEmbeddedByName(name: String): IsPropertyDefinitionWrapper<*, *, *>? = dataModel.properties.getDefinition(name)

    override fun getEmbeddedByIndex(index: Int): IsPropertyDefinitionWrapper<*, *, *>? = dataModel.properties.getDefinition(index)

    override fun validateWithRef(previousValue: DO?, newValue: DO?, refGetter: () -> IsPropertyReference<DO, IsPropertyDefinition<DO>>?) {
        super<IsComparableDefinition>.validateWithRef(previousValue, newValue, refGetter)
        if (newValue != null) {
            this.dataModel.validate(
                    refGetter = refGetter,
                    dataObject = newValue
            )
        }
    }

    /** Writes a value to Json
     * @param value: value to write
     * @param writer: to write json to
     */
    override fun writeJsonValue(value: DO, writer: JsonWriter, context: IsPropertyContext?) = dataModel.writeJson(value, writer, context)

    override fun readJson(reader: JsonReader, context: IsPropertyContext?): DO = dataModel.readJsonToObject(reader, context)


    object Model : DefinitionDataModel<ValueModelDefinition<*, *>>(
            properties = object : PropertyDefinitions<ValueModelDefinition<*, *>>() {
                init {
                    IsPropertyDefinition.addIndexed(this, ValueModelDefinition<*, *>::indexed)
                    IsPropertyDefinition.addSearchable(this, ValueModelDefinition<*, *>::searchable)
                    IsPropertyDefinition.addRequired(this, ValueModelDefinition<*, *>::required)
                    IsPropertyDefinition.addFinal(this, ValueModelDefinition<*, *>::final)
                    IsComparableDefinition.addUnique(this, ValueModelDefinition<*, *>::unique)
                    add(5, "minValue", FlexBytesDefinition()) {
                        it.minValue?._bytes?.let { Bytes(it) }
                    }
                    add(6, "maxValue", FlexBytesDefinition()) {
                        it.maxValue?._bytes?.let { Bytes(it) }
                    }
                    add(7, "dataModel", ContextCaptureDefinition(
                            definition = ContextualModelReferenceDefinition<DataModelContext>(
                                    contextualResolver = { context, name ->
                                        context!!.dataModels[name]!!
                                    }
                            ),
                            capturer = { context, dataModel ->
                                if (!context!!.dataModels.containsKey(dataModel.name)) {
                                    context.dataModels[dataModel.name] = dataModel
                                }
                            }
                    )) {
                        it.dataModel
                    }
                }
            }
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun invoke(map: Map<Int, *>) = ValueModelDefinition(
                indexed = map[0] as Boolean,
                searchable = map[1] as Boolean,
                required = map[2] as Boolean,
                final = map[3] as Boolean,
                unique = map[4] as Boolean,
                minValue = (map[5] as Bytes?)?.let { ValueDataObject(it.bytes) },
                maxValue = (map[6] as Bytes?)?.let { ValueDataObject(it.bytes) },
                dataModel = map[7] as ValueDataModel<ValueDataObject, PropertyDefinitions<ValueDataObject>>

        )
    }
}