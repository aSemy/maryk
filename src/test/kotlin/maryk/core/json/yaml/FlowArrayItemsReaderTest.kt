package maryk.core.json.yaml

import maryk.core.json.testForArrayEnd
import maryk.core.json.testForArrayStart
import maryk.core.json.testForArrayValue
import maryk.core.json.testForEndJson
import maryk.core.json.testForInvalidJson
import kotlin.test.Test

class FlowArrayItemsReaderTest {
    @Test
    fun read_array_items() {
        val reader = createYamlReader("""
            |     - ["test1", "test2", "test3"]
        """.trimMargin())
        testForArrayStart(reader)
        testForArrayStart(reader)
        testForArrayValue(reader, "test1")
        testForArrayValue(reader, "test2")
        testForArrayValue(reader, "test3")
        testForArrayEnd(reader)
        testForArrayEnd(reader)
        testForEndJson(reader)
    }

    @Test
    fun read_array_items_plain() {
        val reader = createYamlReader("""
            |     - [test1, test2, test3]
        """.trimMargin())
        testForArrayStart(reader)
        testForArrayStart(reader)
        testForArrayValue(reader, "test1")
        testForArrayValue(reader, "test2")
        testForArrayValue(reader, "test3")
        testForArrayEnd(reader)
        testForArrayEnd(reader)
        testForEndJson(reader)
    }

    @Test
    fun read_array_items_plain_multiline() {
        val reader = createYamlReader("""
            |     - [test1
            |      longer
            |      and longer,
            |       test2,
            |       test3]
        """.trimMargin())
        testForArrayStart(reader)
        testForArrayStart(reader)
        testForArrayValue(reader, "test1 longer and longer")
        testForArrayValue(reader, "test2")
        testForArrayValue(reader, "test3")
        testForArrayEnd(reader)
        testForArrayEnd(reader)
        testForEndJson(reader)
    }

    @Test
    fun read_array_items_plain_wrong_multiline() {
        val reader = createYamlReader("""
            |     - [test1
            |     wrong]
        """.trimMargin())
        testForArrayStart(reader)
        testForArrayStart(reader)
        testForInvalidJson(reader)
    }

    @Test
    fun read_array_with_whitespacing_items() {
        val reader = createYamlReader("""
            |     - ["test1"    ,    "test2",
            |"test3"  ]
        """.trimMargin())
        testForArrayStart(reader)
        testForArrayStart(reader)
        testForArrayValue(reader, "test1")
        testForArrayValue(reader, "test2")
        testForArrayValue(reader, "test3")
        testForArrayEnd(reader)
        testForArrayEnd(reader)
        testForEndJson(reader)
    }
}