package language.lexer

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import language.error.ErrorManager
import language.serializer.prettyprint.*
import language.streams.FileInputStream
import language.streams.StringInputStream

data class LookaheadScanner(private val lexer: Lexer, var position: Int){
    val lookaheadChar: Char? get(){
//        println("Lookahead position: $position")
        return this.lexer.input.getOrNull(this.position)
    }

    override fun toString(): String {
        return buildPrettyString {
            this.appendWithNewLine("lookahead scanner:")
            this.indent {
                this.appendWithNewLine("lookahead position: ${this@LookaheadScanner.position}")
                this.appendWithNewLine("lookahead char: ${this@LookaheadScanner.lookaheadChar}")
            }
        }
    }
}

class Lexer(internal val input: String, internal val filePath: String = ""){
    internal var lineIdx = 1
        set(new){
            column = 1
            field = new
        }
    internal var column: Int = 0
        set(new){
//            println("Column: $new")
            field = new
        }
    internal val tokenLocation: TokenLocation
        get() = TokenLocation(this.filePath, this.lineIdx, this.column)

    internal val currentChar: Char? get() = this.input.getOrNull(this.position)
    internal var position = -1

    internal val lookaheadScanner: LookaheadScanner = LookaheadScanner(this, this.position)
    val tokenStream = Channel<Token>()

    internal var currentLexeme: StringBuilder = StringBuilder()

    internal val errorManager = LexerErrorManager(this)

    constructor(istream: StringInputStream) : this(istream.readStr())
    constructor(istream: FileInputStream) : this(istream.readStr(), istream.path){
        println(this.input)
    }

    init{
        errorManager.start()
    }

    override fun toString(): String {
        return buildPrettyString {
            this.appendWithNewLine("Lexer:")
            this.indent {
                this.appendWithNewLine("line index: ${this@Lexer.lineIdx}")
                this.appendWithNewLine("column: ${this@Lexer.column}")
                this.appendWithNewLine("token location:")
                this.indent {
                    this.appendWithNewLine(this@Lexer.tokenLocation.toString())
                }
                this.appendWithNewLine("current char: ${this@Lexer.currentChar}")
                this.appendWithNewLine("current lexeme: ${this@Lexer.currentLexeme}")
                this.appendWithNewLine("current position: ${this@Lexer.position}")
                this.indent{
                    this.appendWithNewLine(this@Lexer.lookaheadScanner.toString())
                }
            }
        }
    }
}