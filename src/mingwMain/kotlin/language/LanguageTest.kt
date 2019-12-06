package language

import kotlinx.io.core.ExperimentalIoApi
import kotlinx.io.streams.Input
import language.lexer.Lexer
import language.streams.FileInputStream

@ExperimentalIoApi
fun main(){
    val file = FileInputStream("test.bg")
    val lexer = Lexer(file)
    val tokens = lexer.getTokens()
    tokens.forEach {
        println(it.toString())
    }
}