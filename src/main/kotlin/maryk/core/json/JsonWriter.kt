package maryk.core.json

/** Describes JSON elements that can be written */
private enum class JsonType {
    START, START_OBJ, END_OBJ, FIELD_NAME, OBJ_VALUE, START_ARRAY, END_ARRAY, ARRAY_VALUE
}

/** A JSON generator for streaming JSON generation */
class JsonWriter(
        val pretty: Boolean = false,
        private val writer: (String) -> Unit
) {
    private var lastType: JsonType = JsonType.START
    private var typeStack: MutableList<JsonObjectType> = mutableListOf()

    fun writeStartObject() {
        if(lastType != JsonType.START_ARRAY
                && !typeStack.isEmpty()
                && typeStack.last() == JsonObjectType.ARRAY
        ) {
            writer(",")
            if (pretty) { writer(" ") }
        }
        typeStack.add(JsonObjectType.OBJECT)
        write(JsonType.START_OBJ, "{", JsonType.START, JsonType.FIELD_NAME, JsonType.ARRAY_VALUE, JsonType.START_ARRAY, JsonType.END_OBJ)

        makePretty()
    }

    fun writeEndObject() {
        if(typeStack.isEmpty() || typeStack.last() != JsonObjectType.OBJECT) {
            throw IllegalJsonOperation("Json: There is no object to close")
        }
        typeStack.removeAt(typeStack.lastIndex)
        makePretty()
        write(JsonType.END_OBJ, "}", JsonType.START_OBJ, JsonType.OBJ_VALUE, JsonType.END_OBJ, JsonType.END_ARRAY)
    }

    fun writeStartArray() {
        if(lastType != JsonType.START_ARRAY
                && !typeStack.isEmpty()
                && typeStack.last() == JsonObjectType.ARRAY
                ) {
            writer(",")
            if (pretty) { writer(" ") }
        }
        typeStack.add(JsonObjectType.ARRAY)
        write(JsonType.START_ARRAY, "[", JsonType.START, JsonType.FIELD_NAME, JsonType.START_ARRAY, JsonType.END_ARRAY)
    }

    fun writeEndArray() {
        if(typeStack.isEmpty() || typeStack.last() != JsonObjectType.ARRAY) {
            throw IllegalJsonOperation("Json: There is no array to close")
        }
        typeStack.removeAt(typeStack.lastIndex)
        write(JsonType.END_ARRAY, "]", JsonType.START_ARRAY, JsonType.ARRAY_VALUE, JsonType.END_ARRAY, JsonType.END_OBJ)
    }

    /** Writes the field name for an object */
    fun writeFieldName(name: String) {
        if(lastType != JsonType.START_OBJ) {
            writer(",")
            makePretty()
        }
        write(JsonType.FIELD_NAME, "\"$name\":", JsonType.START_OBJ, JsonType.OBJ_VALUE, JsonType.END_ARRAY, JsonType.END_OBJ)
        if (pretty) { writer(" ") }
    }

    /** Writes a string value including quotes */
    fun writeString(value: String) = writeValue("\"$value\"")

    /** Writes a value excluding quotes */
    fun writeValue(value: String) = if (!typeStack.isEmpty()) {
        when(typeStack.last()) {
            JsonObjectType.OBJECT -> {
                write(JsonType.OBJ_VALUE, value, JsonType.FIELD_NAME)
            }
            JsonObjectType.ARRAY -> {
                if(lastType != JsonType.START_ARRAY) {
                    writer(",")
                    if (pretty) { writer(" ") }
                }
                write(JsonType.ARRAY_VALUE, value, JsonType.START_ARRAY, JsonType.ARRAY_VALUE)
            }
        }
    } else {
        throw IllegalJsonOperation("Cannot write a value outside array or object")
    }

    private fun makePretty() {
        if (pretty) {
            writer("\n")
            typeStack.forEach{
                if(it == JsonObjectType.OBJECT) { writer("\t") }
            }
        }
    }

    private fun write(type: JsonType, value: String, vararg allowed: JsonType){
        if (lastType !in allowed) {
            throw IllegalJsonOperation("Json: $type not allowed after $lastType")
        }

        writer(value)
        lastType = type
    }
}

/** Exception for invalid JSON */
class IllegalJsonOperation(
        description: String
): Throwable(description)