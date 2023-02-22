package maryk.core.query.responses.statuses

import maryk.core.models.IsRootDataModel
import maryk.core.models.SimpleQueryDataModel
import maryk.core.properties.IsValuesPropertyDefinitions
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.query.responses.statuses.StatusType.AUTH_FAIL
import maryk.core.values.SimpleObjectValues

/** Authorization fail for this action */
class AuthFail<DM : IsRootDataModel<*>> :
    IsAddResponseStatus<DM>,
    IsChangeResponseStatus<DM>,
    IsDeleteResponseStatus<DM> {
    override val statusType = AUTH_FAIL

    override fun equals(other: Any?) = other is AuthFail<*>
    override fun hashCode() = 0
    override fun toString() = "AuthFail"

    internal companion object : SimpleQueryDataModel<AuthFail<*>>(
        properties = object : ObjectPropertyDefinitions<AuthFail<*>>() {}
    ) {
        override fun invoke(values: SimpleObjectValues<AuthFail<*>>) =
            AuthFail<IsRootDataModel<IsValuesPropertyDefinitions>>()
    }
}
