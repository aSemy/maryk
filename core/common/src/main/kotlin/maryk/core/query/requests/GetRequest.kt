package maryk.core.query.requests

import maryk.core.objects.QueryDataModel
import maryk.core.objects.RootDataModel
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.types.Key
import maryk.core.properties.types.TypedValue
import maryk.core.properties.types.numeric.UInt64
import maryk.core.query.Order
import maryk.core.query.filters.FilterType
import maryk.core.query.filters.IsFilter

/**
 * Creates a Request to get DataObjects of type [DO] by [keys] and [filter] for the DataModel.
 * Optional: [order] can be applied to the results and the data can be shown as it was at [toVersion]
 * If [filterSoftDeleted] (default true) is set to false it will not filter away all soft deleted results.
 */
fun <DO: Any, P: PropertyDefinitions<DO>> RootDataModel<DO, P>.get(
    vararg keys: Key<DO>,
    filter: IsFilter? = null,
    order: Order? = null,
    toVersion: UInt64? = null,
    filterSoftDeleted: Boolean = true
) =
    GetRequest(this, keys.toList(), filter, order, toVersion, filterSoftDeleted)

/**
 * A Request to get DataObjects of type [DO] by [keys] and [filter] for specific DataModel of type [DM].
 * Optional: [order] can be applied to the results and the data can be shown as it was at [toVersion]
 * If [filterSoftDeleted] (default true) is set to false it will not filter away all soft deleted results.
 */
data class GetRequest<DO: Any, out DM: RootDataModel<DO, *>> internal constructor(
    override val dataModel: DM,
    override val keys: List<Key<DO>>,
    override val filter: IsFilter?,
    override val order: Order?,
    override val toVersion: UInt64?,
    override val filterSoftDeleted: Boolean
) : IsGetRequest<DO, DM> {
    internal companion object: QueryDataModel<GetRequest<*, *>>(
        properties = object : PropertyDefinitions<GetRequest<*, *>>() {
            init {
                IsObjectRequest.addDataModel(this, GetRequest<*, *>::dataModel)
                IsGetRequest.addKeys(this, GetRequest<*, *>::keys)
                IsFetchRequest.addFilter(this) {
                    it.filter?.let { TypedValue(it.filterType, it) }
                }
                IsFetchRequest.addOrder(this, GetRequest<*, *>::order)
                IsFetchRequest.addToVersion(this, GetRequest<*, *>::toVersion)
                IsFetchRequest.addFilterSoftDeleted(this, GetRequest<*, *>::filterSoftDeleted)
            }
        }
    ) {
        @Suppress("UNCHECKED_CAST")
        override fun invoke(map: Map<Int, *>) = GetRequest(
            dataModel = map[0] as RootDataModel<Any, *>,
            keys = map[1] as List<Key<Any>>,
            filter = (map[2] as TypedValue<FilterType, IsFilter>?)?.value,
            order = map[3] as Order?,
            toVersion = map[4] as UInt64?,
            filterSoftDeleted = map[5] as Boolean? ?: true
        )
    }
}
