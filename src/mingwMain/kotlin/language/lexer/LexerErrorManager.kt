package language.lexer

import language.error.ErrorEntry
import language.error.ErrorManager

class LexerErrorManager(override val module: Lexer): ErrorManager<Lexer>() {
    override suspend fun createError(moduleName: String, message: String) {
        val errorMessage = StringBuilder()
        errorMessage.append("Lexer failed during analysis\n")
        errorMessage.append("\tLine: ${module.lineIdx}\n")
        errorMessage.append("\tColumn: ${module.column}\n")
        errorMessage.append("\tFile: ${module.filePath}\n")
        errorMessage.append("\tMessage:\n")
        message.split("\n").withIndex().forEach { (index, str) ->
            val messageModifier = StringBuilder()
            (0 until index).forEach{_ ->
                messageModifier.append("\t")
            }
            messageModifier.append("$str\n")
        }
        errorMessage.append("\t\t$message")
        this.errorStream.send(ErrorEntry("Lexer", errorMessage.toString()))
    }

}