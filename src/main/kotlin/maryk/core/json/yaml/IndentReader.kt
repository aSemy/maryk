package maryk.core.json.yaml

import maryk.core.extensions.isLineBreak
import maryk.core.json.JsonToken

/** Reads indents on a new line until a char is found */
internal class IndentReader<out P>(
    yamlReader: YamlReaderImpl,
    parentReader: P
) : YamlCharWithParentReader<P>(yamlReader, parentReader),
    IsYamlCharWithIndentsReader,
    IsYamlCharWithChildrenReader
        where P : maryk.core.json.yaml.YamlCharReader,
              P : maryk.core.json.yaml.IsYamlCharWithChildrenReader,
              P : maryk.core.json.yaml.IsYamlCharWithIndentsReader
{
    private var indentCounter = -1
    private var mapKeyFound: Boolean = false

    // Should not be called
    override fun <P> newIndentLevel(parentReader: P): JsonToken
            where P : YamlCharReader,
                  P : IsYamlCharWithChildrenReader,
                  P : IsYamlCharWithIndentsReader =
        this.parentReader.newIndentLevel(parentReader)

    override fun continueIndentLevel() =
        LineReader(this.yamlReader, this).let {
            this.currentReader = it
            it.readUntilToken()
        }

    override fun foundMapKey(): JsonToken? =
        if (!this.mapKeyFound) {
            this.mapKeyFound = true
            JsonToken.StartObject
        } else {
            null
        }

    override fun endIndentLevel(indentCount: Int, tokenToReturn: JsonToken?): JsonToken {
        this.yamlReader.hasUnclaimedIndenting(indentCount)

        if (this.mapKeyFound) {
            this.mapKeyFound = false
            return JsonToken.EndObject
        }

        this.parentReader.childIsDoneReading()
        return tokenToReturn ?: this.currentReader.readUntilToken()
    }

    override fun readUntilToken(): JsonToken {
        var currentIndentCount = 0
        while(this.lastChar.isWhitespace()) {
            if (this.lastChar.isLineBreak()) {
                currentIndentCount = 0
            } else {
                currentIndentCount++
            }
            read()
        }

        if (this.indentCounter == -1) {
            this.indentCounter = currentIndentCount
        }

        val parentIndentCount = this.parentReader.indentCount()
        return when(currentIndentCount) {
            parentIndentCount -> this.parentReader.continueIndentLevel()
            in 0 until parentIndentCount -> {
                if (this.mapKeyFound) {
                    this.mapKeyFound = false
                    this.parentReader.childIsDoneReading()
                    return this.parentReader.endIndentLevel(
                        currentIndentCount,
                        tokenToReturn = JsonToken.EndObject
                    )
                }

                this.parentReader.endIndentLevel(currentIndentCount)
            }
            else -> this.parentReader.newIndentLevel(this)
        }
    }

    override fun indentCount() = this.indentCounter

    override fun indentCountForChildren() = this.indentCount()

    override fun childIsDoneReading() {
        this.currentReader = this
    }

    override fun handleReaderInterrupt(): JsonToken {
        if (this.mapKeyFound) {
            this.mapKeyFound = false
            return JsonToken.EndObject
        }
        return parentReader.handleReaderInterrupt()
    }
}