package language.lexer

import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import language.statemanager.State
import language.statemanager.StateManager
import language.streams.FileInputStream

class LexerStateManager private constructor(override val module: Lexer): StateManager<Lexer>(){
    override var currentState: State<Lexer> = LexerState.Idle
    override val finalStates: List<State<Lexer>> = arrayListOf(LexerState.EndOfFileDetected, LexerState.Error)

    constructor(fileInputStream: FileInputStream) : this(Lexer(fileInputStream))

    fun collectTokens() = this.module.tokenFlow
}