package language.lexer

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import language.serializer.bytestream.Serializable
import language.serializer.prettyprint.buildPrettyString

data class TokenLocation(val fileName: String, val line: Int, val column: Int): Serializable<TokenLocation>{
    override fun toBytes(): ByteReadPacket = buildPacket{
        this.writeByte(0xb.toByte())
        //Token location file name
        this.writeByte(0xb1.toByte())
        this.writeStringUtf8(this@TokenLocation.fileName)
        //Token location line number
        this.writeByte(0xb2.toByte())
        this.writeInt(this@TokenLocation.line)
        //Token location column
        this.writeByte(0xb3.toByte())
        this.writeInt(this@TokenLocation.column)
    }

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

sealed class Token(open val tokenLocation: TokenLocation): Serializable{
    data class KeywordToken(val tokenType: KeywordTokenType, override val tokenLocation: TokenLocation): Token(tokenLocation){
        override fun toBytes(): ByteReadPacket = buildPacket {
            //Token Start
            this.writeByte(0xfe.toByte())
            //Token meta start
            this.writeByte(0xa.toByte())
            //Token name
            this.writeByte(0xa1.toByte())
            this.writeStringUtf8(this@KeywordToken.tokenType.tokenName)
            //Token location start
            this.writePacket(this@KeywordToken.tokenLocation.toBytes())
            //Token end
            this.writeByte(0xff.toByte())
        }
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
        override fun toBytes(): ByteReadPacket = buildPacket {
            //Token Start
            this.writeByte(0xfe.toByte())
            //Token meta start
            this.writeByte(0xa.toByte())
            //Token name
            this.writeByte(0xa1.toByte())
            this.writeStringUtf8(this@DelimitingToken.tokenType.tokenName)
            //Token location
            this.writePacket(this@DelimitingToken.tokenLocation.toBytes())
            //Token end
            this.writeByte(0xff.toByte())
        }
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
            override fun toBytes(): ByteReadPacket = buildPacket {
                //Token Start
                this.writeByte(0xfe.toByte())
                //Token meta start
                this.writeByte(0xa.toByte())
                //Token name
                this.writeByte(0xa1.toByte())
                this.writeStringUtf8(this@IdentifierToken.tokenType.tokenName)
                //Token symbol
                this.writeByte(0xa1.toByte())
                this.writeStringUtf8(this@IdentifierToken.symbol)
                //Token location
                this.writePacket(this@IdentifierToken.tokenLocation.toBytes())
                //Token end
                this.writeByte(0xff.toByte())
            }
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
            /*override fun toBytes(): ByteReadPacket = buildBytePacket {
                //Token Start
                this.appendByte(0xfe.toByte())
                //Token meta start
                this.appendByte(0xa.toByte())
                //Token name
                this.appendByte(0xa1.toByte())
                this.appendString(this@IntegerLiteralToken.tokenType.tokenName)
                //Token symbol
                this.appendByte(0xa1.toByte())
                this.appendString(this@IntegerLiteralToken.symbol)
                //Token location
                this.append(this@IntegerLiteralToken.tokenLocation.toBytes())
                //Token end
                this.appendChar(0xff.toChar())
            }*/
            override fun toBytes(): ByteReadPacket = buildPacket {
                //Token Start
                this.writeInt(0xfe)
                //Token meta start
                this.writeInt(0xa)
                //Token name
                this.writeInt(0x1a)
                this.writeStringUtf8(this@IntegerLiteralToken.tokenType.tokenName)
                //Token symbol
                this.writeInt(0xa1)
                this.writeStringUtf8(this@IntegerLiteralToken.symbol)
                //Token location
                this.writePacket(this@IntegerLiteralToken.tokenLocation.toBytes())
                //Token end
                this.writeInt(0xff)
            }
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
            override fun toBytes(): ByteReadPacket = buildPacket {
                //Token Start
                this.writeByte(0xfe.toByte())
                //Token meta start
                this.writeByte(0xa.toByte())
                //Token name
                this.writeByte(0xa1.toByte())
                this.writeStringUtf8(this@EndOfFileToken.tokenType.tokenName)
                //Token location
                this.writePacket(this@EndOfFileToken.tokenLocation.toBytes())
                //Token end
                this.writeByte(0xff.toByte())
            }
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