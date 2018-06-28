package maryk.core.models

import maryk.core.objects.ValueMap
import maryk.core.properties.definitions.IsPropertyDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.properties.definitions.wrapper.IsPropertyDefinitionWrapper
import maryk.core.properties.exceptions.ValidationUmbrellaException
import maryk.core.properties.references.IsPropertyReference

typealias IsSimpleDataModel<DO> = IsDataModel<DO, PropertyDefinitions<DO>>

/** A DataModel which holds properties and can be validated */
interface IsDataModel<DO: Any, P: PropertyDefinitions<DO>> {
    /** Object which contains all property definitions. Can also be used to get property references. */
    val properties: P

    /**
     * Validate a [dataObject] and get reference from [refGetter] if exception needs to be thrown
     * @throws ValidationUmbrellaException if input was invalid
     */
    fun validate(dataObject: DO, refGetter: () -> IsPropertyReference<DO, IsPropertyDefinition<DO>>? = { null })

    /**
     * Validate a [map] with values and get reference from [refGetter] if exception needs to be thrown
     * @throws ValidationUmbrellaException if input was invalid
     */
    fun validate(map: ValueMap<DO, P>, refGetter: () -> IsPropertyReference<DO, IsPropertyDefinition<DO>>? = { null })

    /** Creates a Data Object by [map] */
    operator fun invoke(map: ValueMap<DO, P>): DO

    /** Create a ValueMap with given [createMap] function */
    fun map(createMap: P.() -> Map<Int, Any?>) = ValueMap(this, createMap(this.properties))

    /**
     * To get a top level reference on a model by passing a [propertyDefinitionGetter] from its defined Properties
     * Optionally pass an already resolved [parent]
     */
    fun <T: Any, W: IsPropertyDefinitionWrapper<T, *, *, *>> graph(
        parent: IsPropertyReference<out Any, IsPropertyDefinition<*>>? = null,
        propertyDefinitionGetter: P.()-> W
    ): IsPropertyReference<T, W>
}
