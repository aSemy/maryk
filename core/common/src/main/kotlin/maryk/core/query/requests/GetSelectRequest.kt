package maryk.core.query.requests

import maryk.core.models.IsRootDataModel
import maryk.core.models.RootObjectDataModel
import maryk.core.models.SimpleQueryDataModel
import maryk.core.objects.SimpleObjectValues
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.graph.RootPropRefGraph
import maryk.core.properties.types.Key
import maryk.core.properties.types.TypedValue
import maryk.core.properties.types.numeric.UInt64
import maryk.core.query.Order
import maryk.core.query.filters.FilterType
import maryk.core.query.filters.IsFilter

/**
 * Creates a Request to get [select] values of DataObjects of type [DO] by [keys] and [filter] for the DataModel.
 * Optional: [order] can be applied to the results and the data can be shown as it was at [toVersion]
 * If [filterSoftDeleted] (default true) is set to false it will not filter away all soft deleted results.
 */
fun <DM: IsRootDataModel<P>, P: ObjectPropertyDefinitions<*>> DM.getSelect(
    vararg keys: Key<DM>,
    filter: IsFilter? = null,
    order: Order? = null,
    toVersion: UInt64? = null,
    select: RootPropRefGraph<DM>,
    filterSoftDeleted: Boolean = true
) =
    GetSelectRequest(this, keys.toList(), filter, order, toVersion, select, filterSoftDeleted)

/**
 * A Request to get [select] values of DataObjects of type [DO] by [keys] and [filter] for specific DataModel of type [DM].
 * Optional: [order] can be applied to the results and the data can be shown as it was at [toVersion]
 * If [filterSoftDeleted] (default true) is set to false it will not filter away all soft deleted results.
 */
data class GetSelectRequest<DM: IsRootDataModel<*>> internal constructor(
    override val dataModel: DM,
    override val keys: List<Key<DM>>,
    override val filter: IsFilter?,
    override val order: Order?,
    override val toVersion: UInt64?,
    override val select: RootPropRefGraph<DM>,
    override val filterSoftDeleted: Boolean
) : IsGetRequest<DM>, IsSelectRequest<DM> {
    override val requestType = RequestType.Get

    internal companion object: SimpleQueryDataModel<GetSelectRequest<*>>(
        properties = object : ObjectPropertyDefinitions<GetSelectRequest<*>>() {
            init {
                IsObjectRequest.addDataModel(this, GetSelectRequest<*>::dataModel)
                IsGetRequest.addKeys(this, GetSelectRequest<*>::keys)
                IsFetchRequest.addFilter(this) { request ->
                    request.filter?.let { TypedValue(it.filterType, it) }
                }
                IsFetchRequest.addOrder(this, GetSelectRequest<*>::order)
                IsFetchRequest.addToVersion(this, GetSelectRequest<*>::toVersion)
                IsFetchRequest.addFilterSoftDeleted(this, GetSelectRequest<*>::filterSoftDeleted)
                IsSelectRequest.addSelect(6, this, GetSelectRequest<*>::select)
            }
        }
    ) {
        override fun invoke(map: SimpleObjectValues<GetSelectRequest<*>>) = GetSelectRequest(
            dataModel = map<RootObjectDataModel<*, Any, *>>(0),
            keys = map(1),
            filter = map<TypedValue<FilterType, IsFilter>?>(2)?.value,
            order = map(3),
            toVersion = map(4),
            filterSoftDeleted = map(5),
            select = map(6)
        )
    }
}
