package maryk.core.query.responses

import maryk.core.models.SimpleQueryDataModel
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EnumDefinition
import maryk.core.properties.definitions.StringDefinition
import maryk.core.properties.enum.IndexedEnumComparable
import maryk.core.properties.enum.IndexedEnumDefinition
import maryk.core.properties.enum.IsCoreEnum
import maryk.core.values.SimpleObjectValues

/** Types of failures */
enum class FailType(
    override val index: UInt,
    override val alternativeNames: Set<String>? = null
) : IndexedEnumComparable<FailType>, IsCoreEnum {
    CONNECTION(1u), // Problems with Connection at the server
    STORE_STATE(2u), // Problems with the state of the store
    REQUEST(3u), // Problems with the request content
    AUTH(4u); // Problems with the Authentication

    companion object : IndexedEnumDefinition<FailType>("FailType", FailType::values)
}

/** Response with [message] and [failType] for failed actions. */
data class FailedActionResponse(
    val message: String,
    val failType: FailType
) : IsResponse {
    internal companion object : SimpleQueryDataModel<FailedActionResponse>(
        properties = object : ObjectPropertyDefinitions<FailedActionResponse>() {
            init {
                add(1u, "message", StringDefinition(), FailedActionResponse::message)
                add(2u, "failType", EnumDefinition(enum = FailType), FailedActionResponse::failType)
            }
        }
    ) {
        override fun invoke(values: SimpleObjectValues<FailedActionResponse>) = FailedActionResponse(
            message = values(1u),
            failType = values(2u)
        )
    }
}
