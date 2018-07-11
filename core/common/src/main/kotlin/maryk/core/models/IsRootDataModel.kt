package maryk.core.models

import maryk.core.extensions.bytes.initByteArray
import maryk.core.properties.IsPropertyDefinitions
import maryk.core.properties.types.Key

interface IsRootDataModel<P: IsPropertyDefinitions> : IsNamedDataModel<P> {
    val keySize: Int

    /** Get Key by [base64] bytes as string representation */
    fun key(base64: String): Key<*>

    /** Get Key by byte [reader] */
    fun key(reader: () -> Byte): Key<*> = Key<Any>(
        initByteArray(this.keySize, reader)
    )
}