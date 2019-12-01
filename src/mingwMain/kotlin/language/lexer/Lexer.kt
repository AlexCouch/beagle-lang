package language.lexer

import language.streams.StringInputStream

data class LookaheadScanner(private val lexer: Lexer, var position: Int){
    val lookaheadChar: Char?
        get() = this.lexer.lineStr.getOrNull(this.position)
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
    private val lookaheadScanner: LookaheadScanner = LookaheadScanner(this, this.position)

    constructor(istream: StringInputStream) : this(istream.readStr())
    constructor(istream: StringInputStream, filePath: String) : this(istream.readStr(), filePath)

    fun nextToken(): Token{
        val lexeme = StringBuilder()
        do{
            lexeme.append(this.lookaheadScanner.lookaheadChar)
            val token = TokenBuilder.createToken(lexeme.toString(), this.tokenLocation)
            if(token.tokenType != Tokens.IllegalToken){
                return token
            }
            this.lookaheadScanner.position++
        }while(this.lookaheadScanner.lookaheadChar?.isWhitespace() == false)
        this.position = this.lookaheadScanner.position + 1
        return Token(Tokens.IdentToken, lexeme.toString(), this.tokenLocation)
    }

    fun getTokens(): ArrayList<Token>{
        val tokens = arrayListOf<Token>()
        val lines = this.input.split('\n')
        for ((i, l) in lines.withIndex().iterator()){
            this.lineStr = l
            this.lineIdx = i + 1
            do{
                if(this.currentChar?.isWhitespace() == false){
                    tokens += this.nextToken()
                }
                this.position++
            }while(this.currentChar != null)
        }
        return tokens
    }
}