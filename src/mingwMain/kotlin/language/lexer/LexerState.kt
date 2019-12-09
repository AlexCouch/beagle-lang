package language.lexer

enum class LexerState{
    /*
        Starting state; Does nothing
     */
    Idle{
        override fun transitionTo(lexer: Lexer): Boolean = true

        override fun transitionFrom(lexer: Lexer): LexerState{
            return LexerAdvancing
        }
    },
    LexerResetting{
        override fun transitionTo(lexer: Lexer): Boolean {
            lexer.column = 0
            lexer.position = lexer.lookaheadScanner.position
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = AdvanceScanner

    },
    /*
        LexerAdvancing moves lexer cursor/pointer to the current lookahead cursor/pointer
     */
    LexerAdvancing{
        override fun transitionTo(lexer: Lexer): Boolean {
            val posDelta = lexer.lookaheadScanner.position - lexer.position
            lexer.column += posDelta
//            println("Column: ${lexer.column}; lookaheadPos: ${lexer.lookaheadScanner.position}; lexerpos: ${lexer.position}")
            lexer.position = lexer.lookaheadScanner.position
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = when{
            lexer.currentChar?.toInt() == 65535 -> EndOfFileDetected
            else -> Scanning
        }
    },
    Scanning{
        override fun transitionTo(lexer: Lexer): Boolean = true

        override fun transitionFrom(lexer: Lexer): LexerState = when{
            lexer.lookaheadScanner.lookaheadChar == null -> AdvanceScanner
            lexer.lookaheadScanner.lookaheadChar?.isWhitespace() == true -> when{
                lexer.lookaheadScanner.lookaheadChar?.toString()?.matches(Regex("(\n|\r\n|\r)")) == true -> when{
                    lexer.currentLexeme.toString().isNotBlank() -> BuildingToken
                    else -> EndOfLineDetected
                }
                lexer.currentLexeme.toString().isNotBlank() -> BuildingToken
                else -> AdvanceScanner
            }
            lexer.lookaheadScanner.lookaheadChar?.toInt() == 65535 -> when{
                lexer.currentLexeme.toString().isNotBlank() -> BuildingToken
                else -> LexerAdvancing
            }
            DelimitingTokenType.values().find{
                val regex = Regex(it.symbol)
                lexer.currentLexeme.toString().matches(regex)
            } != null -> BuildingToken
            DelimitingTokenType.values().find{
                val regex = Regex(it.symbol)
                lexer.lookaheadScanner.lookaheadChar?.toString()?.matches(regex) == true
            } != null -> {
//                println("Lookahead char: ${lexer.lookaheadScanner.lookaheadChar}; Position: ${lexer.lookaheadScanner.position}")
                when{
                    lexer.currentLexeme.toString().isNotBlank() -> BuildingToken
                    lexer.currentChar?.isWhitespace() == true -> LexerAdvancing
                    else -> ConsumeChar
                }
            }
            else -> when{
                lexer.currentChar?.isWhitespace() == true -> when{
                    lexer.currentChar?.toString()?.matches(Regex("(\r|\r\n|\n)")) == true -> when{
                        lexer.lookaheadScanner.lookaheadChar?.toString()?.matches(Regex("(\r|\r\n|\n)")) == true -> EndOfLineDetected
                        else -> LexerAdvancing
                    }
                    else -> LexerAdvancing
                }
                lexer.currentChar == null -> LexerAdvancing
                else -> ConsumeChar
            }
        }

    },
    /*
        Scanning increments the current lookahead cursor/pointer
        and then appends the current lookahead char to the current lexeme builder
     */
    AdvanceScanner{
        override fun transitionTo(lexer: Lexer): Boolean {
            lexer.lookaheadScanner.position++
//            println("Scanning: ${lexer.lookaheadScanner.lookaheadChar}")
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = Scanning
    },
    ConsumeChar{
        override fun transitionTo(lexer: Lexer): Boolean {
            lexer.currentLexeme.append(lexer.lookaheadScanner.lookaheadChar)
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = AdvanceScanner
    },
    /*
        End of line detected, so increment the lexer's line counter and continue scanning
     */
    EndOfLineDetected{
        override fun transitionTo(lexer: Lexer): Boolean {
//            println("New line detected!")
            lexer.lineIdx++
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = LexerResetting
    },
    /*
        Attempts to build a token from the current lexeme
     */
    BuildingToken{
        override fun transitionTo(lexer: Lexer): Boolean {
            val lexeme = lexer.currentLexeme.toString()
//            println("Lexeme: $lexeme; position: ${lexer.column}")
            if(lexeme.isBlank()){
                return true
            }
            KeywordTokenType.values().forEach {
                val regex = Regex(it.symbol)
                if(lexeme.matches(regex)){
                    lexer.currentToken = Token.KeywordToken(it, lexer.tokenLocation)
                    return true
                }
            }
            DelimitingTokenType.values().forEach {
                val regex = Regex(it.symbol)
                if(lexeme.matches(regex)){
                    lexer.currentToken = Token.DelimitingToken(it, lexer.tokenLocation)
                    return true
                }
            }
            if(lexeme.matches(Regex("[a-zA-Z][a-zA-Z0-9]+"))) {
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
    /*
        Stores the current token in the lexer's list of tokens and clears the lexeme builder
     */
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

        override fun transitionFrom(lexer: Lexer): LexerState = LexerAdvancing
    },
    /*
        Found the end of the file, so add the EOF token and return to the start state (making this state effectively final)
     */
    EndOfFileDetected{
        override fun transitionTo(lexer: Lexer): Boolean {
            lexer.currentToken = Token.OtherToken.EndOfFileToken(lexer.tokenLocation)
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = Idle
    },
    /*
        Reserved for error handling
     */
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