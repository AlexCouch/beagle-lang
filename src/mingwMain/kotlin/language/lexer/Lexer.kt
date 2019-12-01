package language.lexer

import language.streams.StringInputStream

data class LookaheadScanner(private val lexer: Lexer, var position: Int){
    val lookaheadChar: Char?
        get() = this.lexer.lineStr.getOrNull(this.position)

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("LookaheadScanner{\n")
        sb.append("\tLexer:{\n")
        sb.append("\t\tlineStr: ${this.lexer.lineStr}\n")
        sb.append("\t\tlineIdx: ${this.lexer.lineIdx}\n")
        sb.append("\t\tcolumn: ${this.lexer.column}\n")
        sb.append("\t},\n")
        sb.append("\tposition: ${this.position},\n")
        sb.append("\tlookaheadChar: ${this.lookaheadChar},\n")
        sb.append("}")
        return sb.toString()
    }
}

class Lexer(private val input: String, private val filePath: String = ""){
    var lineStr = ""
    var lineIdx = 0
        set(new){
            position = 0
            field = new
        }
    val column: Int get() = this.position + 1
    private val tokenLocation: TokenLocation
        get() = TokenLocation(this.filePath, this.lineIdx, this.column)

    val errors = arrayListOf<String>()
    private val currentChar: Char? get() = this.lineStr.getOrNull(this.position)

    private var position = 0
        get(){

        }
    private val lookaheadScanner: LookaheadScanner = LookaheadScanner(this, this.position)

    constructor(istream: StringInputStream) : this(istream.readStr())
    constructor(istream: StringInputStream, filePath: String) : this(istream.readStr(), filePath)

    fun nextToken(): Token{
        val lexeme = StringBuilder()
        do{
            lexeme.append(this.lookaheadScanner.lookaheadChar)
            val token = TokenBuilder.createToken(lexeme.toString(), this.tokenLocation)
            if(token.tokenType != Tokens.IllegalToken){
                this.lookaheadScanner.position++
                return token
            }
            this.lookaheadScanner.position++
        }while(this.lookaheadScanner.lookaheadChar?.isWhitespace() == false)
        this.lookaheadScanner.position++
        this.position = this.lookaheadScanner.position
        return Token(Tokens.IdentToken, lexeme.toString(), this.tokenLocation)
    }

    fun getTokens(): ArrayList<Token>{
        val tokens = arrayListOf<Token>()
        val lines = this.input.split('\n')
        for ((i, l) in lines.withIndex().iterator()){
            this.lineStr = l
            this.lineIdx = i + 1
            this.lookaheadScanner.position = position
            do{
                if(this.currentChar?.isWhitespace() == false){
                    tokens += this.nextToken()
                }else{
                    this.lookaheadScanner.position++
                }
                this.position = this.lookaheadScanner.position
            }while(this.currentChar != null)
        }
        tokens += Token(Tokens.EOFToken, "", this.tokenLocation)
        return tokens
    }
}