@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package maryk.core.models

import maryk.EmbeddedMarykModel
import maryk.Option
import maryk.TestMarykModel
import maryk.TestValueObject
import maryk.core.properties.exceptions.InvalidValueException
import maryk.core.properties.exceptions.OutOfRangeException
import maryk.core.properties.exceptions.ValidationUmbrellaException
import maryk.core.properties.types.TypedValue
import maryk.core.protobuf.WriteCache
import maryk.json.JsonReader
import maryk.json.JsonWriter
import maryk.lib.extensions.initByteArrayByHex
import maryk.lib.extensions.toHex
import maryk.lib.time.Date
import maryk.lib.time.DateTime
import maryk.lib.time.Time
import maryk.test.ByteCollector
import maryk.test.shouldBe
import maryk.test.shouldThrow
import maryk.yaml.YamlWriter
import kotlin.test.Test

val testMarykModelObject = TestMarykModel(
    string = "haas",
    int = 4,
    uint = 53u,
    double = 3.5555,
    bool = true,
    dateTime = DateTime(year = 2017, month = 12, day = 5, hour = 12, minute = 40)
)

val testExtendedMarykModelObject = TestMarykModel(
    string = "hay",
    int = 4,
    double = 3.555,
    dateTime = DateTime(year = 2017, month = 12, day = 4, hour = 12, minute = 13),
    uint = 32u,
    bool = true,
    list = listOf(34, 2352, 3423, 766),
    set = setOf(
        Date(2017, 12, 5),
        Date(2016, 3, 2),
        Date(1981, 12, 5)
    ),
    map = mapOf(
        Time(12,55) to "yes",
        Time(10, 3) to "ahum"
    ),
    valueObject = TestValueObject(6, DateTime(2017, 4, 1, 12, 55), true),
    embeddedValues = EmbeddedMarykModel("test"),
    multi = TypedValue(Option.V3, EmbeddedMarykModel("subInMulti!")),
    listOfString = listOf("test1", "another test", "🤗")
)

private const val JSON = "{\"string\":\"hay\",\"int\":4,\"uint\":32,\"double\":\"3.555\",\"dateTime\":\"2017-12-04T12:13\",\"bool\":true,\"enum\":\"V1\",\"list\":[34,2352,3423,766],\"set\":[\"2017-12-05\",\"2016-03-02\",\"1981-12-05\"],\"map\":{\"12:55\":\"yes\",\"10:03\":\"ahum\"},\"valueObject\":{\"int\":6,\"dateTime\":\"2017-04-01T12:55\",\"bool\":true},\"embeddedValues\":{\"value\":\"test\"},\"multi\":[\"V3\",{\"value\":\"subInMulti!\"}],\"listOfString\":[\"test1\",\"another test\",\"\uD83E\uDD17\"]}"

// Test if unknown values will be skipped
private const val PRETTY_JSON_WITH_SKIP = """{
	"string": "hay",
	"int": 4,
	"uint": 32,
	"double": "3.555",
	"bool": true,
	"dateTime": "2017-12-04T12:13",
	"enum": "V1",
	"list": [34, 2352, 3423, 766],
	"set": ["2017-12-05", "2016-03-02", "1981-12-05"],
	"map": {
		"12:55": "yes",
		"10:03": "ahum"
	},
    "skipUnknown": "should be skipped as possible future property",
	"valueObject": {
		"int": 6,
		"dateTime": "2017-04-01T12:55",
		"bool": true
	},
	"embeddedValues": {
		"value": "test"
	},
	"multi": ["V3", {
		"value": "subInMulti!"
	}],
	"listOfString": ["test1", "another test", "🤗"]
}"""

internal class DataModelTest {
    @Test
    fun construct_by_map() {
        TestMarykModel.map {
            mapNonNulls(
                string with testMarykModelObject { string },
                int with testMarykModelObject { int },
                uint with testMarykModelObject { uint },
                double with testMarykModelObject { double },
                dateTime with testMarykModelObject { dateTime },
                bool with testMarykModelObject { bool },
                enum with testMarykModelObject { enum }
            )
        } shouldBe testMarykModelObject
    }

    @Test
    fun validate() {
        TestMarykModel.validate(testMarykModelObject)
    }

    @Test
    fun fail_validation_with_incorrect_values_in_DataObject() {
        shouldThrow<ValidationUmbrellaException> {
            TestMarykModel.validate(
                TestMarykModel(
                    string = "haas",
                    int = 9,
                    uint = 53u,
                    double = 3.5555,
                    bool = true,
                    dateTime = DateTime(year = 2017, month = 12, day = 5, hour = 12, minute = 40)
                )
            )
        }
    }

    @Test
    fun fail_validation_with_incorrect_values_in_map() {
        val e = shouldThrow<ValidationUmbrellaException> {
            TestMarykModel.validate(
                TestMarykModel.map {
                    mapNonNulls(
                        string with "wrong",
                        int with 999
                    )
                }
            )
        }

        e.exceptions.size shouldBe 2

        (e.exceptions[0] is InvalidValueException) shouldBe true
        (e.exceptions[1] is OutOfRangeException) shouldBe true
    }

    @Test
    fun get_property_definition_by_name() {
        TestMarykModel.properties["string"] shouldBe TestMarykModel.Properties.string
        TestMarykModel.properties["int"] shouldBe TestMarykModel.Properties.int
        TestMarykModel.properties["dateTime"] shouldBe TestMarykModel.Properties.dateTime
        TestMarykModel.properties["bool"] shouldBe TestMarykModel.Properties.bool
    }

