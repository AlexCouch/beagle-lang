package language.statemanager

import kotlinx.coroutines.flow.*

data class StateStackEntry(val stateName: String, val resultStr: String)
data class StateManagerResult<out T>(val data: T, val message: String)
interface State<in T>{
    suspend fun transitionTo(moduleInstance: T): StateManagerResult<Boolean>
    suspend fun transitionFrom(moduleInstance: T): StateManagerResult<State<T>>
}

abstract class StateManager<T>{
    internal val stateStack = arrayListOf<StateStackEntry>()
    internal abstract var currentState: State<T>
    internal abstract val module: T
    internal abstract val finalStates: List<State<T>>

    suspend fun start(){
        val stateStackFlow = flow{
//            println("Starting state transition")
            while(!finalStates.contains(currentState)) {
                val propogateResult = currentState.transitionTo(module)
                val messageBuilder = StringBuilder()
                messageBuilder.append("Propogation result:\n")
                //Failed to transition to this state
                if (!propogateResult.data) {
                    messageBuilder.append("\tFailed to transition to ${currentState::class.qualifiedName}\n")
                    messageBuilder.append("\t\tDetails:\n")
                }
                messageBuilder.append(propogateResult.message)
                val predicateResult = this@StateManager.currentState.transitionFrom(this@StateManager.module)
                messageBuilder.append("Predication result:\n")
                messageBuilder.append("\t${predicateResult.message}\n")
                this@StateManager.currentState = predicateResult.data
                emit(StateStackEntry(currentState::class.qualifiedName ?: "Unknown", messageBuilder.toString()))
            }
        }
        stateStackFlow.buffer().collect{
//            println(it.resultStr)
            this.stateStack.add(it)
        }
    }
}