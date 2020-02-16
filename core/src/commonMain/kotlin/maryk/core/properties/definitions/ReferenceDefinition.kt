package maryk.core.properties.definitions

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.exceptions.DefNotFoundException
import maryk.core.models.DefinitionDataModel
import maryk.core.models.IsRootDataModel
import maryk.core.models.IsTypedRootDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.IsPropertyDefinitions
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.PropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextualModelReferenceDefinition
import maryk.core.properties.definitions.contextual.DataModelReference
import maryk.core.properties.definitions.wrapper.DefinitionWrapperDelegateLoader
import maryk.core.properties.definitions.wrapper.FixedBytesDefinitionWrapper
import maryk.core.properties.definitions.wrapper.ObjectDefinitionWrapperDelegateLoader
import maryk.core.properties.definitions.wrapper.ReferenceDefinitionWrapper
import maryk.core.properties.definitions.wrapper.contextual
import maryk.core.properties.types.Bytes
import maryk.core.properties.types.Key
import maryk.core.protobuf.WireType.LENGTH_DELIMITED
import maryk.core.query.ContainsDefinitionsContext
import maryk.core.values.SimpleObjectValues
import maryk.lib.exceptions.ParseException
import maryk.lib.safeLazy

/** Definition for a reference to another DataObject*/
class ReferenceDefinition<DM : IsRootDataModel<*>>(
    override val required: Boolean = true,
    override val final: Boolean = false,
    override val unique: Boolean = false,
    override val minValue: Key<DM>? = null,
    override val maxValue: Key<DM>? = null,
    override val default: Key<DM>? = null,
    dataModel: Unit.() -> DM
) :
    IsReferenceDefinition<DM, PropertyDefinitions, IsPropertyContext> {
    override val propertyDefinitionType = PropertyDefinitionType.Reference
    override val wireType = LENGTH_DELIMITED
    override val byteSize get() = dataModel.keyByteSize

    private val internalDataModel = safeLazy(dataModel)
    override val dataModel: DM get() = internalDataModel.value

    override fun calculateStorageByteLength(value: Key<DM>) = this.byteSize

    override fun writeStorageBytes(value: Key<DM>, writer: (byte: Byte) -> Unit) = value.writeBytes(writer)

    @Suppress("UNCHECKED_CAST")
    override fun readStorageBytes(length: Int, reader: () -> Byte) = dataModel.key(reader) as Key<DM>

    override fun calculateTransportByteLength(value: Key<DM>) = this.byteSize

    override fun fromString(string: String) = try {
        @Suppress("UNCHECKED_CAST")
        dataModel.key(string) as Key<DM>
    } catch (e: Throwable) {
        throw ParseException(string, e)
    }

    override fun fromNativeType(value: Any) =
        if (value is ByteArray && value.size == this.byteSize) {
            @Suppress("UNCHECKED_CAST")
            dataModel.key(value) as Key<DM>
        } else {
            null
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReferenceDefinition<*>) return false

        if (required != other.required) return false
        if (final != other.final) return false
        if (unique != other.unique) return false
        if (minValue != other.minValue) return false
        if (maxValue != other.maxValue) return false
        if (default != other.default) return false
        if (dataModel.name != other.dataModel.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = required.hashCode()
        result = 31 * result + final.hashCode()
        result = 31 * result + unique.hashCode()
        result = 31 * result + (minValue?.hashCode() ?: 0)
        result = 31 * result + (maxValue?.hashCode() ?: 0)
        result = 31 * result + (default?.hashCode() ?: 0)
        result = 31 * result + dataModel.name.hashCode()
        return result
    }

    @Suppress("unused")
    object Model : DefinitionDataModel<ReferenceDefinition<*>>(
        properties = object : ObjectPropertyDefinitions<ReferenceDefinition<*>>() {
            val required by boolean(1u, ReferenceDefinition<*>::required, default = true)
            val final by boolean(2u, ReferenceDefinition<*>::final, default = false)
            val unique by boolean(3u, ReferenceDefinition<*>::unique, default = false)
            val minValue by flexBytes(4u, ReferenceDefinition<*>::minValue)
            val maxValue by flexBytes(5u, ReferenceDefinition<*>::maxValue)
            val default by flexBytes(6u, ReferenceDefinition<*>::default)
            val dataModel by contextual(
                index = 7u,
                definition = ContextualModelReferenceDefinition(
                    contextualResolver = { context: ContainsDefinitionsContext?, name ->
                        context?.let {
                            @Suppress("UNCHECKED_CAST")
                            it.dataModels[name] as (Unit.() -> IsRootDataModel<*>)?
                                ?: throw DefNotFoundException("ObjectDataModel of name $name not found on dataModels")
                        } ?: throw ContextNotFoundException()
                    }
                ),
                getter = {
                    { it.dataModel }
                },
                toSerializable = { value: (Unit.() -> IsRootDataModel<*>)?, _ ->
                    value?.invoke(Unit)?.let { model: IsRootDataModel<*> ->
                        DataModelReference(model.name, value)
                    }
                },
                fromSerializable = {
                    it?.get
                },
                capturer = { context, dataModel ->
                    if (!context.dataModels.containsKey(dataModel.name)) {
                        context.dataModels[dataModel.name] = dataModel.get
                    }
                }
            )
        }
    ) {
        override fun invoke(values: SimpleObjectValues<ReferenceDefinition<*>>) = ReferenceDefinition(
            required = values(1u),
            final = values(2u),
            unique = values(3u),
            minValue = values<Bytes?>(4u)?.let {
                Key<IsTypedRootDataModel<IsRootDataModel<IsPropertyDefinitions>, IsPropertyDefinitions>>(
                    it.bytes
                )
            },
            maxValue = values<Bytes?>(5u)?.let {
                Key<IsTypedRootDataModel<IsRootDataModel<IsPropertyDefinitions>, IsPropertyDefinitions>>(
                    it.bytes
                )
            },
            default = values<Bytes?>(6u)?.let {
                Key<IsTypedRootDataModel<IsRootDataModel<IsPropertyDefinitions>, IsPropertyDefinitions>>(
                    it.bytes
                )
            },
            dataModel = values(7u)
        )
    }
}

