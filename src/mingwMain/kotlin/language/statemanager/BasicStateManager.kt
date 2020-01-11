package language.statemanager

import kotlinx.coroutines.flow.*

data class StateStackEntry(val stateName: String, val resultStr: String)
data class StateManagerResult<out T>(val data: T, val message: String)
interface BasicState<in T>: BiResultState<T, Boolean, BasicState<T>>
interface BiResultState<in T, out R, out V>{
    suspend fun transitionTo(moduleInstance: T): StateManagerResult<R>
    suspend fun transitionFrom(moduleInstance: T): StateManagerResult<V>
}

interface StateManager<T>{
    val stateStack: ArrayList<StateStackEntry>
    var currentState: T
    val finalStates: List<T>
}

abstract class BasicStateManager<T>: StateManager<BasicState<T>>{
    override val stateStack: ArrayList<StateStackEntry> = arrayListOf()
    internal abstract val module: T

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
                    messageBuilder.append(propogateResult.message)
                    break
                }
                messageBuilder.append(propogateResult.message)
                val predicateResult = this@BasicStateManager.currentState.transitionFrom(this@BasicStateManager.module)
                messageBuilder.append("Predication result:\n")
                messageBuilder.append("\t${predicateResult.message}\n")
                this@BasicStateManager.currentState = predicateResult.data
                emit(StateStackEntry(currentState::class.qualifiedName ?: "Unknown", messageBuilder.toString()))
            }
        }
        stateStackFlow.buffer().collect{
//            println(it.resultStr)
            this.stateStack.add(it)
        }
    }
}