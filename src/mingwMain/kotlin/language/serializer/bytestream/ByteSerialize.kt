package language.serializer.bytestream

import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import language.lexer.Token
import kotlin.experimental.and

interface Serializable{
    fun toBytes(): ByteReadPacket
}