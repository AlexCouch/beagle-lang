package language.lexer

import language.serializer.prettyprint.PrettyPrinter
import language.serializer.prettyprint.buildPrettyString

data class TokenLocation(val fileName: String, val line: Int, val column: Int){
    override fun toString(): String {
        return buildPrettyString {
            this.appendWithNewLine("token location:")
            this.indent {
                this.appendWithNewLine("file name: ${this@TokenLocation.fileName}")
                this.appendWithNewLine("line: ${this@TokenLocation.line}")
                this.appendWithNewLine("column: ${this@TokenLocation.column}")
            }
        }
    }
}

enum class KeywordTokenType(val symbol: String, val tokenName: String){
    DefToken("def", "Definition"),
    LetToken("let", "Let"),
}

enum class DelimitingTokenType(val symbol: String, val tokenName: String){
    EqualSignToken("=", "EqualSign"),
    ColonToken(":", "Colon"),
    LeftParenToken("\\(", "LeftParenthesis"),
    RightParenToken("\\)", "RightParenthesis"),
    LeftBraceToken("\\{", "LeftBrace"),
    RightBraceToken("\\}", "RightBrace"),
    PlusSignToken("\\+", "PlusSign"),
    MinusSignToken("\\-", "MinusSign"),
    StarSign("\\*", "StarSign"),
    ForwardSlashSign("\\/", "ForwardSlash"),
    BackslashSign("\\\\", "Backslash"),
    DoubleQuoteSign("\\\"", "DoubleQuote"),
    ApostropheSign("\\'", "Apostrophe"),
    BangSign("\\!", "Bang"),
    UnderscoreSign("\\_", "Underscore"),
    DotSign("\\.", "Dot"),
    CommaSign("\\,", "Comma"),
    SemicolonSign("\\;", "Semicolon"),
    AmpersandSign("\\&", "Ampersand"),
    DollarSign("\\$", "DollarSign"),
    PercentSign("\\%", "PercentSign"),
    HashSign("\\#", "HashSign"),
    AtSign("\\@", "AtSign"),
    PipeSign("\\|", "Pipe"),
    QuestionSign("\\?", "Question"),
    RightAngleBracketSign("\\>", "RightAngleBracket"),
    LeftAngleBracketSign("\\<", "LeftAngleBracket"),
    CaretSign("\\^", "Caret"),
    LeftSquareBracketSign("\\[", "LeftSquareBracket"),
    RightSquareBracketSign("\\]", "RightSquareBracket"),
}

enum class OtherTokenType(val tokenName: String){
    IdentifierToken("Identifier"),
    IntegerToken("Integer"),
    EndOfFileToken("EndOfFile")
}

sealed class Token(open val tokenLocation: TokenLocation){
    data class KeywordToken(val tokenType: KeywordTokenType, override val tokenLocation: TokenLocation): Token(tokenLocation){
        override fun toString(): String {
            return buildPrettyString {
                this.appendWithNewLine("${this@KeywordToken.tokenType.tokenName}{")
                indent {
                    this.appendWithNewLine("${this@KeywordToken.tokenLocation}")
                }
                this.appendWithNewLine("}")
            }
        }
    }
    data class DelimitingToken(val tokenType: DelimitingTokenType, override val tokenLocation: TokenLocation): Token(tokenLocation){
        override fun toString(): String {
            return buildPrettyString {
                this.appendWithNewLine("${this@DelimitingToken.tokenType.tokenName}{")
                indent {
                    this.appendWithNewLine("${this@DelimitingToken.tokenLocation}")
                }
                this.appendWithNewLine("}")
            }
        }
    }
    sealed class OtherToken(open val tokenType: OtherTokenType, override val tokenLocation: TokenLocation): Token(tokenLocation){
        data class IdentifierToken(val symbol: String, override val tokenLocation: TokenLocation): OtherToken(OtherTokenType.IdentifierToken, tokenLocation){
            override fun toString(): String {
                return buildPrettyString {
                    this.appendWithNewLine("${this@IdentifierToken.tokenType.tokenName}{")
                    this.indent {
                        this.appendWithNewLine("symbol: ${this@IdentifierToken.symbol}")
                        this.appendWithNewLine("${this@IdentifierToken.tokenLocation}")
                    }
                    this.appendWithNewLine("}")
                }
            }
        }
        data class IntegerLiteralToken(val symbol: String, override val tokenLocation: TokenLocation): OtherToken(OtherTokenType.IntegerToken, tokenLocation){
            override fun toString(): String {
                return buildPrettyString {
                    this.appendWithNewLine("${this@IntegerLiteralToken.tokenType.tokenName}{")
                    this.indent {
                        this.appendWithNewLine("symbol: ${this@IntegerLiteralToken.symbol}")
                        this.appendWithNewLine("${this@IntegerLiteralToken.tokenLocation}")
                    }
                    this.appendWithNewLine("}")
                }
            }
        }
        data class EndOfFileToken(override val tokenLocation: TokenLocation): OtherToken(OtherTokenType.EndOfFileToken, tokenLocation){
            override fun toString(): String {
                return buildPrettyString {
                    this.appendWithNewLine("${this@EndOfFileToken.tokenType.tokenName}{")
                    this.indent {
                        this.appendWithNewLine("${this@EndOfFileToken.tokenLocation}")
                    }
                    this.appendWithNewLine("}")
                }
            }
        }

    }
}