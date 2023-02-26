package maryk.datastore.memory.processors

import kotlinx.coroutines.flow.MutableSharedFlow
import maryk.core.clock.HLC
import maryk.core.models.fromChanges
import maryk.core.properties.IsRootModel
import maryk.core.query.changes.ObjectCreate
import maryk.core.query.responses.AddOrChangeResponse
import maryk.core.query.responses.statuses.IsAddOrChangeResponseStatus
import maryk.core.query.responses.updates.InitialChangesUpdate
import maryk.core.query.responses.updates.ProcessResponse
import maryk.core.services.responses.UpdateResponse
import maryk.datastore.memory.IsStoreFetcher
import maryk.datastore.memory.records.DataStore
import maryk.datastore.shared.StoreAction
import maryk.datastore.shared.updates.IsUpdateAction

/**
 * Processes the initial changes to values into the data store
 */
internal suspend fun <DM : IsRootModel> processInitialChangesUpdate(
    storeAction: StoreAction<DM, UpdateResponse<DM>, ProcessResponse<DM>>,
    dataStoreFetcher: (IsRootModel) -> DataStore<*>,
    updateSharedFlow: MutableSharedFlow<IsUpdateAction>
) {
    val dataModel = storeAction.request.dataModel
    @Suppress("UNCHECKED_CAST")
    val dataStore = (dataStoreFetcher as IsStoreFetcher<DM>).invoke(dataModel)

    val update = storeAction.request.update as InitialChangesUpdate<DM>

    val changeStatuses = mutableListOf<IsAddOrChangeResponseStatus<DM>>()
    for (change in update.changes) {
        for (versionedChange in change.changes) {
            if (versionedChange.changes.contains(ObjectCreate)) {
                val addedValues = dataModel.fromChanges(null, versionedChange.changes)

                changeStatuses += processAdd(
                    dataStore,
                    dataModel = dataModel,
                    key = change.key,
                    version = HLC(versionedChange.version),
                    objectToAdd = addedValues,
                    updateSharedFlow = updateSharedFlow
                )
            } else {
                changeStatuses += processChange(
                    dataStore,
                    dataModel,
                    change.key,
                    null,
                    versionedChange.changes,
                    HLC(versionedChange.version),
                    updateSharedFlow
                )
            }
        }
    }

    storeAction.response.complete(
        ProcessResponse(
            update.version,
            AddOrChangeResponse(
                dataModel,
                changeStatuses
            )
        )
    )
}
