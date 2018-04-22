package maryk.test

import kotlin.test.assertTrue

inline fun <reified T> shouldBeOfType(value: Any): T {
    assertTrue("expected: ${T::class.simpleName} but was: $value") {
        value is T
    }
    return value as T
}