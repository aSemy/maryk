package maryk.datastore.memory.processors

import kotlinx.coroutines.channels.SendChannel
import maryk.core.clock.HLC
import maryk.core.models.IsRootValuesDataModel
import maryk.core.properties.PropertyDefinitions
import maryk.core.query.responses.updates.ChangeUpdate
import maryk.core.query.responses.updates.ProcessResponse
import maryk.core.services.responses.UpdateResponse
import maryk.datastore.memory.records.DataStore
import maryk.datastore.shared.StoreAction
import maryk.datastore.shared.updates.IsUpdateAction

@Suppress("UNUSED_PARAMETER")
internal suspend fun <DM : IsRootValuesDataModel<P>, P : PropertyDefinitions> processChangeUpdate(
    storeAction: StoreAction<DM, P, UpdateResponse<DM, P>, ProcessResponse>,
    dataStoreFetcher: (IsRootValuesDataModel<*>) -> DataStore<*, *>,
    updateSendChannel: SendChannel<IsUpdateAction>
) {
    val dataModel = storeAction.request.dataModel
    @Suppress("UNCHECKED_CAST")
    val dataStore = dataStoreFetcher(dataModel) as DataStore<DM, P>

    val update = storeAction.request.update as ChangeUpdate<DM, P>

    val index = dataStore.records.binarySearch { it.key.compareTo(update.key) }

    if (index >= 0) {
        val objectToChange = dataStore.records[index]

        processChange(
            dataModel,
            dataStore,
            objectToChange,
            update.changes,
            HLC(update.version),
            dataStore.keepAllVersions,
            updateSendChannel
        )
    }

    storeAction.response.complete(ProcessResponse())
}
