package language.lexer.bytecode

import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream
import kotlinx.io.OutputStream
import kotlinx.io.charsets.MalformedInputException
import kotlinx.io.core.*
import kotlinx.serialization.*
import kotlinx.serialization.internal.HexConverter
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.getContextualOrDefault
import language.lexer.*
import kotlin.experimental.and

@Serializer(Token::class)
object TokenBytecodeSerializer: KSerializer<Token>{
    override fun deserialize(decoder: Decoder): Token {
        val compositeDecoder = decoder.beginStructure(this.descriptor) as TokenBytecode.TokenBytecodeReader
        val tokenType = compositeDecoder.decodeTokenType()
        var symbol = ""
        when(tokenType){
            is OtherTokenType -> when{
                tokenType == OtherTokenType.IdentifierToken || tokenType == OtherTokenType.IntegerToken -> {
                    symbol = compositeDecoder.decodeSymbol()
                }
            }
        }
        val tokenLocation = compositeDecoder.decodeTokenLocation()
        val token = when(tokenType){
            is KeywordTokenType -> {
                Token.KeywordToken(tokenType, tokenLocation)
            }
            is DelimitingTokenType -> {
                Token.DelimitingToken(tokenType, tokenLocation)
            }
            is OtherTokenType -> when(tokenType){
                OtherTokenType.IdentifierToken -> Token.OtherToken.IdentifierToken(symbol, tokenLocation)
                OtherTokenType.IntegerToken -> Token.OtherToken.IntegerLiteralToken(symbol, tokenLocation)
                OtherTokenType.EndOfFileToken -> Token.OtherToken.EndOfFileToken(tokenLocation)
            }
            else -> throw MalformedInputException("Could not decode token")
        }
        compositeDecoder.endStructure(this.descriptor)
        return token
    }

    override val descriptor: SerialDescriptor
        get() = SerialClassDescImpl("Token")

    @InternalSerializationApi
    override fun serialize(encoder: Encoder, obj: Token) {
        val compositeEncoder = encoder.beginStructure(this.descriptor, this) as TokenBytecode.TokenBytecodeWriter
        when(obj){
            is Token.KeywordToken -> {
                compositeEncoder.encodeTokenID(obj.tokenType.highByte, obj.tokenType.byteId)
            }
            is Token.DelimitingToken -> {
                compositeEncoder.encodeTokenID(obj.tokenType.highByte, obj.tokenType.byteId)
            }
            is Token.OtherToken -> {
                compositeEncoder.encodeTokenID(obj.tokenType.highByte, obj.tokenType.byteId)
                when(obj){
                    is Token.OtherToken.IntegerLiteralToken -> {
                        compositeEncoder.encodeSymbol(obj.symbol)
                    }
                    is Token.OtherToken.IdentifierToken -> {
                        compositeEncoder.encodeSymbol(obj.symbol)
                    }
                }
            }
        }
        compositeEncoder.encodeTokenLocation(obj.tokenLocation)
        compositeEncoder.endStructure(this.descriptor)
    }

}


class TokenBytecode internal constructor(override val context: SerialModule = EmptyModule): AbstractSerialFormat(context), BinaryFormat{
    inner class TokenBytecodeEncoder(val output: BytePacketBuilder){

        fun startToken(){
            output.writeByte((BEGIN_TOKEN and 0xff).toByte())
        }
        fun endToken() = output.writeByte((END_TOKEN and 0xff).toByte())
        fun startData(){
            output.writeByte((BEGIN_DATA and 0xff).toByte())
        }
        fun encodeData(data: String){
            output.writeText(data)
        }
        fun endData() = output.writeByte((END_DATA and 0xff).toByte())
        fun encodeTokenLocation(tokenLocation: TokenLocation){
            TokenLocationBytecodeEncoder(this.output){
                this.encodeFilePath(tokenLocation.fileName)
                this.encodeLineNo(tokenLocation.line)
                this.encodeColumnNo(tokenLocation.column)
            }
        }
    }

