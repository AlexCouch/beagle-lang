package language.serializer.bytestream

import kotlinx.io.core.*
import language.lexer.Token
import kotlin.experimental.and

interface Serializable<T>{
    fun toBytes(): ByteReadPacket
}

typealias OnByteBlock = BytesDeserializer.()->Unit
typealias StringDeserializeBlock = BytesDeserializer.(StringBuilder)->Unit
typealias IntDeserializeBlock = BytesDeserializer.(ByteArray)->Unit

class BytesDeserializer(private val byteReadPacket: ByteReadPacket){
    private val onByteBlocks = hashMapOf<Byte, OnByteBlock>()

    fun onByte(byte: Byte, block: OnByteBlock){
        this.onByteBlocks[byte] = block
    }

    fun deserializeString(): String{
        var byte = this.byteReadPacket.readByte()
//        while(byte != 0x51)
    }

    fun deserializeInt(): Int{

    }
}

fun <T> deserializeBytes(block: BytesDeserializer.()->T){

}