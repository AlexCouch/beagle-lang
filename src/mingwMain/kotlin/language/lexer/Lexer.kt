package language.lexer

import language.streams.StringInputStream

data class LookaheadScanner(private val lexer: Lexer, var position: Int){
    val lookaheadChar: Char?
        get() = this.lexer.lineStr.getOrNull(this.position)
}

class Lexer(val input: String){
    var lineStr = ""
    var lineIdx = 0
        set(new){
            position = 0
            field = new
        }
    val column: Int get() = this.position + 1
    val errors = arrayListOf<String>()
    val currentChar: Char? get() = this.lineStr.getOrNull(this.position)

    var position = 0
        set(new){
            this.lookaheadScanner.position = new
            field = new
        }
    var lookaheadScanner: LookaheadScanner = LookaheadScanner(this, this.position)

    constructor(istream: StringInputStream) : this(istream.readStr())

    fun peekIdentifier(): String{
        val sb = StringBuilder()
        do{
            sb.append(this.lookaheadScanner.lookaheadChar)
            this.lookaheadScanner.position++
        }while(this.lookaheadScanner.lookaheadChar?.isLetterOrDigit() == true)
        this.lookaheadScanner.position = this.position
        return sb.toString()
    }

    fun readIdentifier(): String{
        val sb = StringBuilder()
        do{
            sb.append(this.currentChar)
            this.position++
        }while(this.currentChar?.isLetterOrDigit() == true)
        return sb.toString()
    }

    fun getTokens(): ArrayList<Token>{
        val tokens = arrayListOf<Token>()
        var currentToken: Token?
        val lines = this.input.split('\n')
        for ((i, l) in lines.withIndex().iterator()){
//            println("Parsing line: $i")
            this.lineStr = l
            this.lineIdx = i + 1
            tokenize@ do{
//                println(this.currentChar)
                when(this.currentChar){
                    ' ' -> {
                        this.position++
                    }
                    else -> {
                        currentToken = TokenBuilder.nextToken(this)
                        if(currentToken == null){
                            this.errors.add("Could not recognize token at line $i, column ${this.column}")
                        }else{
                            tokens.add(currentToken)
                        }
                        this.position++
                    }
                }

            }while(this.currentChar != null)
        }
        return tokens
    }
}