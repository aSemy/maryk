package maryk.core.definitions

import maryk.core.exceptions.ContextNotFoundException
import maryk.core.models.DataModel
import maryk.core.models.QuerySingleValueDataModel
import maryk.core.models.RootDataModel
import maryk.core.models.ValueDataModel
import maryk.core.objects.ObjectValues
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.IsSubDefinition
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.MultiTypeDefinition
import maryk.core.properties.definitions.contextual.ContextCaptureDefinition
import maryk.core.properties.definitions.contextual.ContextValueTransformDefinition
import maryk.core.properties.definitions.wrapper.IsPropertyDefinitionWrapper
import maryk.core.properties.enum.IndexedEnum
import maryk.core.properties.enum.IndexedEnumDefinition
import maryk.core.properties.types.TypedValue
import maryk.core.query.ContainsDefinitionsContext
import maryk.lib.exceptions.ParseException

/**
 * Contains multiple definitions of models and enums. Is passed MarykPrimitives like
 * DataModels and Enums to [definitions]
 */
data class Definitions(
    val definitions: List<MarykPrimitive>
) {
    constructor(vararg definition: MarykPrimitive): this(definition.toList())

    internal object Properties : ObjectPropertyDefinitions<Definitions>() {
        @Suppress("UNCHECKED_CAST")
        val definitions = add(1, "definitions",
            ListDefinition(
                valueDefinition = MultiTypeDefinition(
                    typeEnum = PrimitiveType,
                    definitionMap = mapOf(
                        PrimitiveType.Model to ContextCaptureDefinition(
                            definition = EmbeddedObjectDefinition(
                                dataModel = { DataModel.Model }
                            ),
                            capturer = { context, model ->
                                context?.let {
                                    it.dataModels[model.name] = { model }
                                } ?: throw ContextNotFoundException()
                            }
                        ),
                        PrimitiveType.ValueModel to ContextCaptureDefinition(
                            definition = EmbeddedObjectDefinition(
                                dataModel = { ValueDataModel.Model }
                            ),
                            capturer = { context, model ->
                                context?.let {
                                    it.dataModels[model.name] = { model }
                                } ?: throw ContextNotFoundException()
                            }
                        ),
                        PrimitiveType.RootModel to ContextCaptureDefinition(
                            definition = EmbeddedObjectDefinition(
                                dataModel = { RootDataModel.Model }
                            ),
                            capturer = { context: ContainsDefinitionsContext?, model ->
                                context?.let {
                                    it.dataModels[model.name] = { model }
                                } ?: throw ContextNotFoundException()
                            }
                        ),
                        PrimitiveType.EnumDefinition to ContextCaptureDefinition(
                            // This transformer takes care to catch Enums without values to replace them
                            // with previously defined Enums which are stored in the context
                            definition = ContextValueTransformDefinition(
                                definition = EmbeddedObjectDefinition(
                                    dataModel = { IndexedEnumDefinition.Model }
                                ),
                                valueTransformer = { context, value ->
                                    if (value.optionalValues == null) {
                                        context?.let {
                                            it.enums[value.name] as IndexedEnumDefinition<IndexedEnum<Any>>?
                                                ?: throw ParseException("Enum ${value.name} has not been defined")
                                        } ?: throw ContextNotFoundException()
                                    } else {
                                        value
                                    }
                                }
                            ),
                            capturer = { context, value ->
                                context?.let{
                                    it.enums[value.name] = value
                                } ?: throw ContextNotFoundException()
                            }
                        )
                    ) as Map<PrimitiveType, IsSubDefinition<out Any, ContainsDefinitionsContext>>
                )
            ) as ListDefinition<TypedValue<PrimitiveType, MarykPrimitive>, ContainsDefinitionsContext>,
            Definitions::definitions,
            fromSerializable = { it.value },
            toSerializable = { TypedValue(it.primitiveType, it) }
        )
    }

    @Suppress("UNCHECKED_CAST")
    internal companion object: QuerySingleValueDataModel<List<MarykPrimitive>, Definitions, Properties, ContainsDefinitionsContext>(
        properties = Properties,
        singlePropertyDefinition = Properties.definitions as IsPropertyDefinitionWrapper<List<MarykPrimitive>, List<MarykPrimitive>, ContainsDefinitionsContext, Definitions>
    ) {
        override fun invoke(map: ObjectValues<Definitions, Properties>) = Definitions(
            definitions = map(1)
        )
    }
}