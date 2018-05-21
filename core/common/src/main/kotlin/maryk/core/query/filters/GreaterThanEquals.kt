package maryk.core.query.filters

import maryk.core.objects.ReferencePairDataModel
import maryk.core.objects.ReferenceValuePairsPropertyDefinitions
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.definitions.wrapper.IsValuePropertyDefinitionWrapper
import maryk.core.properties.references.IsPropertyReference
import maryk.core.query.DataModelPropertyContext
import maryk.core.query.pairs.ReferenceValuePair
import maryk.core.query.pairs.with
import maryk.json.IsJsonLikeWriter

/** Referenced value should be greater than or equal given [value] of type [T] */
infix fun <T: Any> IsPropertyReference<T, IsValuePropertyDefinitionWrapper<T, *, IsPropertyContext, *>>.greaterThanEquals(
    value: T
) = GreaterThanEquals(this with value)

/** [referenceValuePairs] should be greater than and equal given value */
data class GreaterThanEquals(
    override val referenceValuePairs: List<ReferenceValuePair<Any>>
) : IsFilter, HasReferenceValuePairs {
    override val filterType = FilterType.GreaterThanEquals

    @Suppress("UNCHECKED_CAST")
    constructor(vararg referenceValuePair: ReferenceValuePair<*>): this(referenceValuePair.toList() as List<ReferenceValuePair<Any>>)

    internal object Properties : ReferenceValuePairsPropertyDefinitions<Any, GreaterThanEquals>() {
        override val referenceValuePairs = HasReferenceValuePairs.addReferenceValuePairs(
            this, GreaterThanEquals::referenceValuePairs
        )
    }

    internal companion object: ReferencePairDataModel<Any, GreaterThanEquals>(
        properties = Properties
    ) {
        override fun invoke(map: Map<Int, *>) = GreaterThanEquals(
            referenceValuePairs = map(0)
        )

        override fun writeJson(obj: GreaterThanEquals, writer: IsJsonLikeWriter, context: DataModelPropertyContext?) {
            writer.writeJsonMapObject(obj.referenceValuePairs, context)
        }
    }
}
