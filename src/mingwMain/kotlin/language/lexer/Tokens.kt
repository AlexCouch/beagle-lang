package language.lexer

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

enum class KeywordTokenType(val symbol: String, val tokenName: String, val highByte: Byte, val byteId: Byte){
    DefToken("def", "Definition", 0xa1.toByte(), 0x10.toByte()),
    LetToken("let", "Let", 0xa1.toByte(), 0x20.toByte()),
}

enum class DelimitingTokenType(val symbol: String, val tokenName: String, val highByte: Byte, val byteId: Byte){
    EqualSignToken("=", "EqualSign", 0xa2.toByte(), 0xa1.toByte()),
    ColonToken(":", "Colon", 0xa2.toByte(), 0xa2.toByte()),
    LeftParenToken("\\(", "LeftParenthesis", 0xa2.toByte(), 0xa3.toByte()),
    RightParenToken("\\)", "RightParenthesis", 0xa2.toByte(), 0xa4.toByte()),
    LeftBraceToken("\\{", "LeftBrace", 0xa2.toByte(), 0xa5.toByte()),
    RightBraceToken("\\}", "RightBrace", 0xa2.toByte(), 0xa6.toByte()),
    PlusSignToken("\\+", "PlusSign", 0xa2.toByte(), 0xa7.toByte()),
    MinusSignToken("\\-", "MinusSign", 0xa2.toByte(), 0xa8.toByte()),
    StarSign("\\*", "StarSign", 0xa2.toByte(), 0xa9.toByte()),
    ForwardSlashSign("\\/", "ForwardSlash", 0xa2.toByte(), 0xaa.toByte()),
    BackslashSign("\\\\", "Backslash", 0xa2.toByte(), 0xab.toByte()),
    DoubleQuoteSign("\\\"", "DoubleQuote", 0xa2.toByte(), 0xac.toByte()),
    ApostropheSign("\\'", "Apostrophe", 0xa2.toByte(), 0xad.toByte()),
    BangSign("\\!", "Bang", 0xa2.toByte(), 0xae.toByte()),
    UnderscoreSign("\\_", "Underscore", 0xa2.toByte(), 0xaf.toByte()),
    DotSign("\\.", "Dot", 0xa2.toByte(), 0xb0.toByte()),
    CommaSign("\\,", "Comma", 0xa2.toByte(), 0xb1.toByte()),
    SemicolonSign("\\;", "Semicolon", 0xa2.toByte(), 0xb2.toByte()),
    AmpersandSign("\\&", "Ampersand", 0xa2.toByte(), 0xb3.toByte()),
    DollarSign("\\$", "DollarSign", 0xa2.toByte(), 0xb4.toByte()),
    PercentSign("\\%", "PercentSign", 0xa2.toByte(), 0xb5.toByte()),
    HashSign("\\#", "HashSign", 0xa2.toByte(), 0xb6.toByte()),
    AtSign("\\@", "AtSign", 0xa2.toByte(), 0xb7.toByte()),
    PipeSign("\\|", "Pipe", 0xa2.toByte(), 0xb8.toByte()),
    QuestionSign("\\?", "Question", 0xa2.toByte(), 0xb9.toByte()),
    RightAngleBracketSign("\\>", "RightAngleBracket", 0xa2.toByte(), 0xb9.toByte()),
    LeftAngleBracketSign("\\<", "LeftAngleBracket", 0xa2.toByte(), 0xba.toByte()),
    CaretSign("\\^", "Caret", 0xa2.toByte(), 0xbb.toByte()),
    LeftSquareBracketSign("\\[", "LeftSquareBracket", 0xa2.toByte(), 0xbc.toByte()),
    RightSquareBracketSign("\\]", "RightSquareBracket", 0xa2.toByte(), 0xbd.toByte()),
}

enum class OtherTokenType(val tokenName: String, val highByte: Byte, val byteId: Byte){
    IdentifierToken("Identifier", 0xa3.toByte(), 0x3a.toByte()),
    IntegerToken("Integer", 0xa3.toByte(), 0x3b.toByte()),
    EndOfFileToken("EndOfFile", 0xa3.toByte(), 0x3c.toByte())
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
                    this.append("${this@DelimitingToken.tokenLocation}")
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