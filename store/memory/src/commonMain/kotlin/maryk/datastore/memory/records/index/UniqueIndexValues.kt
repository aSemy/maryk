package maryk.datastore.memory.records.index

import maryk.core.properties.IsRootModel

/**
 * Contains all unique index values and has methods to add, get or remove unique value references
 */
internal class UniqueIndexValues<DM : IsRootModel, T : Comparable<T>>(
    indexReference: ByteArray
) : AbstractIndexValues<DM, T>(
    indexReference
) {
    override val compareTo = Comparable<T>::compareTo
}
