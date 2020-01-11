package language.serializer.bytestream

import kotlinx.io.core.*

abstract class BytecodeConverter<T>{
    abstract val startByte: Byte
    abstract val endByte: Byte
    var module: T? = null
        private set
    protected abstract fun serializeToBytes(tObj: T): ByteReadPacket
    protected abstract fun deserializeFromBytes(bytes: ByteReadPacket): T?
    fun serialize(tObj: T): ByteReadPacket = buildPacket{
        this.writeByte(this@BytecodeConverter.startByte)
        this.writePacket(this@BytecodeConverter.serializeToBytes(tObj))
        this.writeByte(this@BytecodeConverter.endByte)
    }
    fun deserialize(bytes: ByteReadPacket): List<T?>{
        val retList = arrayListOf<T?>()

        while(bytes.canRead()) {
            if (bytes.readByte() == this.startByte) {
                val tokenBytes = BytePacketBuilder()
                bytes.readUntilDelimiter(this.endByte, tokenBytes)
                val result = this.deserializeFromBytes(tokenBytes.build())
                retList += result
            }
        }

        return retList
    }
}