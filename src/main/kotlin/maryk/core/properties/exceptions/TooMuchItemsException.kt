package maryk.core.properties.exceptions

import maryk.core.objects.QueryDataModel
import maryk.core.properties.definitions.NumberDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.references.IsPropertyReference
import maryk.core.properties.types.numeric.SInt32

/**
 * Exception for when a map or collection property referred by [reference] of [size] has
 * too much items compared to [maxSize]
 */
data class TooMuchItemsException(
    val reference: IsPropertyReference<*, *>?,
    val size: Int,
    val maxSize: Int
) : ValidationException(
    reference = reference,
    reason = "has too much items: $size over max of $maxSize items"
) {
    override val validationExceptionType = ValidationExceptionType.TOO_MUCH_ITEMS

    internal companion object: QueryDataModel<TooMuchItemsException>(
        properties = object : PropertyDefinitions<TooMuchItemsException>() {
            init {
                ValidationException.addReference(this, TooMuchItemsException::reference)
                add(1, "size", NumberDefinition(type = SInt32), TooMuchItemsException::size)
                add(2, "maxSize", NumberDefinition(type = SInt32), TooMuchItemsException::maxSize)
            }
        }
    ) {
        override fun invoke(map: Map<Int, *>) = TooMuchItemsException(
            reference = map[0] as IsPropertyReference<*, *>,
            size = map[1] as Int,
            maxSize = map[2] as Int
        )
    }
}