package maryk.yaml

import maryk.json.JsonToken
import maryk.json.JsonToken.FieldName
import maryk.json.JsonToken.MergeFieldName
import maryk.json.JsonToken.NullValue
import maryk.json.JsonToken.Value
import maryk.json.TokenType
import maryk.json.ValueType
import maryk.json.ValueType.Bool
import maryk.json.ValueType.IsNullValueType
import maryk.lib.bytes.Base64
import maryk.lib.time.DateTime
import maryk.yaml.YamlValueType.Binary
import maryk.yaml.YamlValueType.TimeStamp
import kotlin.math.pow

private val trueValues = arrayOf("True", "TRUE", "true", "y", "Y", "yes", "YES", "Yes", "on", "ON", "On")
private val falseValues = arrayOf("False", "FALSE", "false", "n", "N", "no", "NO", "No", "off", "OFF", "Off")
private val nullValues = arrayOf("~", "Null", "null", "NULL")
private val nanValues = arrayOf(".nan", ".NAN", ".Nan")
private val infinityRegEx = Regex("^([-+]?)(\\.inf|\\.Inf|\\.INF)$")
private val base2RegEx = Regex("^[-+]?0b([0-1_]+)$")
private val base8RegEx = Regex("^[-+]?0([0-7_]+)$")
private val base10RegEx = Regex("^[-+]?(0|[1-9][0-9_]*)$")
private val base16RegEx = Regex("^[-+]?0x([0-9a-fA-F_]+)$")
private val base60RegEx = Regex("^[-+]?([1-9][0-9_]*)(:([0-5]?[0-9]))+$")
private val floatRegEx = Regex("^[-+]?(\\.[0-9]+|[0-9]+(\\.[0-9]*)?)([eE][-+]?[0-9]+)?$")
private val timestampRegex = Regex(
    "^([0-9][0-9][0-9][0-9])" + // year
            "-([0-9][0-9]?)" + // month
            "-([0-9][0-9]?)" + // day
            "(([Tt]|[ \\t]+)([0-9][0-9]?)" + // hour
            ":([0-9][0-9])" + // minute
            ":([0-9][0-9])" + // second
            "(\\.([0-9]*))?" + // fraction
            "(([ \\t]*)Z|([-+][0-9][0-9])?(:([0-9][0-9]))?)?)?$"  // time zone
)

internal typealias JsonTokenCreator = (value: String?, isPlainStringReader: Boolean, tag: TokenType?, extraIndentAtStart: Int) -> JsonToken

/**
 * Creates a FieldName based on [fieldName]
 * Will return an exception if [fieldName] was already present in [foundFieldNames]
 * Can create a MergeFieldName if token was << and was read by a PlainStringReader signalled by [isPlainStringReader] = true
 */
internal fun checkAndCreateFieldName(
    foundFieldNames: MutableList<String?>,
    fieldName: String?,
    isPlainStringReader: Boolean
) =
    if (!foundFieldNames.contains(fieldName)) {
        foundFieldNames += fieldName
        if (isPlainStringReader && fieldName == "<<") {
            MergeFieldName
        } else {
            FieldName(fieldName)
        }
    } else {
        throw InvalidYamlContent("Duplicate field name $fieldName in flow map")
    }

/**
 * Creates a JsonToken.Value by reading [value] and [tag]
 * If from plain string with [isPlainStringReader] = true and [tag] = false it will try to determine ValueType from contents.
 */
