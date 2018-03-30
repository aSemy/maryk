package maryk.core.json.yaml

import maryk.core.json.JsonToken
import maryk.core.json.TokenType

/** Last char is already at '. Read until next ' */
internal class StringInSingleQuoteReader<out P>(
    yamlReader: YamlReaderImpl,
    parentReader: P,
    private val jsonTokenConstructor: (String?) -> JsonToken
) : YamlCharWithParentReader<P>(yamlReader, parentReader)
        where P : YamlCharReader,
              P : IsYamlCharWithChildrenReader
{
    private var aQuoteFound = false
    private var storedValue: String? = ""

    override fun readUntilToken(tag: TokenType?): JsonToken {
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

        this.parentReader.childIsDoneReading(false)

        return this.jsonTokenConstructor(storedValue)
    }

    override fun handleReaderInterrupt(): JsonToken {
        if (this.aQuoteFound) {
            this.parentReader.childIsDoneReading(false)
            return this.jsonTokenConstructor(storedValue)
        } else {
            throw InvalidYamlContent("Single quoted string was never closed")
        }
    }
}

fun <P> P.singleQuoteString(tag: TokenType?, jsonTokenCreator: JsonTokenCreator): JsonToken
        where P : IsYamlCharWithChildrenReader,
              P : YamlCharReader,
              P : IsYamlCharWithIndentsReader {
    read()
    return StringInSingleQuoteReader(this.yamlReader, this, {
        jsonTokenCreator(it, false, tag)
    }).let {
        this.currentReader = it
        it.readUntilToken(tag)
    }
}