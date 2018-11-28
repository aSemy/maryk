@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS", "EXPERIMENTAL_API_USAGE")

package maryk.datastore.memory.processors

import maryk.core.models.IsRootValuesDataModel
import maryk.core.properties.PropertyDefinitions
import maryk.core.query.requests.DeleteRequest
import maryk.core.query.responses.DeleteResponse
import maryk.core.query.responses.statuses.DoesNotExist
import maryk.core.query.responses.statuses.IsDeleteResponseStatus
import maryk.core.query.responses.statuses.ServerFail
import maryk.core.query.responses.statuses.Success
import maryk.datastore.memory.StoreAction
import maryk.datastore.memory.records.DataStore
import maryk.datastore.memory.records.DeleteState.Deleted
import maryk.lib.time.Instant

internal typealias DeleteStoreAction<DM, P> = StoreAction<DM, P, DeleteRequest<DM>, DeleteResponse<DM>>
internal typealias AnyDeleteStoreAction = DeleteStoreAction<IsRootValuesDataModel<PropertyDefinitions>, PropertyDefinitions>

internal fun <DM: IsRootValuesDataModel<P>, P: PropertyDefinitions> processDeleteRequest(
    storeAction: DeleteStoreAction<DM, P>,
    dataStore: DataStore<DM, P>
) {
    val deleteRequest = storeAction.request
    val statuses = mutableListOf<IsDeleteResponseStatus<DM>>()

    if (deleteRequest.objectsToDelete.isNotEmpty()) {
        val version = Instant.getCurrentEpochTimeInMillis().toULong()

        for (key in deleteRequest.objectsToDelete) {
            try {
                val index = dataStore.records.binarySearch { it.key.compareTo(key) }

                val status: IsDeleteResponseStatus<DM> = when {
                    index > -1 -> {
                        if (deleteRequest.hardDelete) {
                            dataStore.records.removeAt(index)
                        } else {
                            val newRecord = dataStore.records[index].copy(
                                isDeleted = Deleted(version)
                            )
                            dataStore.records[index] = newRecord
                        }
                        Success(version)
                    }
                    else -> DoesNotExist(key)
                }

                statuses.add(status)
            } catch (e: Throwable) {
                statuses.add(ServerFail(e.toString()))
            }
        }
    }

    storeAction.response.complete(
        DeleteResponse(
            storeAction.request.dataModel,
            statuses
        )
    )
}
