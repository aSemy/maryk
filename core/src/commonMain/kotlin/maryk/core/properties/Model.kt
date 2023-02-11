package maryk.core.properties

import maryk.core.models.PropertyBaseDataModel
import maryk.core.properties.definitions.IsPropertyDefinition
import maryk.core.properties.references.AnyOutPropertyReference
import maryk.core.properties.references.IsPropertyReference
import maryk.core.values.MutableValueItems
import maryk.core.values.ValueItem

open class Model<P: PropertyDefinitions>(
    reservedIndices: List<UInt>? = null,
    reservedNames: List<String>? = null,
) : PropertyDefinitions(){
    @Suppress("UNCHECKED_CAST")
    val Model = PropertyBaseDataModel(
        reservedIndices = reservedIndices,
        reservedNames = reservedNames,
        properties = this,
    ) as PropertyBaseDataModel<P>

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any, R : IsPropertyReference<T, IsPropertyDefinition<T>, *>> invoke(
        parent: AnyOutPropertyReference? = null,
        referenceGetter: P.() -> (AnyOutPropertyReference?) -> R
    ) = referenceGetter(this as P)(parent)

    operator fun <R> invoke(block: P.() -> R): R {
        @Suppress("UNCHECKED_CAST")
        return block(this as P)
    }

    fun create (vararg pairs: ValueItem?) = Model.values {
        MutableValueItems().also { items ->
            for (it in pairs) {
                if (it != null) items += it
            }
        }
    }
}
