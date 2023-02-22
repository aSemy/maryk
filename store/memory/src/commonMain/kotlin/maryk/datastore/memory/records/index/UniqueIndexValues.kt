package maryk.datastore.memory.records.index

import maryk.core.models.IsRootDataModel
import maryk.core.properties.IsValuesPropertyDefinitions

/**
 * Contains all unique index values and has methods to add, get or remove unique value references
 */
internal class UniqueIndexValues<DM : IsRootDataModel<P>, P : IsValuesPropertyDefinitions, T : Comparable<T>>(
    indexReference: ByteArray
) : AbstractIndexValues<DM, P, T>(
    indexReference
) {
    override val compareTo = Comparable<T>::compareTo
}
