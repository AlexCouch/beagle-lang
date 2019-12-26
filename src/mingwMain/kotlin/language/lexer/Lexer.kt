package language.lexer

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

    internal var currentToken: Token? = null
    internal var currentLexeme: StringBuilder = StringBuilder()

    private var state: LexerState = LexerState.Idle

    internal val errors = arrayListOf<String>()

    constructor(istream: StringInputStream) : this(istream.readStr())
    constructor(istream: FileInputStream) : this(istream.readStr(), istream.path){
        println(this.input)
    }

    //TODO: Create stringify library
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("\tLexer:{\n")
        sb.append("\t\tcurrent filePath: ${this.filePath}\n")
        sb.append("\t\tcurrent input string: ${this.input}\n")
        sb.append("\t\tcurrent line: ${this.lineIdx}\n")
        sb.append("\t\tcurrent column: ${this.column}\n")
        sb.append("\t\tcurrent token location: {\n")
        sb.append("\t\t\tfilename: ${this.tokenLocation.fileName},\n")
        sb.append("\t\t\tline: ${this.tokenLocation.line},\n")
        sb.append("\t\t\tcolumn: ${this.tokenLocation.column},\n")
        sb.append("\t\t},\t")
        sb.append("\t\tcurrent char: ${this.currentChar},\n")
        sb.append("\t\ttokens: {\n")
        sb.append("\t\t},\n")
        sb.append("\t\tcurrent token: {\n")
        sb.append(this.currentToken?.toString())
        sb.append("\t\t}\n")
        sb.append("current lexeme: ${this.currentLexeme},\n")
        sb.append("current state: ${this.state.name}")
        return super.toString()
    }
}