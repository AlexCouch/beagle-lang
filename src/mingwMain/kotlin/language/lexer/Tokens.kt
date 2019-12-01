package language.lexer

import kotlin.native.concurrent.ThreadLocal

enum class Tokens(val tokenName: String){
    DefToken("DEFINITION"),
    IdentToken("IDENTIFIER"),
    EqualSignToken("EQUAL_SIGN"),
    IntegerToken("INTEGER_LITERAL"),
    IllegalToken("ILLEGAL"),
    EOFToken("EOF")
}

data class Token(val tokenType: Tokens, val line: Int, val column: Int){
    var value: String = ""

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(tokenType.tokenName)
        sb.append("{\n")
        sb.append("\tValue: $value\n")
        sb.append("\tLine: $line\n")
        sb.append("\tColumn: $column\n")
        sb.append("}")
        return sb.toString()
    }
}

data class TokenBuilderConditionResult(val tokenString: String, val result: Boolean)
typealias TokenBuilderCallback = Lexer.(String) -> Token
typealias TokenBuilderCondition = Lexer.() -> TokenBuilderConditionResult

@ThreadLocal
object TokenBuilder{
    private val tokenBuilders = hashMapOf<TokenBuilderCondition, TokenBuilderCallback>()

    fun putTokenBuilder(condition: TokenBuilderCondition, callback: TokenBuilderCallback){
        this.tokenBuilders[condition] = callback
    }

    fun nextToken(lexer: Lexer): Token?{
        for((condition, callback) in tokenBuilders){
            val conditionResult = lexer.condition()
            if(conditionResult.result){
                return lexer.callback(conditionResult.tokenString)
            }
        }
        return null
    }
}