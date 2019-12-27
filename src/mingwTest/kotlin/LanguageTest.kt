import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import language.lexer.Lexer
import language.lexer.LexerStateManager
import language.streams.FileInputStream
import kotlin.test.Test

@Test
fun lexerTest() = runBlocking<Unit>{
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