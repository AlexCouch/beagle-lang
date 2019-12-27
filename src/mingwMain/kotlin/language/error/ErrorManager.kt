package language.error

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class ErrorEntry(val moduleName: String, val message: String)
abstract class ErrorManager<T>{
    protected val errorStream = Channel<ErrorEntry>()

    abstract val module: T

    fun start(){
        GlobalScope.launch {
            while(!errorStream.isClosedForSend){
                val errorEntry = errorStream.receive()
                val (moduleName, message) = errorEntry
                val errorStringBuilder = StringBuilder()
                errorStringBuilder.append("An error occurred in $moduleName:\n")
                errorStringBuilder.append("\t$message")
                println(errorStringBuilder.toString())
            }
        }

    }

    abstract suspend fun createError(moduleName: String, message: String)

    fun stop(){
        this.errorStream.close()
    }
}