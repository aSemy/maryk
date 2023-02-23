package maryk.datastore.shared.updates

import maryk.core.models.IsRootDataModel
import maryk.core.properties.IsValuesPropertyDefinitions
import maryk.core.properties.types.Key
import maryk.core.query.requests.IsGetRequest
import maryk.core.query.responses.IsDataResponse
import maryk.core.values.Values
import maryk.datastore.shared.IsDataStore
import maryk.datastore.shared.updates.Update.Change

/** Update listener for get requests */
class UpdateListenerForGet<DM: IsRootDataModel<P>, P: IsValuesPropertyDefinitions, RP: IsDataResponse<DM, P>>(
    request: IsGetRequest<DM, P, RP>,
    response: RP
) : UpdateListener<DM, P, IsGetRequest<DM, P, RP>>(
    request,
    response
) {
    override suspend fun process(
        update: Update<DM, P>,
        dataStore: IsDataStore
    ) {
        if (request.keys.contains(update.key)) {
            update.process(this, dataStore, sendFlow)
        }
    }

    override fun addValues(key: Key<DM>, values: Values<P>) =
        matchingKeys.value.binarySearch { it compareTo key }.let {
            // Only insert keys which were found in the matching keys
            if (it < 0) null else it
        }

    override suspend fun changeOrder(change: Change<DM, P>, changedHandler: suspend (Int?, Boolean) -> Unit) {
        val keyIndex = matchingKeys.value.indexOfFirst { it compareTo change.key == 0 }

        if (keyIndex >= 0) {
            changedHandler(if (keyIndex >= 0) keyIndex else null, false)
        }
    }
}
