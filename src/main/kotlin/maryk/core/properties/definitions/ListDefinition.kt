package maryk.core.properties.definitions

import maryk.core.objects.DataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.references.ListItemReference
import maryk.core.properties.references.ListReference
import maryk.core.properties.types.TypedValue
import maryk.core.properties.types.numeric.UInt32
import maryk.core.properties.types.numeric.toUInt32

data class ListDefinition<T: Any, CX: IsPropertyContext>(
        override val indexed: Boolean = false,
        override val searchable: Boolean = true,
        override val required: Boolean = true,
        override val final: Boolean = false,
        override val minSize: Int? = null,
        override val maxSize: Int? = null,
        override val valueDefinition: IsValueDefinition<T, CX>
) :
        IsCollectionDefinition<T, List<T>, CX, IsValueDefinition<T, CX>>,
        IsTransportablePropertyDefinitionType
{
    override val propertyDefinitionType = PropertyDefinitionType.List

    init {
        assert(valueDefinition.required, { "Definition for value should have required=true on List" })
    }

    override fun newMutableCollection(context: CX?) = mutableListOf<T>()

    /** Get a reference to a specific list item by index
     * @param index to get list item reference for
     * @param parentList (optional) factory to create parent ref
     */
    fun getItemRef(index: Int, parentList: ListReference<T, CX>?)
            = ListItemReference(index, this, parentList)

    override fun validateCollectionForExceptions(refGetter: () -> IsPropertyReference<List<T>, IsPropertyDefinition<List<T>>>?, newValue: List<T>, validator: (item: T, parentRefFactory: () -> IsPropertyReference<T, IsPropertyDefinition<T>>?) -> Any) {
        newValue.forEachIndexed { index, item ->
            validator(item) {
                @Suppress("UNCHECKED_CAST")
                this.getItemRef(index, refGetter() as ListReference<T, CX>?)
            }
        }
    }

    companion object : DataModel<ListDefinition<*, *>, PropertyDefinitions<ListDefinition<*, *>>>(
            properties = object : PropertyDefinitions<ListDefinition<*, *>>() {
                init {
                    IsPropertyDefinition.addIndexed(this, ListDefinition<*, *>::indexed)
                    IsPropertyDefinition.addSearchable(this, ListDefinition<*, *>::searchable)
                    IsPropertyDefinition.addRequired(this, ListDefinition<*, *>::required)
                    IsPropertyDefinition.addFinal(this, ListDefinition<*, *>::final)
                    HasSizeDefinition.addMinSize(4, this) { it.minSize?.toUInt32() }
                    HasSizeDefinition.addMaxSize(5, this) { it.maxSize?.toUInt32() }
                    add(6, "valueDefinition", MultiTypeDefinition(
                            definitionMap = mapOfPropertyDefSubModelDefinitions
                    )) {
                        val defType = it.valueDefinition as IsTransportablePropertyDefinitionType
                        TypedValue(defType.propertyDefinitionType.index, it.valueDefinition)
                    }
                }
            }
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun invoke(map: Map<Int, *>) = ListDefinition(
                indexed = map[0] as Boolean,
                searchable = map[1] as Boolean,
                required = map[2] as Boolean,
                final = map[3] as Boolean,
                minSize = (map[4] as UInt32?)?.toInt(),
                maxSize = (map[5] as UInt32?)?.toInt(),
                valueDefinition = (map[6] as TypedValue<IsValueDefinition<*, *>>).value
        )
    }
}
