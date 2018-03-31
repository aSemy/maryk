package maryk.core.json.yaml

import maryk.core.json.JsonToken
import maryk.core.json.TokenType

/** Reads tags and returns reading when done */
internal class TagReader<out P>(
    yamlReader: YamlReaderImpl,
    parentReader: P
) : YamlCharWithParentReader<P>(yamlReader, parentReader)
        where P : YamlCharReader,
              P : IsYamlCharWithChildrenReader,
              P : IsYamlCharWithIndentsReader
{
    override fun readUntilToken(extraIndent: Int, tag: TokenType?): JsonToken {
        read()

        var prefix = ""
        var newTag = ""

        while(!this.lastChar.isWhitespace()) {
            if(this.lastChar == '!') {
                // Double !!
                if(prefix.isEmpty()) {
                    prefix = "!$newTag!"
                    newTag = ""
                } else {
                    throw InvalidYamlContent("Invalid tag $newTag")
                }
            } else {
                newTag += this.lastChar
            }
            read()
        }
        // Single !
        if(prefix.isEmpty()) {
            prefix = "!"
        }

        this.parentReader.childIsDoneReading(false)
        return this.parentReader.continueIndentLevel(
            extraIndent,
            this.yamlReader.resolveTag(prefix, newTag)
        )
    }

    override fun handleReaderInterrupt(): JsonToken {
        this.parentReader.childIsDoneReading(false)
        return this.parentReader.handleReaderInterrupt()
    }
}

internal fun <P> P.tagReader(extraIndent: Int)
        where P : IsYamlCharWithChildrenReader,
              P : YamlCharReader,
              P : IsYamlCharWithIndentsReader =
    TagReader(this.yamlReader, this).let {
        this.currentReader = it
        it.readUntilToken(extraIndent)
    }
