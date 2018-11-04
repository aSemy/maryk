package maryk.core.properties.definitions

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.models.ContextualDataModel
import maryk.core.objects.SimpleObjectValues
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.contextual.ContextTransformerDefinition
import maryk.core.properties.definitions.contextual.ContextualCollectionDefinition
import maryk.core.properties.references.AnyPropertyReference
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.references.SetItemReference
import maryk.core.properties.references.SetReference
import maryk.core.properties.types.TypedValue
import maryk.core.query.ContainsDefinitionsContext
import maryk.core.query.DefinitionsContext

/** Definition for Set property */
data class SetDefinition<T: Any, CX: IsPropertyContext>(
    override val indexed: Boolean = false,
    override val required: Boolean = true,
    override val final: Boolean = false,
    override val minSize: Int? = null,
    override val maxSize: Int? = null,
    override val valueDefinition: IsValueDefinition<T, CX>,
    override val default: Set<T>? = null
) : IsCollectionDefinition<T, Set<T>, CX, IsValueDefinition<T, CX>>, HasDefaultValueDefinition<Set<T>> {
    override val propertyDefinitionType = PropertyDefinitionType.Set

    init {
        require(valueDefinition.required) { "Definition for value should have required=true on set" }
    }

    override fun newMutableCollection(context: CX?) = mutableSetOf<T>()

    /** Get a reference by [value] to a specific set item of set of [setReference] */
    fun getItemRef(value: T, setReference: SetReference<T, CX>?) =
        SetItemReference(value, this, setReference)

    override fun getItemPropertyRefCreator(
        index: Int,
        item: T
    ) = { parentRef: AnyPropertyReference? ->
        @Suppress("UNCHECKED_CAST")
        this.getItemRef(item, parentRef as SetReference<T, CX>?) as IsPropertyReference<Any, *, *>
    }

    override fun validateCollectionForExceptions(
        refGetter: () -> IsPropertyReference<Set<T>,IsPropertyDefinition<Set<T>>, *>?,
        newValue: Set<T>,
        validator: (item: T, parentRefFactory: () -> IsPropertyReference<T, IsPropertyDefinition<T>, *>?) -> Any
    ) {
        for (it in newValue) {
            validator(it) {
                @Suppress("UNCHECKED_CAST")
                this.getItemRef(it, refGetter() as SetReference<T, CX>?)
            }
        }
    }

    object Model : ContextualDataModel<SetDefinition<*, *>, ObjectPropertyDefinitions<SetDefinition<*, *>>, ContainsDefinitionsContext, SetDefinitionContext>(
        contextTransformer = { it: ContainsDefinitionsContext? -> SetDefinitionContext(it) },
        properties = object : ObjectPropertyDefinitions<SetDefinition<*, *>>() {
            init {
                IsPropertyDefinition.addIndexed(this, SetDefinition<*, *>::indexed)
                IsPropertyDefinition.addRequired(this, SetDefinition<*, *>::required)
                IsPropertyDefinition.addFinal(this, SetDefinition<*, *>::final)
                HasSizeDefinition.addMinSize(4, this, SetDefinition<*, *>::minSize)
                HasSizeDefinition.addMaxSize(5, this, SetDefinition<*, *>::maxSize)
                add(6, "valueDefinition",
                    ContextTransformerDefinition(
                        contextTransformer = { it?.definitionsContext },
                        definition = MultiTypeDefinition(
                            typeEnum = PropertyDefinitionType,
                            definitionMap = mapOfPropertyDefEmbeddedObjectDefinitions
                        )
                    ),
                    getter = SetDefinition<*, *>::valueDefinition,
                    toSerializable = { value, _ ->
                        val defType = value!! as IsTransportablePropertyDefinitionType<*>
                        TypedValue(defType.propertyDefinitionType, value)
                    },
                    fromSerializable = {
                        @Suppress("UNCHECKED_CAST")
                        it?.value as IsValueDefinition<Any, DefinitionsContext>?
                    },
                    capturer = { context: SetDefinitionContext, value ->
                        @Suppress("UNCHECKED_CAST")
                        context.valueDefinion = value.value as IsValueDefinition<Any, ContainsDefinitionsContext>
                    }
                )
                @Suppress("UNCHECKED_CAST")
                add(7, "default", ContextualCollectionDefinition(
                    required = false,
                    contextualResolver = { context: SetDefinitionContext? ->
                        context?.setDefinition?.let {
                            it as IsByteTransportableCollection<Any, Collection<Any>, SetDefinitionContext>
                        } ?: throw ContextNotFoundException()
                    }
                ), SetDefinition<*, *>::default)
            }
        }
    ) {
        override fun invoke(map: SimpleObjectValues<SetDefinition<*, *>>) = SetDefinition(
            indexed = map(1),
            required = map(2),
            final = map(3),
            minSize = map(4),
            maxSize = map(5),
            valueDefinition = map<IsValueDefinition<*, *>>(6),
            default = map(7)
        )
    }
}

class SetDefinitionContext(
    val definitionsContext: ContainsDefinitionsContext?
) : IsPropertyContext {
    var valueDefinion: IsValueDefinition<Any, ContainsDefinitionsContext>? = null

    private var _setDefinition: Lazy<SetDefinition<Any, ContainsDefinitionsContext>> = lazy {
        SetDefinition(valueDefinition = this.valueDefinion ?: throw ContextNotFoundException())
    }

    val setDefinition: SetDefinition<Any, ContainsDefinitionsContext> get() = this._setDefinition.value
}