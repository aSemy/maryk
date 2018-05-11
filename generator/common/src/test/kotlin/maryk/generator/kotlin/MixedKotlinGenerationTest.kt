package maryk.generator.kotlin

import maryk.CompleteMarykObject
import maryk.EmbeddedMarykObject
import maryk.MarykEnum
import maryk.SimpleMarykObject
import maryk.ValueMarykObject
import maryk.test.shouldBe
import kotlin.test.Test
import kotlin.test.fail

class MixedKotlinGenerationTest {
    @Test
    fun generate_mixed_maryk_primitives() {
        val mapOfWriters = mutableMapOf(
            "MarykEnum" to Writer(),
            "SimpleMarykObject" to Writer(),
            "EmbeddedMarykObject" to Writer(),
            "CompleteMarykObject" to Writer(),
            "ValueMarykObject" to Writer()
        )

        val setOfNames = mutableSetOf<String>()

        generateKotlin("maryk",
            MarykEnum,
            SimpleMarykObject,
            EmbeddedMarykObject,
            CompleteMarykObject,
            ValueMarykObject
        ) { name ->
            setOfNames.add(name)
            val writer = mapOfWriters[name]
                    ?: fail("Called for not known writer $name")
            writer::writer
        }

        setOfNames.size shouldBe 5

        mapOfWriters["MarykEnum"]!!.output shouldBe generatedKotlinForEnum
        mapOfWriters["SimpleMarykObject"]!!.output shouldBe generatedKotlinForSimpleDataModel
        mapOfWriters["EmbeddedMarykObject"]!!.output shouldBe generatedKotlinForEmbeddedDataModel
        mapOfWriters["CompleteMarykObject"]!!.output shouldBe generatedKotlinForCompleteDataModel
        mapOfWriters["ValueMarykObject"]!!.output shouldBe generatedKotlinForValueDataModel
    }
}

private class Writer(
    var output: String = ""
) {
    fun writer(input: String) {
        this.output += input
    }
}
