package language.error

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import language.serializer.prettyprint.PrettyPrinter
import language.serializer.prettyprint.buildPrettyString

data class ErrorEntry(val moduleName: String, val message: String)
abstract class ErrorManager<T>{
    protected val errorStream = Channel<ErrorEntry>()

    abstract val module: T

    fun start(){
        GlobalScope.launch {
            while(!errorStream.isClosedForSend){
                val errorEntry = errorStream.receive()
                val (moduleName, message) = errorEntry
                val errorMessage = buildPrettyString {
                    this.appendWithNewLine("An error occurred in $moduleName:")
                    this.indent {
                        this.appendWithNewLine(message)
                    }
                }
                println(errorMessage)
            }
        }

    }

    abstract suspend fun createError(moduleName: String, message: String)

    fun stop(){
        this.errorStream.close()
    }
}