    @Test
    fun get_property_definition_by_index() {
        TestMarykModel.properties[1] shouldBe TestMarykModel.Properties.string
        TestMarykModel.properties[2] shouldBe TestMarykModel.Properties.int
        TestMarykModel.properties[3] shouldBe TestMarykModel.Properties.uint
        TestMarykModel.properties[4] shouldBe TestMarykModel.Properties.double
        TestMarykModel.properties[5] shouldBe TestMarykModel.Properties.dateTime
        TestMarykModel.properties[6] shouldBe TestMarykModel.Properties.bool
    }

    @Test
    fun write_into_a_JSON_object() {
        var output = ""
        val writer = JsonWriter {
            output += it
        }

        TestMarykModel.writeJson(testExtendedMarykModelObject, writer)

        output shouldBe JSON
    }

    @Test
    fun write_into_a_pretty_JSON_object() {
        var output = ""
        val writer = JsonWriter(pretty = true) {
            output += it
        }

        TestMarykModel.writeJson(testExtendedMarykModelObject, writer)

        output shouldBe """
        {
        	"string": "hay",
        	"int": 4,
        	"uint": 32,
        	"double": "3.555",
        	"dateTime": "2017-12-04T12:13",
        	"bool": true,
        	"enum": "V1",
        	"list": [34, 2352, 3423, 766],
        	"set": ["2017-12-05", "2016-03-02", "1981-12-05"],
        	"map": {
        		"12:55": "yes",
        		"10:03": "ahum"
        	},
        	"valueObject": {
        		"int": 6,
        		"dateTime": "2017-04-01T12:55",
        		"bool": true
        	},
        	"embeddedValues": {
        		"value": "test"
        	},
        	"multi": ["V3", {
        		"value": "subInMulti!"
        	}],
        	"listOfString": ["test1", "another test", "🤗"]
        }
        """.trimIndent()
    }

    @Test
    fun write_into_a_YAML_object() {
        var output = ""
        val writer = YamlWriter {
            output += it
        }

        TestMarykModel.writeJson(testExtendedMarykModelObject, writer)

        output shouldBe """
        string: hay
        int: 4
        uint: 32
        double: 3.555
        dateTime: '2017-12-04T12:13'
        bool: true
        enum: V1
        list: [34, 2352, 3423, 766]
        set: [2017-12-05, 2016-03-02, 1981-12-05]
        map:
          12:55: yes
          10:03: ahum
        valueObject:
          int: 6
          dateTime: '2017-04-01T12:55'
          bool: true
        embeddedValues:
          value: test
        multi: !V3
          value: subInMulti!
        listOfString: [test1, another test, 🤗]

        """.trimIndent()
    }

    @Test
    fun write_to_ProtoBuf_bytes() {
        val bc = ByteCollector()
        val cache = WriteCache()

        val map = TestMarykModel.map {
            mapNonNulls(
                string with "hay",
                int with 4,
                uint with 32u,
                double with 3.555,
                dateTime with DateTime(year = 2017, month = 12, day = 4, hour = 12, minute = 13),
                bool with true,
                enum with Option.V3,
                reference with TestMarykModel.key(byteArrayOf(1, 5, 1, 5, 1, 5, 1, 5, 1))
            )
        }

        bc.reserve(
            TestMarykModel.calculateProtoBufLength(map, cache)
        )

        TestMarykModel.writeProtoBuf(map, cache, bc::write)

        bc.bytes!!.toHex() shouldBe "0a036861791008182021713d0ad7a3700c4028ccf794d105300138037209010501050105010501"
    }

    @Test
    fun convert_to_ProtoBuf_and_back() {
        val bc = ByteCollector()
        val cache = WriteCache()

        bc.reserve(
            TestMarykModel.calculateProtoBufLength(testExtendedMarykModelObject, cache)
        )

        TestMarykModel.writeProtoBuf(testExtendedMarykModelObject, cache, bc::write)

        bc.bytes!!.toHex() shouldBe "0a036861791008182021713d0ad7a3700c4028ccf794d10530013801420744e024be35fc0b4a08c29102bc87028844520908a4eb021203796573520a08d49a0212046168756d5a0e800000060180000058dfa324010162060a04746573746a0f1a0d0a0b737562496e4d756c7469217a0574657374317a0c616e6f7468657220746573747a04f09fa497"

        TestMarykModel.readProtoBuf(bc.size, bc::read) shouldBe testExtendedMarykModelObject
    }

    @Test
    fun skip_reading_unknown_fields() {
        val bytes = initByteArrayByHex("930408161205ffffffffff9404a20603686179a80608b00620b906400c70a3d70a3d72c80601d006028a07020105")
        var index = 0

        val map = TestMarykModel.readProtoBuf(bytes.size, {
            bytes[index++]
        })

        map.size shouldBe 0
    }

    @Test
    fun convert_from_JSON() {
        var input = ""
        var index = 0
        val reader = { input[index++] }
        val jsonReader = { JsonReader(reader = reader) }

        listOf(
            JSON,
            PRETTY_JSON_WITH_SKIP
        ).forEach { jsonInput ->
            input = jsonInput
            index = 0
            TestMarykModel.readJson(reader = jsonReader()) shouldBe testExtendedMarykModelObject
        }
    }

    @Test
    fun convert_map_to_JSON_and_back_to_map() {
        var output = ""
        val writer = { string: String -> output += string }

        listOf(
            JsonWriter(writer = writer),
            JsonWriter(pretty = true, writer = writer)
        ).forEach { generator ->
            TestMarykModel.writeJson(testExtendedMarykModelObject, generator)

            var index = 0
            val reader = { JsonReader(reader = { output[index++] }) }
            TestMarykModel.readJson(reader = reader()) shouldBe testExtendedMarykModelObject

            output = ""
        }
    }
}