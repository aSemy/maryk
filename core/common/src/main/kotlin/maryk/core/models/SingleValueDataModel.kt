package maryk.core.models

import maryk.core.objects.ObjectValues
import maryk.core.properties.IsPropertyContext
import maryk.core.properties.ObjectPropertyDefinitions
import maryk.core.properties.definitions.wrapper.IsPropertyDefinitionWrapper
import maryk.json.IsJsonLikeReader
import maryk.json.IsJsonLikeWriter
import maryk.json.JsonToken
import maryk.lib.exceptions.ParseException

/**
 * ObjectDataModel of type [DO] with [properties] definitions with a single property to contain
 * query actions so they can be validated and transported.
 *
 * In JSON/YAML this model is represented as just that property.
 */
internal abstract class QuerySingleValueDataModel<T: Any, DO: Any, P: ObjectPropertyDefinitions<DO>, CX: IsPropertyContext>(
    properties: P,
    private val singlePropertyDefinition: IsPropertyDefinitionWrapper<T, T, CX, DO>
) : AbstractDataModel<DO, P, CX, CX>(properties) {
    override fun writeJson(map: ObjectValues<DO, P>, writer: IsJsonLikeWriter, context: CX?) {
        // Input and output of singlePropertyDefinition has both to be T so can fetch original
        val value = map.original { singlePropertyDefinition } ?: throw ParseException("Missing requests in Requests")

        singlePropertyDefinition.writeJsonValue(value, writer, context)
        singlePropertyDefinition.capture(context, value)
    }

    override fun writeJson(obj: DO, writer: IsJsonLikeWriter, context: CX?) {
        val value = singlePropertyDefinition.getPropertyAndSerialize(obj, context) ?: throw ParseException("Missing ${singlePropertyDefinition.name} value")
        singlePropertyDefinition.writeJsonValue(value, writer, context)
        singlePropertyDefinition.capture(context, value)
    }

    override fun readJson(reader: IsJsonLikeReader, context: CX?): ObjectValues<DO, P> {
        if (reader.currentToken == JsonToken.StartDocument){
            reader.nextToken()
        }

        val value = singlePropertyDefinition.readJson(reader, context)
        singlePropertyDefinition.capture(context, value)

        return this.map {
            mapOf(
                singlePropertyDefinition with value
            )
        }
    }
}
