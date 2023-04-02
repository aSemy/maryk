package maryk.core.properties

import maryk.core.properties.definitions.contextual.ContextualPropertyReferenceDefinition
import maryk.core.properties.definitions.wrapper.ContextualDefinitionWrapper
import maryk.core.properties.definitions.wrapper.IsDefinitionWrapper
import maryk.core.properties.references.AnyPropertyReference
import maryk.core.query.DefinedByReference
import maryk.core.query.RequestContext
import maryk.core.values.ObjectValues


/** Defines PropertyDefinitions for a reference value pair, so they can be easily converted inside ReferencePairDataModels */
interface IsReferenceValuePairModel<R : DefinedByReference<*>, T: Any, TO: Any, out W: IsDefinitionWrapper<T, TO, RequestContext, R>> : IsObjectPropertyDefinitions<R> {
    val reference: ContextualDefinitionWrapper<AnyPropertyReference, AnyPropertyReference, RequestContext, ContextualPropertyReferenceDefinition<RequestContext>, R>
    val value: W
}

abstract class ReferenceValuePairModel<R : DefinedByReference<*>, P: ReferenceValuePairModel<R, P, T, TO, W>, T: Any, TO: Any, W: IsDefinitionWrapper<T, TO, RequestContext, R>>
    : ObjectModel<R, P, RequestContext, RequestContext>(), IsReferenceValuePairModel<R, T, TO, W> {
    abstract override fun invoke(values: ObjectValues<R, P>): R
}
