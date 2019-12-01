package language.lexer

import kotlin.native.concurrent.ThreadLocal

data class TokenLocation(val fileName: String, val line: Int, val column: Int)

enum class Tokens(val tokenName: String){
    DefToken("DEFINITION"),
    IdentToken("IDENTIFIER"),
    EqualSignToken("EQUAL_SIGN"),
    IntegerToken("INTEGER_LITERAL"),
    IllegalToken("ILLEGAL"),
    EOFToken("EOF")
}

data class Token(val tokenType: Tokens, val lexeme: String, val tokenLocation: TokenLocation){
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(tokenType.tokenName)
        sb.append("{\n")
        sb.append("\tValue: $lexeme\n")
        sb.append("\tFile: ${this.tokenLocation.fileName}\n")
        sb.append("\tLine: ${this.tokenLocation.line}\n")
        sb.append("\tColumn: ${this.tokenLocation.column}\n")
        sb.append("}")
        return sb.toString()
    }
}

@ThreadLocal
object TokenBuilder{
    val tokenRegistry = hashMapOf<Regex, Tokens>()

    fun createToken(currentLexeme: String, location: TokenLocation): Token{
        for((regex, token) in this.tokenRegistry){
            if(regex.matches(currentLexeme)){
                return Token(token, currentLexeme, location)
            }
        }
        return Token(Tokens.IllegalToken, currentLexeme, location)
    }
}

fun registerTokens(){
    TokenBuilder.tokenRegistry[Regex("def")] = Tokens.DefToken
    TokenBuilder.tokenRegistry[Regex("=")] = Tokens.EqualSignToken
    TokenBuilder.tokenRegistry[Regex("[0-9]+")] = Tokens.IntegerToken
}