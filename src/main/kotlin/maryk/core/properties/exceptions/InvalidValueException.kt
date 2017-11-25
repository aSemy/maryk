package maryk.core.properties.exceptions

import maryk.core.objects.Def
import maryk.core.objects.QueryDataModel
import maryk.core.properties.references.IsPropertyReference

/**
 * Exception for when properties are set with invalid input.
 */
data class InvalidValueException(
        val reference: IsPropertyReference<*, *>,
        val value: String
) : ValidationException(
        reference = reference,
        reason = "has invalid value: «$value»"
) {
    override val validationExceptionType = ValidationExceptionType.INVALID_VALUE

    companion object: QueryDataModel<InvalidValueException>(
            construct = {
                InvalidValueException(
                        reference = it[0] as IsPropertyReference<*, *>,
                        value = it[1] as String
                )
            },
            definitions = listOf(
                    Def(Properties.reference, InvalidValueException::reference),
                    Def(Properties.value, InvalidValueException::value)
            )
    )
}
