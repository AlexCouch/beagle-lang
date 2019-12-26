package language.lexer

data class TokenLocation(val fileName: String, val line: Int, val column: Int)

enum class KeywordTokenType(val symbol: String, val tokenName: String){
    DefToken("def", "Definition"),
    FunctionToken("fun", "Function")
}

enum class DelimitingTokenType(val symbol: String, val tokenName: String){
    EqualSignToken("=", "EqualSign"),
    ColonToken(":", "Colon"),
    LeftParenToken("\\(", "LeftParenthesis"),
    RightParenToken("\\)", "RightParenthesis"),
    LeftBraceToken("\\{", "LeftBrace"),
    RightBraceToken("\\}", "RightBrace"),
    PlusSignToken("\\+", "PlusSign"),
    MinusSignToken("\\-", "MinusSign")
}

enum class OtherTokenType(val tokenName: String){
    IdentifierToken("Identifier"),
    IntegerLiteralToken("IntegerLiteral"),
    EndOfFileToken("EndOfFile")
}

sealed class Token(open val tokenLocation: TokenLocation){
    data class KeywordToken(val tokenType: KeywordTokenType, override val tokenLocation: TokenLocation): Token(tokenLocation){
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(tokenType.tokenName)
            sb.append("{\n")
            sb.append("\tFile: ${this.tokenLocation.fileName}\n")
            sb.append("\tLine: ${this.tokenLocation.line}\n")
            sb.append("\tColumn: ${this.tokenLocation.column}\n")
            sb.append("}")
            return sb.toString()
        }
    }
    data class DelimitingToken(val tokenType: DelimitingTokenType, override val tokenLocation: TokenLocation): Token(tokenLocation){
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(tokenType.tokenName)
            sb.append("{\n")
            sb.append("\tFile: ${this.tokenLocation.fileName}\n")
            sb.append("\tLine: ${this.tokenLocation.line}\n")
            sb.append("\tColumn: ${this.tokenLocation.column}\n")
            sb.append("}")
            return sb.toString()
        }
    }
    sealed class OtherToken(open val tokenType: OtherTokenType, override val tokenLocation: TokenLocation): Token(tokenLocation){
        data class IdentifierToken(val symbol: String, override val tokenLocation: TokenLocation): OtherToken(OtherTokenType.IdentifierToken, tokenLocation){
            override fun toString(): String {
                val sb = StringBuilder()
                sb.append(tokenType.tokenName)
                sb.append("{\n")
                sb.append("\tSymbol: ${this.symbol}\n")
                sb.append("\tFile: ${this.tokenLocation.fileName}\n")
                sb.append("\tLine: ${this.tokenLocation.line}\n")
                sb.append("\tColumn: ${this.tokenLocation.column}\n")
                sb.append("}")
                return sb.toString()
            }
        }
        data class IntegerLiteralToken(val symbol: String, override val tokenLocation: TokenLocation): OtherToken(OtherTokenType.IntegerLiteralToken, tokenLocation){
            override fun toString(): String {
                val sb = StringBuilder()
                sb.append(tokenType.tokenName)
                sb.append("{\n")
                sb.append("\tSymbol: ${this.symbol}\n")
                sb.append("\tFile: ${this.tokenLocation.fileName}\n")
                sb.append("\tLine: ${this.tokenLocation.line}\n")
                sb.append("\tColumn: ${this.tokenLocation.column}\n")
                sb.append("}")
                return sb.toString()
            }
        }
        data class EndOfFileToken(override val tokenLocation: TokenLocation): OtherToken(OtherTokenType.EndOfFileToken, tokenLocation){
            override fun toString(): String {
                val sb = StringBuilder()
                sb.append(tokenType.tokenName)
                sb.append("{\n")
                sb.append("\tFile: ${this.tokenLocation.fileName}\n")
                sb.append("\tLine: ${this.tokenLocation.line}\n")
                sb.append("\tColumn: ${this.tokenLocation.column}\n")
                sb.append("}")
                return sb.toString()
            }
        }
    }
}