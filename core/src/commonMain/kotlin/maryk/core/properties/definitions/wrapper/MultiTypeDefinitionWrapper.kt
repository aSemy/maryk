package maryk.core.properties.definitions.wrapper

import maryk.core.models.IsValuesDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.PropertyDefinitions
import maryk.core.properties.definitions.EmbeddedValuesDefinition
import maryk.core.properties.definitions.IsMultiTypeDefinition
import maryk.core.properties.enum.TypeEnum
import maryk.core.properties.graph.PropRefGraphType.PropRef
import maryk.core.properties.references.AnyOutPropertyReference
import maryk.core.properties.references.AnyPropertyReference
import maryk.core.properties.references.CanHaveComplexChildReference
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.references.MultiTypePropertyReference
import maryk.core.properties.references.TypeReference
import maryk.core.properties.references.TypedValueReference
import maryk.core.properties.types.TypedValue
import maryk.core.values.Values

/**
 * Contains a Multi Type property [definition] containing type [E]
 * It contains an [index] and [name] to which it is referred inside DataModel and a [getter]
 * function to retrieve value on dataObject of [DO] in context [CX]
 */
data class MultiTypeDefinitionWrapper<E : TypeEnum<T>, T: Any, TO : Any, CX : IsPropertyContext, DO : Any> internal constructor(
    override val index: UInt,
    override val name: String,
    override val definition: IsMultiTypeDefinition<E, CX>,
    override val getter: (DO) -> TO? = { null },
    override val capturer: ((CX, TypedValue<E, Any>) -> Unit)? = null,
    override val toSerializable: ((TO?, CX?) -> TypedValue<E, Any>?)? = null,
    override val fromSerializable: ((TypedValue<E, Any>?) -> TO?)? = null,
    override val shouldSerialize: ((Any) -> Boolean)? = null
) :
    AbstractPropertyDefinitionWrapper(index, name),
    IsMultiTypeDefinition<E, CX> by definition,
    IsPropertyDefinitionWrapper<TypedValue<E, Any>, TO, CX, DO> {
    override val graphType = PropRef

    override fun ref(parentRef: AnyPropertyReference?) =
        MultiTypePropertyReference(this, parentRef)

    /** For quick notation to get a [type] reference */
    infix fun refAtType(type: E): (AnyOutPropertyReference?) -> TypedValueReference<E, CX> {
        return { this.typedValueRef(type, this.ref(it)) }
    }

    /** For quick notation to get an any type reference */
    fun refToType(): (AnyOutPropertyReference?) -> TypeReference<E, CX> {
        return {
            @Suppress("UNCHECKED_CAST")
            this.typeRef(it as CanHaveComplexChildReference<TypedValue<E, *>, IsMultiTypeDefinition<E, *>, *, *>?)
        }
    }

    override fun typeRef(parentReference: CanHaveComplexChildReference<TypedValue<E, *>, IsMultiTypeDefinition<E, *>, *, *>?): TypeReference<E, CX> {
        return this.definition.typeRef(this.ref(parentReference))
    }

    /** Specific extension to support fetching deeper references with [type] */
    fun <P : PropertyDefinitions, T : Any, R : IsPropertyReference<T, IsPropertyDefinitionWrapper<T, *, *, *>, *>> withType(
        type: E,
        @Suppress("UNUSED_PARAMETER") properties: P, // So it is not needed to pass in types
        referenceGetter: P.() -> (AnyOutPropertyReference?) -> R
    ): (AnyOutPropertyReference?) -> R =
        {
            val typeRef = this.typedValueRef(type, this.ref(it))
            @Suppress("UNCHECKED_CAST")
            (this.definitionMap[type] as EmbeddedValuesDefinition<IsValuesDataModel<P>, P>).dataModel(
                typeRef,
                referenceGetter
            )
        }

    /** Specific extension to support fetching deeper references with [type] */
    @Suppress("UNCHECKED_CAST")
    fun <P : PropertyDefinitions, T : Any, R : IsPropertyReference<T, IsPropertyDefinitionWrapper<T, *, *, *>, *>> withType(
        type: TypeEnum<Values<*, P>>,
        referenceGetter: P.() ->
            (AnyOutPropertyReference?) -> R
    ): (AnyOutPropertyReference?) -> R  =
        {
            val typeRef = this.typedValueRef(type as E, this.ref(it))
            (this.definitionMap[type] as EmbeddedValuesDefinition<IsValuesDataModel<P>, P>).dataModel(
                typeRef,
                referenceGetter
            )
        }
}
