package maryk.datastore.rocksdb.processors

import maryk.core.models.IsRootValuesDataModel
import maryk.core.properties.PropertyDefinitions
import maryk.core.query.requests.IsFetchRequest
import maryk.datastore.rocksdb.TableColumnFamilies
import maryk.datastore.rocksdb.processors.helpers.get
import maryk.rocksdb.ReadOptions
import maryk.rocksdb.Transaction

/**
 * Test if [dataRecord] should be filtered based on given FetchRequest
 * Filters on soft deleted state and given filters.
 * Return true if [dataRecord] should be filtered away.
 */
internal fun <DM : IsRootValuesDataModel<P>, P : PropertyDefinitions> IsFetchRequest<DM, P, *>.shouldBeFiltered(
    transaction: Transaction,
    columnFamilies: TableColumnFamilies,
    readOptions: ReadOptions,
    key: ByteArray,
    createdVersion: ULong,
    toVersion: ULong?
) = when {
    toVersion != null && createdVersion > toVersion -> true
    this.filterSoftDeleted && transaction.get(columnFamilies, readOptions, toVersion, byteArrayOf(*key, SOFT_DELETE_INDICATOR))?.last() == TRUE -> true
//    this.where != null -> !filterMatches(where as IsFilter, transaction, columnFamilies, readOptions, toVersion)
    else -> false
}

//
///** Test if [dataRecord] is passing given [filter]. True if filter matches */
//internal fun <DM : IsRootValuesDataModel<P>, P : PropertyDefinitions> filterMatches(
//    filter: IsFilter,
//    transaction: Transaction,
//    columnFamilies: TableColumnFamilies,
//    readOptions: ReadOptions,
//    toVersion: ULong?
//): Boolean {
//    when (filter.filterType) {
//        FilterType.And -> {
//            val and = filter as And
//            for (f in and.filters) {
//                if (!filterMatches(f, dataRecord, toVersion)) return false
//            }
//            return true
//        }
//        FilterType.Or -> {
//            val or = filter as Or
//            for (f in or.filters) {
//                if (filterMatches(f, dataRecord, toVersion)) return true
//            }
//            return false
//        }
//        FilterType.Not -> {
//            val notFilter = (filter as Not)
//            for (aFilter in notFilter.filters) {
//                // If internal filter succeeds, then fail
//                if (filterMatches(aFilter, dataRecord, toVersion)) return false
//            }
//            return true
//        }
//        FilterType.Exists -> {
//            val exists = filter as Exists
//            for (propRef in exists.references) {
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null }) return false
//            }
//            return true
//        }
//        FilterType.Equals -> {
//            val equals = filter as Equals
//            for ((propRef, value) in equals.referenceValuePairs) {
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it == value }) return false
//            }
//            return true
//        }
//        FilterType.LessThan -> {
//            val lessThan = filter as LessThan
//            for ((propRef, value) in lessThan.referenceValuePairs) {
//                @Suppress("UNCHECKED_CAST")
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null && (value as Comparable<Any>) > it }) return false
//            }
//            return true
//        }
//        FilterType.LessThanEquals -> {
//            val lessThanEquals = filter as LessThanEquals
//            for ((propRef, value) in lessThanEquals.referenceValuePairs) {
//                @Suppress("UNCHECKED_CAST")
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null && (value as Comparable<Any>) >= it }) return false
//            }
//            return true
//        }
//        FilterType.GreaterThan -> {
//            val greaterThan = filter as GreaterThan
//            for ((propRef, value) in greaterThan.referenceValuePairs) {
//                @Suppress("UNCHECKED_CAST")
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null && (value as Comparable<Any>) < it }) return false
//            }
//            return true
//        }
//        FilterType.GreaterThanEquals -> {
//            val greaterThanEquals = filter as GreaterThanEquals
//            for ((propRef, value) in greaterThanEquals.referenceValuePairs) {
//                @Suppress("UNCHECKED_CAST")
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null && (value as Comparable<Any>) <= it }) return false
//            }
//            return true
//        }
//        FilterType.Prefix -> {
//            val prefixFilter = filter as Prefix
//            for ((propRef, prefix) in prefixFilter.referenceValuePairs) {
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null && it.startsWith(prefix) }) return false
//            }
//            return true
//        }
//        FilterType.Range -> {
//            val rangeFilter = filter as Range
//            for ((propRef, range) in rangeFilter.referenceRangePairs) {
//                @Suppress("UNCHECKED_CAST")
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null && (it as Comparable<Any>) in range as ValueRange<Comparable<Any>> }) return false
//            }
//            return true
//        }
//        FilterType.RegEx -> {
//            val regExFilter = filter as RegEx
//            for ((propRef, regEx) in regExFilter.referenceValuePairs) {
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null && regEx.matches(it) }) return false
//            }
//            return true
//        }
//        FilterType.ValueIn -> {
//            val valueInFilter = filter as ValueIn
//            for ((propRef, values) in valueInFilter.referenceValuePairs) {
//                if (!dataRecord.matchQualifier(propRef, toVersion) { it != null && values.contains(it) }) return false
//            }
//            return true
//        }
//    }
//}
