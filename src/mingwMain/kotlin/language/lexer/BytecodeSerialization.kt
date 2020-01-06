package language.lexer

import kotlinx.io.core.*
import language.serializer.bytestream.BytecodeConverter

class TokenLocationBytecodeConverter : BytecodeConverter<TokenLocation>(){
    override val startByte: Byte = 0xce.toByte()
    override val endByte: Byte = 0xcf.toByte()

    override fun serializeToBytes(tObj: TokenLocation): ByteReadPacket = buildPacket{
        this.writeByte(0xc1.toByte())
        this.writeStringUtf8(tObj.fileName)
        this.writeByte(0xc2.toByte())
        this.writeByte(0xc3.toByte())
        this.writeInt(tObj.line)
        this.writeByte(0xc4.toByte())
        this.writeByte(0xc5.toByte())
        this.writeInt(tObj.column)
        this.writeByte(0xc6.toByte())
    }

    override fun deserializeFromBytes(bytes: ByteReadPacket): TokenLocation? {
        val filePathStart = bytes.readByte()
        if(filePathStart != 0xc1.toByte()){
            return null
        }
        val filePathBytes = BytePacketBuilder()
        bytes.readUntilDelimiter(0xc2.toByte(), filePathBytes)
        val filePath = filePathBytes.build().readUTF8Line() ?: return null
        val filePathEnd = bytes.readByte()
        val lineNoStart = if(filePathEnd != 0xc2.toByte()){
            bytes.readByte()
        }else{
            filePathEnd
        }
        if(lineNoStart != 0xc3.toByte()){
            return null
        }
        val lineNo = bytes.readInt()
        val lineNoEnd = bytes.readByte()
        if(lineNoEnd != 0xc4.toByte()){
            return null
        }
        val columnNoStart = bytes.readByte()
        if(columnNoStart != 0xc5.toByte()){
            return null
        }
        val columnNo = bytes.readInt()
        val columnNoEnd = bytes.readByte()
        if(columnNoEnd != 0xc6.toByte()){
            return null
        }
        return TokenLocation(filePath, lineNo, columnNo)
    }

}

class TokenBytecodeConverter : BytecodeConverter<Token>() {
    override val startByte: Byte = 0xfe.toByte()
    override val endByte: Byte = 0xff.toByte()
    override fun serializeToBytes(tObj: Token): ByteReadPacket = buildPacket {
        val tokenLocationBytecodeConverter = TokenLocationBytecodeConverter()
        when(tObj){
            is Token.KeywordToken -> {
                this.writeByte(tObj.tokenType.highByte)
                this.writeByte(tObj.tokenType.byteId)
            }
            is Token.DelimitingToken -> {
                this.writeByte(tObj.tokenType.highByte)
                this.writeByte(tObj.tokenType.byteId)
            }
            is Token.OtherToken -> {
                this.writeByte(tObj.tokenType.highByte)
                this.writeByte(tObj.tokenType.byteId)
                this.writeByte(0xb1.toByte())
                when(tObj){
                    is Token.OtherToken.IntegerLiteralToken -> {
                        this.writeStringUtf8(tObj.symbol)
                    }
                    is Token.OtherToken.IdentifierToken -> {
                        this.writeStringUtf8(tObj.symbol)
                    }
                }
                this.writeByte(0xb2.toByte())
            }
        }
        this.writePacket(tokenLocationBytecodeConverter.serialize(tObj.tokenLocation))

    }

    override fun deserializeFromBytes(bytes: ByteReadPacket): Token? {
        val tokenLocationBytecodeConverter = TokenLocationBytecodeConverter()
        val tokenIdHighByte = bytes.readByte()
        val tokenIdLowByte = bytes.readByte()
        val tokenDataStart = bytes.readByte()
        if(tokenDataStart != 0xb1.toByte()){
            return null
        }
        val tokenData = buildPacket {
            bytes.readUntilDelimiter(0xb2.toByte(), this)
        }.readUTF8Line() ?: return null
        val tokenDataEnd = bytes.readByte()
        if(tokenDataEnd != 0xb2.toByte()){
            return null
        }
        val tokenLocation = tokenLocationBytecodeConverter.deserialize(bytes) ?: return null
        KeywordTokenType.values().forEach {
            if(it.byteId == tokenIdLowByte){
                return Token.KeywordToken(it, tokenLocation)
            }
        }
        DelimitingTokenType.values().forEach {
            if(it.byteId == tokenIdLowByte){
                return Token.DelimitingToken(it, tokenLocation)
            }
        }
        return when(tokenIdLowByte){
            OtherTokenType.IntegerToken.byteId -> {
                Token.OtherToken.IntegerLiteralToken(tokenData, tokenLocation)
            }
            OtherTokenType.IdentifierToken.byteId -> {
                Token.OtherToken.IdentifierToken(tokenData, tokenLocation)
            }
            OtherTokenType.EndOfFileToken.byteId -> {
                Token.OtherToken.EndOfFileToken(tokenLocation)
            }
            else -> null
        }
    }

}