    inner class TokenLocationBytecodeEncoder internal constructor(private val output: BytePacketBuilder, block: TokenLocationBytecodeEncoder.()->Unit){
        init{
            this.startTokenLocation(block)
            this.endTokenLocation()
        }

        private fun startTokenLocation(block: TokenLocationBytecodeEncoder.()->Unit){
            output.writeByte((BEGIN_TOKEN_LOC and 0xff).toByte())
            this.block()
        }
        private fun endTokenLocation(){
            output.writeByte((END_TOKEN_LOC and 0xff).toByte())
        }
        private fun startFilePath(block: TokenLocationBytecodeEncoder.()->Unit){
            output.writeByte((BEGIN_TOKEN_LOC_FILE_PATH and 0xff).toByte())
            this.block()
        }
        fun encodeFilePath(filePath: String){
            this.startFilePath {
                output.writeStringUtf8(filePath)
            }
            this.endFilePath()
        }
        private fun endFilePath(){
            output.writeByte((END_TOKEN_LOC_FILE_PATH and 0xff).toByte())
        }
        private fun startLineNo(block: TokenLocationBytecodeEncoder.()->Unit){
            output.writeByte((BEGIN_TOKEN_LOC_LINE_NO and 0xff).toByte())
            this.block()
        }
        fun encodeLineNo(lineNo: Int){
            this.startLineNo {
                output.writeInt(lineNo)
            }
            this.endLineNo()
        }
        private fun endLineNo(){
            output.writeByte((END_TOKEN_LOC_LINE_NO and 0xff).toByte())
        }
        private fun startColumnNo(block: TokenLocationBytecodeEncoder.()->Unit){
            output.writeByte((BEGIN_TOKEN_LOC_COLUMN_NO and 0xff).toByte())
            this.block()
        }
        fun encodeColumnNo(columnNo: Int){
            this.startColumnNo {
                output.writeInt(columnNo)
            }
            this.endColumnNo()
        }
        private fun endColumnNo(){
            output.writeByte((END_TOKEN_LOC_COLUMN_NO and 0xff).toByte())
        }
    }

    inner class TokenBytecodeWriter internal constructor(val encoder: TokenBytecodeEncoder): ElementValueEncoder() {
        override val context: SerialModule
            get() = this@TokenBytecode.context

        override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeEncoder {
            this.encoder.startToken()
            return this
        }

        override fun endStructure(desc: SerialDescriptor) {
            this.encoder.endToken()
        }

        override fun encodeByte(value: Byte) {
            this.encoder.output.writeByte(value)
        }

        fun encodeTokenID(highByte: Byte, lowByte: Byte){
            this.encodeByte(highByte)
            this.encodeByte(lowByte)
        }

        fun encodeSymbol(symbol: String){
            this.encoder.startData()
            this.encoder.encodeData(symbol)
            this.encoder.endData()
        }

        fun encodeTokenLocation(tokenLocation: TokenLocation){
            this.encoder.encodeTokenLocation(tokenLocation)
        }
    }

    inner class TokenBytecodeReader internal constructor(val decoder: TokenBytecodeDecoder): ElementValueDecoder(){
        override fun beginStructure(desc: SerialDescriptor, vararg typeParams: KSerializer<*>): CompositeDecoder {
            val currentByte = this.decoder.readByte()
            println(HexConverter.printHexBinary(byteArrayOf(currentByte)))
            if(currentByte != (BEGIN_TOKEN and 0xff).toByte()){
                throw MalformedInputException("Tried to begin structure at the wrong place in input")
            }
            return this
        }

        override fun endStructure(desc: SerialDescriptor) {
            if(this.decoder.readByte() != END_TOKEN.toByte()){
                throw MalformedInputException("Tried to end structure at the wrong place in input")
            }
        }

        override fun decodeString(): String = this.decodeSymbol()

        fun decodeSymbol(): String = this.decoder.readSymbol()

        fun decodeTokenType(): TokenType {
            val highByte = this.decoder.readByte()
            val lowByte = this.decoder.readByte()
            KeywordTokenType.values().forEach {
                if(highByte == it.highByte){
                    if(lowByte == it.byteId){
                        return it
                    }
                }
            }

            DelimitingTokenType.values().forEach {
                if(highByte == it.highByte){
                    if(lowByte == it.byteId){
                        return it
                    }
                }
            }

            OtherTokenType.values().forEach {
                if(highByte == it.highByte){
                    if(lowByte == it.byteId){
                        return it
                    }
                }
            }
            throw MalformedInputException("Invalid token id: ${HexConverter.printHexBinary(byteArrayOf(highByte, lowByte))}")
        }

        fun decodeTokenLocation() = this.decoder.readTokenLocation()

        fun canRead() = this.decoder.input.canRead()
    }

    inner class TokenBytecodeDecoder internal constructor(val input: ByteReadPacket){
        private var curByte: Byte = -1
            get(){
                println(field)
                return field
            }

        fun readByte(): Byte{
            this.curByte = this.input.readByte()
            return curByte
        }

        fun readSymbol(): String{
            this.readByte()
            var symbol = ""
            if(this.curByte == BEGIN_DATA.toByte()){
                symbol = this.input.readStringUntilDelimiter((END_DATA and 0xff).toByte())
            }
            this.readByte()
            return symbol
        }

        fun readTokenLocation(): TokenLocation{
            var filePath = ""
            var lineNo = -1
            var columnNo = -1
            TokenLocationDecoder(this.input){
                filePath = this.readFilePath()
                lineNo = this.readLineNo()
                columnNo = this.readColumnNo()
            }
            this.readByte()
            return TokenLocation(filePath, lineNo, columnNo)
        }

    }

