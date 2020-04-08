package maryk.datastore.shared.updates

import kotlinx.coroutines.channels.SendChannel
import maryk.core.models.IsRootValuesDataModel
import maryk.core.properties.PropertyDefinitions
import maryk.core.properties.types.Key
import maryk.core.query.requests.GetChangesRequest
import maryk.core.query.responses.updates.IsUpdateResponse
import maryk.core.values.Values
import maryk.datastore.shared.AbstractDataStore

/** Update listener for get requests */
class UpdateListenerForGet<DM: IsRootValuesDataModel<P>, P: PropertyDefinitions>(
    request: GetChangesRequest<DM, P>,
    matchingKeys: List<Key<DM>>,
    sendChannel: SendChannel<IsUpdateResponse<DM, P>>
) : UpdateListener<DM, P, GetChangesRequest<DM, P>>(request, matchingKeys.toMutableList(), sendChannel) {
    override suspend fun process(
        update: Update<DM, P>,
        dataStore: AbstractDataStore
    ) {
        if (request.keys.contains(update.key)) {
            update.process(this, dataStore, sendChannel)
        }
    }

    override fun addValues(key: Key<DM>, values: Values<DM, P>) =
        matchingKeys.binarySearch { it.compareTo(key) }.let {
            // Only insert keys which were found in the matching keys
            if (it < 0) null else it
        }
}