fun <DM: IsRootDataModel<*>, TO: Any, DO: Any> ObjectPropertyDefinitions<DO>.reference(
    index: UInt,
    getter: (DO) -> TO?,
    name: String? = null,
    required: Boolean = true,
    final: Boolean = false,
    unique: Boolean = false,
    minValue: Key<DM>? = null,
    maxValue: Key<DM>? = null,
    default: Key<DM>? = null,
    dataModel: Unit.() -> DM,
    alternativeNames: Set<String>? = null
): ObjectDefinitionWrapperDelegateLoader<FixedBytesDefinitionWrapper<Key<DM>, TO, IsPropertyContext, ReferenceDefinition<DM>, DO>, DO> =
    reference(index, getter, name, required, final,  unique, minValue, maxValue, default, dataModel, alternativeNames, toSerializable = null)

fun <DM: IsRootDataModel<P>, P: PropertyDefinitions> PropertyDefinitions.reference(
    index: UInt,
    name: String? = null,
    required: Boolean = true,
    final: Boolean = false,
    unique: Boolean = false,
    minValue: Key<DM>? = null,
    maxValue: Key<DM>? = null,
    default: Key<DM>? = null,
    dataModel: Unit.() -> DM,
    alternativeNames: Set<String>? = null
) = DefinitionWrapperDelegateLoader(this) { propName ->
    @Suppress("UNCHECKED_CAST")
    ReferenceDefinitionWrapper<Key<DM>, DM, P, IsReferenceDefinition<DM, P, IsPropertyContext>, Any>(
        index,
        name ?: propName,
        ReferenceDefinition(required, final, unique, minValue, maxValue, default, dataModel) as IsReferenceDefinition<DM, P, IsPropertyContext>,
        alternativeNames
    )
}

fun <DM: IsRootDataModel<*>, TO: Any, DO: Any, CX: IsPropertyContext> ObjectPropertyDefinitions<DO>.reference(
    index: UInt,
    getter: (DO) -> TO?,
    name: String? = null,
    required: Boolean = true,
    final: Boolean = false,
    unique: Boolean = false,
    minValue: Key<DM>? = null,
    maxValue: Key<DM>? = null,
    default: Key<DM>? = null,
    dataModel: Unit.() -> DM,
    alternativeNames: Set<String>? = null,
    toSerializable: (Unit.(TO?, CX?) -> Key<DM>?)? = null,
    fromSerializable: (Unit.(Key<DM>?) -> TO?)? = null,
    shouldSerialize: (Unit.(Any) -> Boolean)? = null,
    capturer: (Unit.(CX, Key<DM>) -> Unit)? = null
) = ObjectDefinitionWrapperDelegateLoader(this) { propName ->
    FixedBytesDefinitionWrapper(
        index,
        name ?: propName,
        ReferenceDefinition(required, final, unique, minValue, maxValue, default, dataModel),
        alternativeNames,
        getter = getter,
        capturer = capturer,
        toSerializable = toSerializable,
        fromSerializable = fromSerializable,
        shouldSerialize = shouldSerialize
    )
}
