import language.lexer.Lexer
import language.streams.FileInputStream
import kotlin.test.Test

@Test
fun lexerTest(){
    val file = FileInputStream("test.bg")
    val lexer = Lexer(file)
    val tokens = lexer.getTokens()
    tokens.forEach {
        println(it.toString())
    }
}