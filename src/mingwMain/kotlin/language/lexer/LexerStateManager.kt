package language.lexer

import language.statemanager.BasicState
import language.statemanager.BasicStateManager
import language.streams.FileInputStream
import language.streams.StringInputStream

class LexerStateManager private constructor(override val module: Lexer): BasicStateManager<Lexer>(){
    override var currentState: BasicState<Lexer> = LexerState.Idle
    override val finalStates: List<LexerState> = listOf(LexerState.EndOfFileDetected, LexerState.Error)

    constructor(fileInputStream: FileInputStream) : this(Lexer(fileInputStream))
    constructor(strInputStream: StringInputStream): this(Lexer(strInputStream))
}