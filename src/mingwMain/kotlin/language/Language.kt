package language

import language.lexer.Lexer
import language.lexer.registerTokens
import language.streams.FileInputStream
import language.streams.StringLiteralInputStream

fun main() {
    registerTokens()
    val file = FileInputStream("test.bg")
    val lexer = Lexer(file, file.path)
    val tokens = lexer.getTokens()
    if(lexer.errors.size > 0){
        for(error in lexer.errors){
            println(error)
        }
    }else{
        tokens.forEach{
            println(it.toString())
        }
    }
}