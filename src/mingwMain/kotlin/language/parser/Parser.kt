package language.parser

import language.lexer.Token

sealed class ParseState{
    abstract val validTokens: Array<String>
    object TopLevel : ParseState() {
        override val validTokens: Array<String> = arrayOf("DEFINITION", "IDENTIFIER")
    }
    class DefinitionState() : ParseState(){
        override val validTokens: Array<String> = arrayOf("IDENTIFIER")
    }
    class ReferenceState() : ParseState() {
        override val validTokens: Array<String> = arrayOf("ASSIGNMENT", "ADDITION", "SUBTRACT")
    }
    abstract class EvaluationState<T>(open val valueToEval: T) : ParseState()
    data class IntegerEvaluationState(override val valueToEval: Int) : EvaluationState<Int>(valueToEval) {
        override val validTokens: Array<String> = arrayOf()
    }
    data class FloatEvaluationState(override val valueToEval: Float) : EvaluationState<Float>(valueToEval) {
        override val validTokens: Array<String> = arrayOf()
    }
}


//enum class ParseState{
//    Assignment{
//        override val validTokens: Array<KClass<out Token>> = arrayOf(IdentToken::class, DefToken::class, IntegerToken::class, FloatToken::class)
//
//        override val childStates: Array<ParseState> = arrayOf()
//
//    },
//    Definition{
//        override val validTokens: Array<KClass<out Token>> = arrayOf(IdentToken::class, AssignToken::class)
//
//        override val childStates: Array<ParseState> = arrayOf(Assignment)
//    },
//    Reference{ //Expand as needed for functions, classes, properties, etc
//        override val validTokens: Array<KClass<out Token>> = arrayOf(DefToken::class, IdentToken::class)
//
//        override val childStates: Array<ParseState> = arrayOf(Definition, Assignment)
//
//    },
//    TopLevel{
//        override val validTokens: Array<KClass<out Token>> = arrayOf(DefToken::class, IdentToken::class)
//        override val childStates: Array<ParseState> = arrayOf(Definition, Reference)
//    }
//    ;
//
//    abstract val childStates: Array<ParseState>
//    abstract val validTokens: Array<KClass<out Token>>
//}

class Parser{
    private var parserState = ParseState.TopLevel

    private fun tryNextState(token: Token): Boolean{


        return true
    }

    fun parse(tokens: List<Token>){
        for(token in tokens){

        }
    }
}