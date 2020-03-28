package maryk.datastore.shared.updates

import kotlinx.coroutines.channels.SendChannel
import maryk.core.models.IsRootValuesDataModel
import maryk.core.properties.PropertyDefinitions
import maryk.core.query.requests.GetChangesRequest
import maryk.core.query.responses.updates.IsUpdateResponse

/** Update listener for get requests */
class UpdateListenerForGet<DM: IsRootValuesDataModel<P>, P: PropertyDefinitions>(
    val request: GetChangesRequest<DM, P>,
    sendChannel: SendChannel<IsUpdateResponse<DM, P>>
) : UpdateListener<DM, P>(sendChannel) {
    override suspend fun process(update: Update<DM, P>) {
        if (request.keys.contains(update.key)) {
            update.process(request, sendChannel)
        }
    }
}
