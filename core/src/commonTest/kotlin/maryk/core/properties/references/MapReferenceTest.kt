package maryk.core.properties.references

import maryk.core.exceptions.UnexpectedValueException
import maryk.core.processors.datastore.matchers.FuzzyExactLengthMatch
import maryk.core.processors.datastore.matchers.QualifierExactMatcher
import maryk.core.processors.datastore.matchers.QualifierFuzzyMatcher
import maryk.core.protobuf.WriteCache
import maryk.lib.extensions.toHex
import maryk.lib.time.Time
import maryk.test.ByteCollector
import maryk.test.models.TestMarykModel
import maryk.test.shouldBe
import maryk.test.shouldThrow
import kotlin.test.Test

class MapReferenceTest {
    private val mapReference = TestMarykModel.ref { map }
    private val keyReference = TestMarykModel { map refToKey Time(12, 0, 1) }
    private val valReference = TestMarykModel { map refAt Time(15, 22, 55) }
    private val anyReference = TestMarykModel { map.refToAny() }

    private val subReference = TestMarykModel { embeddedValues { marykModel { map refAt Time(15, 22, 55) } } }
    private val subKeyReference = TestMarykModel { embeddedValues { marykModel { map refToKey Time(15, 22, 55) } } }
    private val subAnyReference = TestMarykModel { embeddedValues { marykModel { map.refToAny() } } }

    @Test
    fun getValueFromMap() {
        val map = mapOf(
            Time(12, 0, 1) to "right",
            Time(15, 22, 55) to "right2",
            Time(0, 0, 1) to "wrong",
            Time(2, 14, 52) to "wrong again"
        )

        this.keyReference.resolveFromAny(map) shouldBe Time(12, 0, 1)
        this.valReference.resolveFromAny(map) shouldBe "right2"

        shouldThrow<UnexpectedValueException> {
            this.keyReference.resolveFromAny("wrongInput")
        }
    }

    @Test
    fun convertToProtoBufAndBack() {
        val bc = ByteCollector()
        val cache = WriteCache()

        for (it in arrayOf(mapReference, keyReference, valReference, anyReference)) {
            bc.reserve(
                it.calculateTransportByteLength(cache)
            )
            it.writeTransportBytes(cache, bc::write)

            val converted = TestMarykModel.getPropertyReferenceByBytes(bc.size, bc::read)
            converted shouldBe it
            bc.reset()
        }
    }

    @Test
    fun testStringConversion() {
        mapReference.completeName shouldBe "map"
        keyReference.completeName shouldBe "map.\$12:00:01"
        valReference.completeName shouldBe "map.@15:22:55"
        anyReference.completeName shouldBe "map.*"

        for (it in arrayOf(mapReference, keyReference, valReference, anyReference)) {
            val converted = TestMarykModel.getPropertyReferenceByName(it.completeName)
            converted shouldBe it
        }
    }

    @Test
    fun writeAndReadMapRefStorageBytes() {
        val bc = ByteCollector()

        bc.reserve(
            mapReference.calculateStorageByteLength()
        )
        mapReference.writeStorageBytes(bc::write)

        bc.bytes!!.toHex() shouldBe "54"

        TestMarykModel.Properties.getPropertyReferenceByStorageBytes(bc.size, bc::read) shouldBe mapReference
    }

    @Test
    fun writeAndReadValueRefStorageBytes() {
        val bc = ByteCollector()

        bc.reserve(
            valReference.calculateStorageByteLength()
        )
        valReference.writeStorageBytes(bc::write)

        bc.bytes!!.toHex() shouldBe "540300d84f"

        TestMarykModel.Properties.getPropertyReferenceByStorageBytes(bc.size, bc::read) shouldBe valReference
    }

    @Test
    fun writeAndReadDeepValueRefStorageBytes() {
        val bc = ByteCollector()

        bc.reserve(
            subReference.calculateStorageByteLength()
        )
        subReference.writeStorageBytes(bc::write)

        bc.bytes!!.toHex() shouldBe "661e540300d84f"

        TestMarykModel.Properties.getPropertyReferenceByStorageBytes(bc.size, bc::read) shouldBe subReference
    }

    @Test
    fun writeAndReadAnyRefStorageBytes() {
        val bc = ByteCollector()

        bc.reserve(
            anyReference.calculateStorageByteLength()
        )
        anyReference.writeStorageBytes(bc::write)

        bc.bytes!!.toHex() shouldBe "100a00"

        TestMarykModel.Properties.getPropertyReferenceByStorageBytes(bc.size, bc::read) shouldBe anyReference
    }

    @Test
    fun createAnyRefQualifierMatcher() {
        val matcher = anyReference.toQualifierMatcher()

        (matcher is QualifierFuzzyMatcher) shouldBe true
        (matcher as QualifierFuzzyMatcher).let {
            it.firstPossible().toHex() shouldBe "54"
            it.qualifierParts.size shouldBe 1
            it.fuzzyMatchers.size shouldBe 1

            it.fuzzyMatchers.first().let { matcher ->
                (matcher is FuzzyExactLengthMatch) shouldBe true
                (matcher as FuzzyExactLengthMatch).length shouldBe 3
            }
        }
    }

    @Test
    fun writeAndReadKeyRefStorageBytes() {
        val bc = ByteCollector()

        bc.reserve(
            keyReference.calculateStorageByteLength()
        )
        keyReference.writeStorageBytes(bc::write)

        bc.bytes!!.toHex() shouldBe "080a0300a8c1"

        TestMarykModel.Properties.getPropertyReferenceByStorageBytes(bc.size, bc::read) shouldBe keyReference
    }

    @Test
    fun createKeyRefQualifierMatcher() {
        val matcher = keyReference.toQualifierMatcher()

        (matcher is QualifierExactMatcher) shouldBe true
        (matcher as QualifierExactMatcher).qualifier.toHex() shouldBe "080a0300a8c1"
    }

    @Test
    fun writeAndReadDeepKeyRefStorageBytes() {
        val bc = ByteCollector()

        bc.reserve(
            subKeyReference.calculateStorageByteLength()
        )
        subKeyReference.writeStorageBytes(bc::write)

        bc.bytes!!.toHex() shouldBe "661e080a0300d84f"

        TestMarykModel.Properties.getPropertyReferenceByStorageBytes(bc.size, bc::read) shouldBe subKeyReference
    }

    @Test
    fun writeAndReadDeepAnyRefStorageBytes() {
        val bc = ByteCollector()

        bc.reserve(
            subAnyReference.calculateStorageByteLength()
        )
        subAnyReference.writeStorageBytes(bc::write)

        bc.bytes!!.toHex() shouldBe "661e100a00"

        TestMarykModel.Properties.getPropertyReferenceByStorageBytes(bc.size, bc::read) shouldBe subAnyReference
    }
}
