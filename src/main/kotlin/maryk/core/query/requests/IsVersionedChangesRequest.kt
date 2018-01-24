package maryk.core.query.requests

import maryk.core.objects.RootDataModel
import maryk.core.properties.definitions.NumberDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.types.numeric.UInt32

/** Request for all versioned changes from a version and later */
interface IsVersionedChangesRequest<DO: Any, out DM: RootDataModel<DO, *>> : IsChangesRequest<DO, DM> {
    val maxVersions: UInt32

    companion object {
        fun <DM: Any> addMaxVersions(index: Int, definitions: PropertyDefinitions<DM>, getter: (DM) -> UInt32?) {
            definitions.add(index, "maxVersions", NumberDefinition(type = UInt32), getter)
        }
    }
}