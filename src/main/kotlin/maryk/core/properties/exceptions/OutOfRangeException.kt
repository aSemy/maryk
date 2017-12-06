package maryk.core.properties.exceptions

import maryk.core.objects.QueryDataModel
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.StringDefinition
import maryk.core.properties.references.IsPropertyReference

/** Exception for when a value was out of range.
 *
 * This can be both of value or for the size of value containers like List or
 * Map
 *
 * @param reference   of property
 * @param value which was invalid
 * @param min   minimum of range
 * @param max   maximum of range
 */
data class OutOfRangeException(
        val reference: IsPropertyReference<*, *>?,
        val value: String,
        val min: String?,
        val max: String?
) : ValidationException(
        reference = reference,
        reason = "is out of range: «$value» with range [$min,$max]"
) {
    override val validationExceptionType = ValidationExceptionType.OUT_OF_RANGE

    companion object: QueryDataModel<OutOfRangeException>(
            properties = object: PropertyDefinitions<OutOfRangeException>() {
                init {
                    ValidationException.addReference(this, OutOfRangeException::reference)
                    ValidationException.addValue(this, OutOfRangeException::value)
                    add(2, "min", StringDefinition(), OutOfRangeException::min)
                    add(3, "max", StringDefinition(), OutOfRangeException::max)
                }
            }
    ) {
        override fun invoke(map: Map<Int, *>) = OutOfRangeException(
                reference = map[0] as IsPropertyReference<*, *>,
                value = map[1] as String,
                min = map[2] as String?,
                max = map[3] as String?
        )
    }
}
