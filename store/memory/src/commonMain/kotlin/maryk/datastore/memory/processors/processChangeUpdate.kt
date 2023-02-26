package maryk.datastore.memory.processors

import kotlinx.coroutines.flow.MutableSharedFlow
import maryk.core.clock.HLC
import maryk.core.models.fromChanges
import maryk.core.properties.IsRootModel
import maryk.core.query.changes.ObjectCreate
import maryk.core.query.responses.AddResponse
import maryk.core.query.responses.ChangeResponse
import maryk.core.query.responses.updates.ChangeUpdate
import maryk.core.query.responses.updates.ProcessResponse
import maryk.core.services.responses.UpdateResponse
import maryk.datastore.memory.IsStoreFetcher
import maryk.datastore.memory.records.DataStore
import maryk.datastore.shared.StoreAction
import maryk.datastore.shared.updates.IsUpdateAction

/**
 * Processes the changes to values into the data store
 */
internal suspend fun <DM : IsRootModel> processChangeUpdate(
    storeAction: StoreAction<DM, UpdateResponse<DM>, ProcessResponse<DM>>,
    dataStoreFetcher: (IsRootModel) -> DataStore<IsRootModel>,
    updateSharedFlow: MutableSharedFlow<IsUpdateAction>
) {
    val dataModel = storeAction.request.dataModel
    @Suppress("UNCHECKED_CAST")
    val dataStore = (dataStoreFetcher as IsStoreFetcher<DM>).invoke(dataModel)

    val update = storeAction.request.update as ChangeUpdate<DM>

    if (update.changes.contains(ObjectCreate)) {
        val addedValues = dataModel.fromChanges(null, update.changes)

        val response = processAdd(
            dataStore = dataStore,
            dataModel = dataModel,
            key = update.key,
            version = HLC(update.version),
            objectToAdd = addedValues,
            updateSharedFlow = updateSharedFlow
        )

        storeAction.response.complete(
            ProcessResponse(update.version, AddResponse(dataModel, listOf(response)))
        )
    } else {
        val response = processChange(
            dataStore,
            dataModel,
            update.key,
            null,
            update.changes,
            HLC(update.version),
            updateSharedFlow
        )

        storeAction.response.complete(
            ProcessResponse(update.version, ChangeResponse(dataModel, listOf(response)))
        )
    }
}
