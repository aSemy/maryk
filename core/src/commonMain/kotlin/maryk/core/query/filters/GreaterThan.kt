package maryk.core.query.filters

import maryk.core.properties.ReferenceValuePairModel
import maryk.core.query.pairs.ReferenceValuePair
import maryk.core.values.ObjectValues

/** Referenced values in [referenceValuePairs] should be greater than and not equal given value */
data class GreaterThan internal constructor(
    override val referenceValuePairs: List<ReferenceValuePair<Any>>
) : IsReferenceValuePairsFilter<Any> {
    override val filterType = FilterType.GreaterThan

    @Suppress("UNCHECKED_CAST")
    constructor(vararg referenceValuePair: ReferenceValuePair<*>) : this(referenceValuePair.toList() as List<ReferenceValuePair<Any>>)

    companion object : ReferenceValuePairModel<GreaterThan, Companion, ReferenceValuePair<Any>, Any, Any>(
        pairName = "referenceValuePairs",
        pairGetter = GreaterThan::referenceValuePairs,
        pairModel = ReferenceValuePair,
        pairProperties = ReferenceValuePair.Properties
    ) {
        override fun invoke(values: ObjectValues<GreaterThan, Companion>) = GreaterThan(
            referenceValuePairs = values(1u)
        )
    }
}
