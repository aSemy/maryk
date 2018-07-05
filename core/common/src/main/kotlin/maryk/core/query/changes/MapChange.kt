package maryk.core.query.changes

import maryk.core.models.ReferenceMappedDataModel
import maryk.core.objects.Values
import maryk.core.properties.definitions.EmbeddedObjectDefinition
import maryk.core.properties.definitions.ListDefinition
import maryk.core.properties.definitions.PropertyDefinitions
import maryk.core.query.DataModelPropertyContext
import maryk.json.IsJsonLikeWriter
import maryk.lib.exceptions.ParseException

/** Defines changes to maps by [mapValueChanges] */
data class MapChange internal constructor(
    val mapValueChanges: List<MapValueChanges<*, *>>
) : IsChange {
    override val changeType = ChangeType.MapChange

    constructor(vararg mapValueChange: MapValueChanges<*, *>): this(mapValueChange.toList())

    internal object Properties : PropertyDefinitions<MapChange>() {
        val mapValueChanges = add(0, "mapValueChanges",
            ListDefinition(
                valueDefinition = EmbeddedObjectDefinition(
                    dataModel = { MapValueChanges }
                )
            ),
            MapChange::mapValueChanges
        )
    }

    internal companion object: ReferenceMappedDataModel<MapChange, MapValueChanges<*, *>, Properties, MapValueChanges.Properties>(
        properties = MapChange.Properties,
        containedDataModel = MapValueChanges,
        referenceProperty = MapValueChanges.Properties.reference
    ) {
        override fun invoke(map: Values<MapChange, MapChange.Properties>) = MapChange(
            mapValueChanges = map(0)
        )

        override fun writeJson(map: Values<MapChange, MapChange.Properties>, writer: IsJsonLikeWriter, context: DataModelPropertyContext?) {
            writeReferenceValueMap(
                writer,
                map { mapValueChanges } ?: throw ParseException("Missing mapValueChanges in MapChange"),
                context
            )
        }

        override fun writeJson(obj: MapChange, writer: IsJsonLikeWriter, context: DataModelPropertyContext?) {
            writeReferenceValueMap(writer, obj.mapValueChanges, context)
        }
    }
}
