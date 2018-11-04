package maryk.yaml

import kotlin.test.Test

class ReservedIndicatorReaderTest {
    @Test
    fun fail_on_reserved_chars() {
        createYamlReader("`test").apply {
            assertInvalidYaml()
        }

        createYamlReader("@test").apply {
            assertInvalidYaml()
        }
    }

    @Test
    fun fail_on_reserved_sign_in_map() {
        createYamlReader("test: @test").apply {
            assertStartObject()
            assertFieldName("test")
            assertInvalidYaml()
        }
    }

    @Test
    fun fail_on_reserved_sign_in_array() {
        createYamlReader(" - `test").apply {
            assertStartArray()
            assertInvalidYaml()
        }
    }
}