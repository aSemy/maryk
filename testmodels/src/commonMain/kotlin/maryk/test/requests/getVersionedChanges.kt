@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package maryk.test.requests

import maryk.core.properties.graph.graph
import maryk.core.query.descending
import maryk.core.query.filters.Exists
import maryk.core.query.requests.getChanges
import maryk.test.models.SimpleMarykModel

private val key1 = SimpleMarykModel.key("WWurg6ysTsozoMei/SurOw")
private val key2 = SimpleMarykModel.key("awfbjYrVQ+cdXblfQKV10A")

val getChangesRequest = SimpleMarykModel.getChanges(
    key1,
    key2,
    fromVersion = 1234uL
)

val getChangesMaxRequest = SimpleMarykModel.run {
    getChanges(
        key1,
        key2,
        filter = Exists(ref { value }),
        order = ref { value }.descending(),
        fromVersion = 1234uL,
        toVersion = 12345uL,
        maxVersions = 5u,
        filterSoftDeleted = true,
        select = graph { listOf(value) }
    )
}
