package language

import language.lexer.Lexer
import language.lexer.registerTokenBuilders
import language.streams.StringLiteralInputStream

fun main() {
    registerTokenBuilders()
    val lexer = Lexer(StringLiteralInputStream("def test = 5\ntest == 5"))
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