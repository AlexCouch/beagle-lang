package language

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import language.lexer.Lexer
import language.lexer.LexerStateManager
import language.lexer.Token
import language.streams.FileInputStream

fun main() = runBlocking<Unit>{
    val file = FileInputStream("test.bg")
    val lexerStateManager = LexerStateManager(file)
    launch{
        lexerStateManager.start()
        lexerStateManager.collectTokens().forEach {
            println(it)
        }
    }
}