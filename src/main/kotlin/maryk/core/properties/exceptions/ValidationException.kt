package maryk.core.properties.exceptions

import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.StringDefinition
import maryk.core.properties.definitions.SubModelDefinition
import maryk.core.properties.definitions.contextual.ContextCaptureDefinition
import maryk.core.properties.definitions.contextual.ContextualPropertyReferenceDefinition
import maryk.core.properties.definitions.wrapper.PropertyDefinitionWrapper
import maryk.core.properties.references.IsPropertyReference
import maryk.core.query.DataModelPropertyContext

/**
 * Validation Exception for properties
 */
abstract class ValidationException(
        newMessage: String
) : Throwable(
        newMessage
) {
    constructor(
            reason: String?,
            reference: IsPropertyReference<*,*>?
    ) : this(
            newMessage = "Property «${reference?.completeName}» $reason"
    )

    abstract val validationExceptionType: ValidationExceptionType

    internal object Properties : PropertyDefinitions<ValidationException>() {
        val reference = ContextCaptureDefinition(
                ContextualPropertyReferenceDefinition<DataModelPropertyContext>(
                        contextualResolver = { it!!.dataModel!! }
                )
        ) { context, value ->
            @Suppress("UNCHECKED_CAST")
            context!!.reference = value as IsPropertyReference<*, PropertyDefinitionWrapper<*, *, *, *>>
        }
    }

    companion object {
        fun <DM: ValidationException> addReference(definitions: PropertyDefinitions<DM>, getter: (DM) -> IsPropertyReference<*, *>?) {
            definitions.add(
                    0, "reference",
                    Properties.reference,
                    getter
            )
        }
        fun <DM: ValidationException> addValue(definitions: PropertyDefinitions<DM>, getter: (DM) -> String?) {
            definitions.add(
                    1, "value",
                    StringDefinition(
                            required = true
                    ),
                    getter
            )
        }
    }
}

internal val mapOfValidationExceptionDefinitions = mapOf(
        ValidationExceptionType.ALREADY_SET.index to SubModelDefinition(
                required = true,
                dataModel = AlreadySetException
        ),
        ValidationExceptionType.INVALID_SIZE.index to SubModelDefinition(
                required = true,
                dataModel = InvalidSizeException
        ),
        ValidationExceptionType.INVALID_VALUE.index to SubModelDefinition(
                required = true,
                dataModel = InvalidValueException
        ),
        ValidationExceptionType.OUT_OF_RANGE.index to SubModelDefinition(
                required = true,
                dataModel = OutOfRangeException
        ),
        ValidationExceptionType.REQUIRED.index to SubModelDefinition(
                required = true,
                dataModel = RequiredException
        ),
        ValidationExceptionType.TOO_LITTLE_ITEMS.index to SubModelDefinition(
                required = true,
                dataModel = TooLittleItemsException
        ),
        ValidationExceptionType.TOO_MUCH_ITEMS.index to SubModelDefinition(
                required = true,
                dataModel = TooMuchItemsException
        ),
        ValidationExceptionType.UMBRELLA.index to SubModelDefinition(
                required = true,
                dataModel = ValidationUmbrellaException
        )
)
