package maryk.core.query.responses.statuses

import maryk.core.objects.QueryDataModel
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.types.Key

/** Given object already exists
 * @param key of already existing object
 */
data class AlreadyExists<DO: Any>(
        val key: Key<DO>
) : IsAddResponseStatus<DO> {
    override val statusType = StatusType.ALREADY_EXISTS

    companion object: QueryDataModel<AlreadyExists<*>>(
            properties = object : PropertyDefinitions<AlreadyExists<*>>() {
                init {
                    IsResponseStatus.addKey(this, AlreadyExists<*>::key)
                }
            }
    ) {
        override fun invoke(map: Map<Int, *>) = AlreadyExists(
                key = map[0] as Key<Any>
        )
    }
}