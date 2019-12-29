package language.lexer

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import language.error.ErrorManager
import language.streams.FileInputStream
import language.streams.StringInputStream

data class LookaheadScanner(private val lexer: Lexer, var position: Int){
    val lookaheadChar: Char? get(){
//        println("Lookahead position: $position")
        return this.lexer.input.getOrNull(this.position)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("LookaheadScanner{\n")

        sb.append("\tposition: ${this.position},\n")
        sb.append("\tlookaheadChar: ${this.lookaheadChar},\n")
        sb.append("}")
        return sb.toString()
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
}