package language

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import language.lexer.LexerStateManager
import language.streams.FileInputStream
import language.streams.ScannerInputStream
import language.streams.StringInputStream
import language.streams.StringLiteralInputStream

fun main() = runBlocking<Unit>{
    val file = FileInputStream("test.bg")
    val lexerStateManager = LexerStateManager(file)

    launch{
        lexerStateManager.start()
        lexerStateManager.module.tokenStream.close()
    }
    launch{
        lexerStateManager.module.tokenStream.consumeEach {
            println(it)
        }
    }
}