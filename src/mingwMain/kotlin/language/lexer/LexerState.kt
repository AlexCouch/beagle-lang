package language.lexer

import platform.posix.EOF

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
    /*
        LexerAdvancing moves lexer cursor/pointer to the current lookahead cursor/pointer
     */
    LexerAdvancing{
        override fun transitionTo(lexer: Lexer): Boolean {
            val prePos = lexer.position
            lexer.position = lexer.lookaheadScanner.position
            val delta = lexer.position - prePos
            lexer.column += delta
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = when{
            lexer.currentChar?.toInt() == 65535 -> EndOfFileDetected
            lexer.currentChar?.toString()?.matches(Regex("(\n|\r\n|\r)")) == true-> EndOfLineDetected
            else -> Scanning
        }
    },
    Scanning{
        override fun transitionTo(lexer: Lexer): Boolean = true

        override fun transitionFrom(lexer: Lexer): LexerState = when{
            lexer.currentChar == null -> when{
                lexer.lookaheadScanner.lookaheadChar == null -> AdvanceScanner
                lexer.lookaheadScanner.lookaheadChar?.toInt() == 65535 -> EndOfFileDetected
                else -> LexerAdvancing
            }
            lexer.currentLexeme.toString().isNotBlank() -> when{
                lexer.lookaheadScanner.lookaheadChar?.isWhitespace() == true -> BuildingToken
                lexer.lookaheadScanner.lookaheadChar?.toInt() == 65535 -> BuildingToken
                DelimitingTokenType.values().find{
                    val regex = Regex(it.symbol)
                    lexer.lookaheadScanner.lookaheadChar?.toString()?.matches(regex) == true ||
                    lexer.currentLexeme.toString().matches(regex)
                } != null -> BuildingToken
                else -> ConsumeChar
            }
            lexer.currentChar?.isWhitespace() == true -> when{
                lexer.lookaheadScanner.lookaheadChar?.toInt() == 65535 -> EndOfFileDetected
                DelimitingTokenType.values().find{
                    val regex = Regex(it.symbol)
                    lexer.lookaheadScanner.lookaheadChar?.toString()?.matches(regex) == true
                } != null -> ConsumeChar
                lexer.lookaheadScanner.lookaheadChar?.isWhitespace() == true -> AdvanceScanner
                else -> LexerAdvancing
            }
            else -> ConsumeChar
        }

    },
    /*
        Scanning increments the current lookahead cursor/pointer
        and then appends the current lookahead char to the current lexeme builder
     */
    AdvanceScanner{
        override fun transitionTo(lexer: Lexer): Boolean {
            lexer.lookaheadScanner.position++
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
            lexer.lineIdx++
            return true
        }

        override fun transitionFrom(lexer: Lexer): LexerState = Scanning
    },
    /*
        Attempts to build a token from the current lexeme
     */
    BuildingToken{
        override fun transitionTo(lexer: Lexer): Boolean {
            val lexeme = lexer.currentLexeme.toString()
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