    inner class TokenLocationDecoder internal constructor(val input: ByteReadPacket, block: TokenLocationDecoder.() -> Unit){
        private var curByte: Byte = this.input.readByte()

        init{
            this.block()
        }

        fun readByte(): Byte{
            this.curByte = this.input.readByte()
            return this.curByte
        }

        fun readFilePath(): String{
            println(this.curByte)
            if(this.curByte != BEGIN_TOKEN_LOC_FILE_PATH.toByte()){
                if(this.input.tryPeek() != BEGIN_TOKEN_LOC_FILE_PATH){
                    throw MalformedInputException("Could not find file path start byte while trying to decode token location file path")
                }
                this.readByte()
            }
            println(HexConverter.printHexBinary(this.input.copy().readBytes()))
            val filePath = this.input.readStringUntilDelimiter((END_TOKEN_LOC_FILE_PATH and 0xff).toByte())
            this.readByte()
            return filePath
        }

        fun readLineNo(): Int{
            if(this.curByte != BEGIN_TOKEN_LOC_LINE_NO.toByte()){
                if(this.input.tryPeek() != BEGIN_TOKEN_LOC_LINE_NO){
                    throw MalformedInputException("Could not find token location line number in input")
                }
                this.readByte()
            }
            val lineNo = this.input.readInt()
            this.readByte()
            return lineNo
        }

        fun readColumnNo(): Int{
            if(this.curByte != BEGIN_TOKEN_LOC_COLUMN_NO.toByte()){
                if(this.input.tryPeek() != BEGIN_TOKEN_LOC_COLUMN_NO){
                    throw MalformedInputException("Could not find token location column number in input")
                }
                this.readByte()
            }
            val columnNo = this.input.readInt()
            this.readByte()
            return columnNo
        }
    }

    companion object: BinaryFormat{
        private const val BEGIN_TOKEN = 0xfe
        private const val END_TOKEN = 0xff
        private const val BEGIN_DATA = 0xb1
        private const val END_DATA = 0xb2
        private const val BEGIN_TOKEN_LOC = 0xc0
        private const val BEGIN_TOKEN_LOC_FILE_PATH = 0xc1
        private const val END_TOKEN_LOC_FILE_PATH = 0xc2
        private const val BEGIN_TOKEN_LOC_LINE_NO = 0xc3
        private const val END_TOKEN_LOC_LINE_NO = 0xc4
        private const val BEGIN_TOKEN_LOC_COLUMN_NO = 0xc5
        private const val END_TOKEN_LOC_COLUMN_NO = 0xc6
        private const val END_TOKEN_LOC = 0xcf

        private val plain = TokenBytecode()
        override val context: SerialModule = plain.context

        override fun <T> dump(serializer: SerializationStrategy<T>, obj: T): ByteArray = plain.dump(serializer, obj)
        override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T = plain.load(deserializer, bytes)

    }

    override fun <T> dump(serializer: SerializationStrategy<T>, obj: T): ByteArray {
        val bytePacket = BytePacketBuilder()
        val dumper = TokenBytecodeWriter(TokenBytecodeEncoder(bytePacket))
        dumper.encode(serializer, obj)
        return bytePacket.build().readBytes()
    }

    override fun <T> load(deserializer: DeserializationStrategy<T>, bytes: ByteArray): T {
        val stream = ByteReadPacket(bytes)
        val reader = TokenBytecodeReader(TokenBytecodeDecoder(stream))
        return reader.decode(deserializer)
    }

    @ImplicitReflectionSerializer
    fun loadAll(bytes: ByteArray): List<Token>{
        val tokens = arrayListOf<Token>()
        val stream = ByteReadPacket(bytes)
        val reader = TokenBytecodeReader(TokenBytecodeDecoder(stream))
        while(reader.canRead()){
            tokens.add(reader.decode())
        }
        return tokens.toList()
    }
}

fun Input.readStringUntilDelimiter(delimiter: Byte): String = buildString{
    var byte: Byte
    while(true){
        byte = this@readStringUntilDelimiter.tryPeek().toByte()
        if(byte == delimiter){
            break
        }
        byte = this@readStringUntilDelimiter.readByte()
        this.append(byte.toChar())
    }
}