internal fun createYamlValueToken(
    value: String?,
    tag: TokenType?,
    isPlainStringReader: Boolean
): Value<Any?> {
    return tag?.let { tokenType ->
        if (value == null && tokenType != ValueType.Null) {
            throw InvalidYamlContent("Cannot have a null value with explicit tag which is not !!null")
        }
        when (tokenType) {
            !is ValueType<*> -> {
                throw InvalidYamlContent("Cannot use non value tag with value $value")
            }
            is Bool -> when (value) {
                in trueValues -> Value(true, tokenType)
                in falseValues -> Value(false, tokenType)
                else -> throw InvalidYamlContent("Unknown !!bool value $value")
            }
            is IsNullValueType -> when (value) {
                null, in nullValues -> NullValue
                else -> throw InvalidYamlContent("Unknown !!null value $value")
            }
            is ValueType.Float -> when (value) {
                in nanValues -> Value(Double.NaN, tokenType)
                else -> {
                    findInfinity(value!!)?.let { return it }
                    findFloat(value)?.let { return it }
                    throw InvalidYamlContent("Expected float value and not $value")
                }
            }
            is ValueType.Int -> findInt(value!!)?.let { return it }
                ?: throw InvalidYamlContent("Not an integer: $value")
            is Binary -> {
                Value(Base64.decode(value!!), tokenType)
            }
            is TimeStamp -> {
                findTimestamp(value!!)
            }
            else -> Value(value, tokenType)
        }
    } ?: if (value == null) {
        NullValue
    } else {
        if (isPlainStringReader) {
            return when (value) {
                in nullValues -> NullValue
                in trueValues -> Value(true, Bool)
                in falseValues -> Value(false, Bool)
                in nanValues -> Value(Double.NaN, ValueType.Float)
                else -> {
                    findInfinity(value)?.let { return it }
                    findInt(value)?.let { return it }
                    findFloat(value)?.let { return it }
                    findTimestamp(value)?.let { return it }
                    Value(value, ValueType.String)
                }
            }
        }

        Value(value, ValueType.String)
    }
}

/** Tries to find infinity value in [value] and returns a Value with infinity if found */
private fun findInfinity(value: String): Value<Double>? {
    infinityRegEx.find(value)?.let {
        return if (value.startsWith("-")) {
            Value(Double.NEGATIVE_INFINITY, ValueType.Float)
        } else {
            Value(Double.POSITIVE_INFINITY, ValueType.Float)
        }
    }
    return null
}

/** Tries to find Integer value in [value] and returns a Value with a Long if found */
private fun findInt(value: String): Value<Long>? {
    val minus = if (value.startsWith('-')) {
        -1
    } else {
        1
    }
    base2RegEx.find(value)?.let {
        val result = it.groupValues[1].replace("_", "").toLong(2)

        return Value(
            result * minus,
            ValueType.Int
        )
    }
    base8RegEx.find(value)?.let {
        return Value(
            value.replace("_", "").toLong(8),
            ValueType.Int
        )
    }
    base10RegEx.find(value)?.let {
        return Value(
            value.replace("_", "").toLong(10),
            ValueType.Int
        )
    }
    base16RegEx.find(value)?.let {
        val result = it.groupValues[1].replace("_", "").toLong(16)
        return Value(
            result * minus,
            ValueType.Int
        )
    }
    base60RegEx.find(value)?.let {
        val segments = value.replace("_", "").split(':')
        var result = 0L
        val power = segments.size - 1
        segments.forEachIndexed { index, segment ->
            result += (segment.toInt() * 60.0.pow(power - index)).toLong()
        }
        return Value(result, ValueType.Int)
    }
    return null
}

/** Tries to find float value in [value] and returns a Double if found */
private fun findFloat(value: String): Value<Double>? {
    floatRegEx.find(value)?.let {
        return value.replace("_", "").toDoubleOrNull()?.let { double ->
            Value(
                double,
                ValueType.Float
            )
        }
    }
    return null
}

/** Tries to find timestamp in [value] and returns a DateTime if found */
private fun findTimestamp(value: String): Value<DateTime>? {
    timestampRegex.find(value)?.let {
        return if (it.groups[4] == null) {
            Value(
                DateTime(
                    it.groups[1]!!.value.toInt(),
                    it.groups[2]!!.value.toByte(),
                    it.groups[3]!!.value.toByte()
                ),
                TimeStamp
            )
        } else {
            val dateTime =
                if (it.groups[11] == null || it.groups[11]!!.value == "Z" || it.groups[11]!!.value.isEmpty()) {
                    DateTime(
                        it.groups[1]!!.value.toInt(),
                        it.groups[2]!!.value.toByte(),
                        it.groups[3]!!.value.toByte(),
                        it.groups[6]!!.value.toByte(),
                        it.groups[7]!!.value.toByte(),
                        it.groups[8]!!.value.toByte(),
                        it.groups[10]?.value?.let { value ->
                            when {
                                value.length < 3 -> {
                                    var longer = value
                                    for (i in 1..3 - value.length) {
                                        longer += "0"
                                    }
                                    longer
                                }
                                value.length == 3 -> value
                                else -> value.substring(0, 3)
                            }
                        }?.toShort() ?: 0
                    )
                } else {
                    DateTime.parse(value)
                }

            Value(dateTime, TimeStamp)
        }
    }
    return null
}
