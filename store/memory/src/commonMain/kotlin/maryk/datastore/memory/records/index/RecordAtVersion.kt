package maryk.datastore.memory.records.index

import maryk.core.clock.HLC
import maryk.core.properties.IsRootModel
import maryk.datastore.memory.records.DataRecord

/**  [record] at [version]. Primarily for in indexes */
internal class RecordAtVersion<DM : IsRootModel>(
    override val record: DataRecord<DM>?,
    override val version: HLC
) : IsRecordAtVersion<DM>
