package language.lexer

import kotlinx.io.core.ExperimentalIoApi

@ExperimentalIoApi
enum class LexerState{
    Idle{
        override fun transitionTo(lexer: Lexer): Boolean = true

        override fun transitionFrom(lexer: Lexer): LexerState{
            println(lexer.input)
            return LexerAdvancing
        }
    },
    LexerAdvancing{
        override fun transitionTo(lexer: Lexer): Boolean {
            println(lexer.input)
            lexer.lookaheadScanner.position++
            lexer.position = lexer.lookaheadScanner.position
            println("Lexer position: ${lexer.position}")
            println("Lookahead position: ${lexer.lookaheadScanner.position}")
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = when{
            lexer.currentChar == null && lexer.lookaheadScanner.lookaheadChar != null -> LexerAdvancing
            lexer.currentChar == null && lexer.lookaheadScanner.lookaheadChar == null -> EndOfFileDetected
            else -> Scanning
        }
    },
    Scanning{
        override fun transitionTo(lexer: Lexer): Boolean {
            println("Lexer scanning")
            lexer.currentLexeme.append(lexer.lookaheadScanner.lookaheadChar)
            lexer.lookaheadScanner.position++
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = when{
            lexer.lookaheadScanner.lookaheadChar == null -> EndOfFileDetected
            lexer.lookaheadScanner.lookaheadChar?.isWhitespace() == true -> BuildingToken
            lexer.lookaheadScanner.lookaheadChar == '\n' -> EndOfLineDetected
            lexer.lookaheadScanner.lookaheadChar == '\r' -> LexerAdvancing
            DelimitingTokenType.values().find { it.symbol == lexer.lookaheadScanner.lookaheadChar.toString() } != null -> BuildingToken
            else -> Scanning
        }
    },
    EndOfLineDetected{
        override fun transitionTo(lexer: Lexer): Boolean {
            println("End of line detected!")
            lexer.lineIdx++
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = Scanning
    },
    BuildingToken{
        override fun transitionTo(lexer: Lexer): Boolean {
            val lexeme = lexer.currentLexeme.toString()
            KeywordTokenType.values().forEach {
                if(it.symbol == lexeme){
                    lexer.currentToken = Token.KeywordToken(it, lexer.tokenLocation)
                    return true
                }
            }
            DelimitingTokenType.values().forEach {
                if(it.symbol == lexeme){
                    lexer.currentToken = Token.DelimitingToken(it, lexer.tokenLocation)
                    return true
                }
            }
            if(lexeme.matches(Regex("[a-zA-Z]?[a-zA-Z0-9]+"))) {
                lexer.currentToken = Token.OtherToken.IdentifierToken(lexeme, lexer.tokenLocation)
                return true
            }else if(lexeme.matches(Regex("[0-9]+"))){
                lexer.currentToken = Token.OtherToken.IntegerLiteralToken(lexeme, lexer.tokenLocation)
                return true
            }
            return false
        }

        override fun transitionFrom(lexer: Lexer): LexerState = BuiltToken
    },
    BuiltToken{
        override fun transitionTo(lexer: Lexer): Boolean {
            if(lexer.currentToken == null){
                return false
            }
            lexer.tokens += lexer.currentToken!!
            lexer.currentLexeme.apply {
                if(this.isNotEmpty()) this.clear()
            }
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = Idle
    },
    EndOfFileDetected{
        override fun transitionTo(lexer: Lexer): Boolean {
            lexer.currentToken = Token.OtherToken.EndOfFileToken(lexer.tokenLocation)
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = Idle
    },
    Error{
        override fun transitionTo(lexer: Lexer): Boolean {
            lexer.errors += "Lexer encountered an error while scanning:"
            lexer.errors += lexer.toString()
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = Error
    };

    abstract fun transitionTo(lexer: Lexer): Boolean
    abstract fun transitionFrom(lexer: Lexer): LexerState
}