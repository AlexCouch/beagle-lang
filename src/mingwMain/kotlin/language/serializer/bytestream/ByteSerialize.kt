package language.serializer.bytestream

import kotlinx.io.core.*
import language.lexer.Token
import language.statemanager.State
import language.statemanager.StateManager
import language.statemanager.StateManagerResult
import kotlin.experimental.and

abstract class BytecodeConverter<T>{
    abstract val startByte: Byte
    abstract val endByte: Byte
    var module: T? = null
        private set
    protected abstract fun serializeToBytes(tObj: T): ByteReadPacket
    protected abstract fun deserializeFromBytes(bytes: ByteReadPacket): T?
    fun serialize(tObj: T): ByteReadPacket = buildPacket{

    }
    fun deserialize(bytes: ByteReadPacket): T?{
        val startByte = bytes.readByte()
        if(startByte != this.startByte){
            return null
        }
        if(!bytes.hasBytes(this.endByte.toInt())){
            return null
        }
        val tPacket = BytePacketBuilder()
        bytes.readUntilDelimiter(this.endByte, tPacket)
        return this.deserializeFromBytes(tPacket.build())
    }
}

class BytecodeConverterManager<T, R>(override val module: T): StateManager<T>() where T: BytecodeConverter<R>{
    override var currentState: State<T> = ByteConversionState.Idle
    override val finalStates: List<State<T>> = arrayListOf()
}

enum class ByteConversionState: State<BytecodeConverter<*>>{
    Idle{
        override suspend fun transitionTo(moduleInstance: BytecodeConverter<*>): StateManagerResult<Boolean> {

        }

        override suspend fun transitionFrom(moduleInstance: BytecodeConverter<*>): StateManagerResult<State<BytecodeConverter<*>>> {
        }

    },
    ByteRead{
        override suspend fun transitionTo(moduleInstance: BytecodeConverter<*>): StateManagerResult<Boolean> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override suspend fun transitionFrom(moduleInstance: BytecodeConverter<*>): StateManagerResult<State<BytecodeConverter<*>>> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    },
    ByteWrite{

    },
    StringRead{

    },
    StringWrite{

    },
    IntRead{

    },
    IntWrite{

    }
}