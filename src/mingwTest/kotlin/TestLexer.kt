import kotlinx.cinterop.*
import language.lexer.Lexer
import language.streams.ScannerInputStream
import platform.posix.scanf
import kotlin.test.Test

@Test
fun lexerGetTokensTest(){
//    val scannerInputStream = ScannerInputStream()
    memScoped {
        do {
            val input = readLine()
            check(input != null){
                val lexer = Lexer(input!!)
                val tokens = lexer.getTokens()
                if (lexer.errors.size > 0) {
                    lexer.errors.forEach {
                        println(it)
                    }
                    return
                }
                tokens.forEach {
                    println(it.toString())
                }
            }
        } while (input != "exit")
    }
}