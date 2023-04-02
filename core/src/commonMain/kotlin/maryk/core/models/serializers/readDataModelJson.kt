package maryk.core.models.serializers

import maryk.core.exceptions.RequestException
import maryk.core.exceptions.SerializationException
import maryk.core.models.AbstractDataModel
import maryk.core.models.IsDataModelWithPropertyDefinitions
import maryk.core.models.IsMutableDataModel
import maryk.core.properties.IsPropertyContext
import maryk.core.models.IsDataModel
import maryk.core.models.IsDataModelCollectionDefinition
import maryk.core.properties.definitions.wrapper.IsDefinitionWrapper
import maryk.core.query.ContainsDefinitionsContext
import maryk.core.query.DefinitionsConversionContext
import maryk.core.values.MutableValueItems
import maryk.json.IsJsonLikeReader
import maryk.json.JsonToken.FieldName
import maryk.json.JsonToken.StartComplexFieldName
import maryk.json.JsonToken.Stopped
import maryk.lib.exceptions.ParseException
import maryk.yaml.IsYamlReader

internal fun <DM : IsDataModelWithPropertyDefinitions<*, *>> readDataModelJson(
    context: ContainsDefinitionsContext?,
    reader: IsJsonLikeReader,
    values: MutableValueItems,
    properties: DM,
    propertyDefinitionsCreator: () -> IsMutableDataModel<*>,
    processAfterPropertiesAndContinue: ((IsDefinitionWrapper<Any, Any, IsPropertyContext, *>) -> Boolean)? = null
) {
    var propertiesAreProcessed = false
    val propertiesAsWrapper = properties.properties as IsDefinitionWrapper<*, *, *, *>
    @Suppress("UNCHECKED_CAST")
    val propertyDefinitions = lazy {
        propertyDefinitionsCreator().apply {
            (propertiesAsWrapper.definition as IsDataModelCollectionDefinition<IsDataModel>).capturer.invoke(
                Unit,
                context as DefinitionsConversionContext,
                this
            )
        } as IsMutableDataModel<IsDefinitionWrapper<*, *, *, *>>
    }

    // Inject name if it was defined as a map key in a higher level
    context?.currentDefinitionName?.let { name ->
        if (name.isNotBlank()) {
            if (values.contains(properties.name.index)) {
                throw RequestException("Name $name was already defined by map")
            }
            // Reset it so no deeper value can reuse it
            context.currentDefinitionName = ""

            values[properties.name.index] = name
        }
    }

    walker@ do {
        val token = reader.currentToken
        when (token) {
            is StartComplexFieldName -> {
                propertyDefinitions.value += properties.properties.valueDefinition.readJson(reader, context)
            }
            is FieldName -> {
                val value = token.value ?: throw ParseException("Empty field name not allowed in JSON")
                if (propertyDefinitions.isInitialized()) {
                    throw ParseException("Cannot use $value definition after property definitions")
                }

                val definition = (properties as AbstractDataModel<*>)[value]
                if (definition == null) {
                    reader.skipUntilNextField()
                    continue@walker
                } else {
                    if (definition == properties.properties) {
                        if (reader is IsYamlReader) {
                            throw SerializationException("Property definitions should be written as complex key value.")
                        }
                        propertiesAreProcessed = true
                    } else if (
                        !propertiesAreProcessed &&
                        processAfterPropertiesAndContinue != null &&
                        processAfterPropertiesAndContinue(definition)
                    ) {
                        continue@walker
                    }

                    reader.nextToken()

                    values[definition.index] = definition.definition.readJson(reader, context)
                }
            }
            else -> break@walker
        }
        reader.nextToken()
    } while (token !is Stopped)

    if (propertyDefinitions.isInitialized()) {
        values[propertiesAsWrapper.index] = propertyDefinitions.value
    }
}
