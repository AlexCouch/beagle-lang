package language.lexer

import language.error.ErrorEntry
import language.error.ErrorManager
import language.serializer.prettyprint.PrettyPrinter
import language.serializer.prettyprint.buildPrettyString

class LexerErrorManager(override val module: Lexer): ErrorManager<Lexer>() {
    override suspend fun createError(moduleName: String, message: String) {
        val errorMessage = buildPrettyString {
            this.appendWithNewLine("Lexer failed during analysis")
            this.indent {
                this.appendWithNewLine(moduleName)
                this.appendWithNewLine(message)
            }
        }
        this.errorStream.send(ErrorEntry("Lexer", errorMessage))
    }

}