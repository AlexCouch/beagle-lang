package language.lexer

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import language.statemanager.State
import language.statemanager.StateManagerResult

enum class LexerState: State<Lexer>{
    /*
        Starting state; Does nothing
     */
    Idle{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> = StateManagerResult(true, "Idle state does nothing.")

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>>{
            return StateManagerResult(LexerAdvancing, "")
        }
    },
    LexerResetting{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
            moduleInstance.column = 0
            moduleInstance.position = moduleInstance.lookaheadScanner.position
            return StateManagerResult(true, "")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = StateManagerResult(AdvanceScanner, "")

    },
    /*
        LexerAdvancing moves lexer cursor/pointer to the current lookahead cursor/pointer
     */
    LexerAdvancing{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
            val posDelta = moduleInstance.lookaheadScanner.position - moduleInstance.position
            moduleInstance.column += posDelta
//            println("Column: ${lexer.column}; lookaheadPos: ${lexer.lookaheadScanner.position}; lexerpos: ${lexer.position}")
            moduleInstance.position = moduleInstance.lookaheadScanner.position
            return StateManagerResult(true, "")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = when{
            moduleInstance.currentChar?.toInt() == 65535 -> StateManagerResult(EndOfFileDetected, "Transitioning to EOF state")
            else -> StateManagerResult(Scanning, "Transitioning to scanning state")
        }
    },
    Scanning{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> = StateManagerResult(true, "Scanning does nothing.")

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = when{
            moduleInstance.lookaheadScanner.lookaheadChar == null -> StateManagerResult(AdvanceScanner, "Transitioning to scanner advancing state")
            moduleInstance.lookaheadScanner.lookaheadChar?.isWhitespace() == true -> when{
                moduleInstance.lookaheadScanner.lookaheadChar?.toString()?.matches(Regex("(\n|\r\n|\r)")) == true -> when{
                    moduleInstance.currentLexeme.toString().isNotBlank() -> StateManagerResult(BuildingToken, "Transitioning to token building state.")
                    else -> StateManagerResult(EndOfLineDetected, "Transitioning to end of line state")
                }
                moduleInstance.currentLexeme.toString().isNotBlank() -> StateManagerResult(BuildingToken, "")
                else -> StateManagerResult(AdvanceScanner, "Transitioning to scanner advancing state")
            }
            moduleInstance.lookaheadScanner.lookaheadChar?.toInt() == 65535 -> when{
                moduleInstance.currentLexeme.toString().isNotBlank() -> StateManagerResult(BuildingToken, "Transitioning to token building state.")
                else -> StateManagerResult(LexerAdvancing, "Transitioning to lexer advancing state.")
            }
            DelimitingTokenType.values().find{
                val regex = Regex(it.symbol)
                moduleInstance.currentLexeme.toString().matches(regex)
            } != null -> StateManagerResult(BuildingToken, "Transitioning to token building state.")
            DelimitingTokenType.values().find{
                val regex = Regex(it.symbol)
                moduleInstance.lookaheadScanner.lookaheadChar?.toString()?.matches(regex) == true
            } != null -> {
//                println("Lookahead char: ${lexer.lookaheadScanner.lookaheadChar}; Position: ${lexer.lookaheadScanner.position}")
                when{
                    moduleInstance.currentLexeme.toString().isNotBlank() -> StateManagerResult(BuildingToken, "Transitioning to token building state.")
                    moduleInstance.currentChar?.isWhitespace() == true -> StateManagerResult(LexerAdvancing, "Transitioning to lexer advancing state.")
                    else -> StateManagerResult(ConsumeChar, "Transitioning to consume char state")
                }
            }
            else -> when{
                moduleInstance.currentChar?.isWhitespace() == true -> when{
                    moduleInstance.currentChar?.toString()?.matches(Regex("(\r|\r\n|\n)")) == true -> when{
                        moduleInstance.lookaheadScanner.lookaheadChar?.toString()?.matches(Regex("(\r|\r\n|\n)")) == true -> StateManagerResult(EndOfLineDetected, "Transitioning to EOF state")
                        else -> StateManagerResult(LexerAdvancing, "Transitioning to lexer advancing state.")
                    }
                    else -> StateManagerResult(LexerAdvancing, "Transitioning to lexer advancing state.")
                }
                moduleInstance.currentChar == null -> StateManagerResult(LexerAdvancing, "Transitioning to lexer advancing state.")
                else -> StateManagerResult(ConsumeChar, "Transitioning to consume char state")
            }
        }

    },
    /*
        Scanning increments the current lookahead cursor/pointer
        and then appends the current lookahead char to the current lexeme builder
     */
    AdvanceScanner{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
            moduleInstance.lookaheadScanner.position++
//            println("Scanning: ${lexer.lookaheadScanner.lookaheadChar}")
            return StateManagerResult(true, "")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = StateManagerResult(Scanning, "Transitioning to scanning state")
    },
    ConsumeChar{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
            moduleInstance.currentLexeme.append(moduleInstance.lookaheadScanner.lookaheadChar)
            return StateManagerResult(true, "")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = StateManagerResult(AdvanceScanner, "Transitioning to scanner advancing state.")
    },
    /*
        End of line detected, so increment the lexer's line counter and continue scanning
     */
    EndOfLineDetected{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
//            println("New line detected!")
            moduleInstance.lineIdx++
            return StateManagerResult(true, "")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = StateManagerResult(LexerResetting, "Transitioning to resetting lexer state.")
    },
    /*
        Attempts to build a token from the current lexeme
     */
    BuildingToken{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
            val lexeme = moduleInstance.currentLexeme.toString()
//            println("Lexeme: $lexeme; position: ${lexer.column}")
            if(lexeme.isBlank()){
                return StateManagerResult(true, "")
            }
            KeywordTokenType.values().forEach {
                val regex = Regex(it.symbol)
                if(lexeme.matches(regex)){
                    moduleInstance.tokenStream.send(Token.KeywordToken(it, moduleInstance.tokenLocation))
                    return StateManagerResult(true, "")
                }
            }
            DelimitingTokenType.values().forEach {
                val regex = Regex(it.symbol)
                if(lexeme.matches(regex)){
                    moduleInstance.tokenStream.send(Token.DelimitingToken(it, moduleInstance.tokenLocation))
                    return StateManagerResult(true, "")
                }
            }
            if(lexeme.matches(Regex("[a-zA-Z][a-zA-Z0-9]+"))) {
                moduleInstance.tokenStream.send(Token.OtherToken.IdentifierToken(lexeme, moduleInstance.tokenLocation))
                return StateManagerResult(true, "")
            }else if(lexeme.matches(Regex("[0-9]+"))){
                moduleInstance.tokenStream.send(Token.OtherToken.IntegerLiteralToken(lexeme, moduleInstance.tokenLocation))
                return StateManagerResult(true, "")
            }
            return StateManagerResult(true, "Could not build a token!")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = StateManagerResult(BuiltToken, "Transitioning to built token state.")
    },
    /*
        Stores the current token in the lexer's list of tokens and clears the lexeme builder
     */
    BuiltToken{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
//            println("Built a token...adding it to token stream!")
            moduleInstance.currentLexeme.apply {
                if(this.isNotEmpty()) this.clear()
            }
            return StateManagerResult(true, "")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = StateManagerResult(LexerAdvancing, "Transitioning to advancing lexer state.")
    },
    /*
        Found the end of the file, so add the EOF token and return to the start state (making this state effectively final)
     */
    EndOfFileDetected{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
            moduleInstance.tokenStream.send(Token.OtherToken.EndOfFileToken(moduleInstance.tokenLocation))
            return StateManagerResult(true, "")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = StateManagerResult(Idle, "Transitioning to idle state.")
    },
    /*
        Reserved for error handling
     */
    Error{
        override suspend fun transitionTo(moduleInstance: Lexer): StateManagerResult<Boolean> {
            moduleInstance.errors += "Lexer encountered an error while scanning:"
            moduleInstance.errors += moduleInstance.toString()
            return StateManagerResult(true, "")
        }

        override suspend fun transitionFrom(moduleInstance: Lexer): StateManagerResult<State<Lexer>> = StateManagerResult(Error, "Staying in error state.")
    };
}