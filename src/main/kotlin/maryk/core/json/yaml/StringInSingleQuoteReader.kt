package maryk.core.json.yaml

import maryk.core.json.InvalidJsonContent
import maryk.core.json.JsonToken

/** Last char is already at '. Read until next ' */
internal class StringInSingleQuoteReader(
    yamlReader: YamlReader,
    parentReader: YamlCharReader,
    private val jsonTokenConstructor: (String?) -> JsonToken
) : YamlCharReader(yamlReader, parentReader) {
    private var aQuoteFound = false
    private var storedValue: String? = ""

    override fun readUntilToken(): JsonToken {
        read()
        loop@while(true) {
            if(lastChar == '\'') {
                if (this.aQuoteFound) {
                    this.storedValue += lastChar
                    this.aQuoteFound = false
                } else {
                    this.aQuoteFound = true
                }
            } else {
                if (this.aQuoteFound) {
                    break@loop
                } else {
                    this.storedValue += lastChar
                }
            }
            read()
        }

        currentReader = this.parentReader!!

        return this.jsonTokenConstructor(storedValue)
    }

    override fun handleReaderInterrupt(): JsonToken {
        if (this.aQuoteFound) {
            currentReader = EndReader(this.yamlReader)
            return this.jsonTokenConstructor(storedValue)
        } else {
            throw InvalidJsonContent("Single quoted string was never closed")
        }
    }
}