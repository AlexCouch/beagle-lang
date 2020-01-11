package language

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import language.lexer.LexerStateManager
import language.lexer.TokenBytecodeConverter
import language.lexer.toHexString
import language.streams.FileInputStream

fun main() = runBlocking<Unit>{
    val file = FileInputStream("test.bg")
    val lexerStateManager = LexerStateManager(file)

    launch{
        lexerStateManager.start()
        lexerStateManager.module.tokenStream.close()
    }
    flow<ByteReadPacket>{
        val bytecode = buildPacket {
            runBlocking {
                lexerStateManager.module.tokenStream.consumeEach {
                    val tokenBytecodeConverter = TokenBytecodeConverter()
                    this@buildPacket.writePacket(tokenBytecodeConverter.serialize(it))
                }
            }
        }
        emit(bytecode)
    }.collect{
        println(it.copy().readBytes().toUByteArray().toHexString())
        val tokenBytecodeConverter = TokenBytecodeConverter()
        val tokens = tokenBytecodeConverter.deserialize(it)
        tokens.forEach(::println)
    }
    /*val bytecode = buildBytePacket{
        launch{

        }
    }*/
    /*println(bytecode.remaining)
    bytecode.readBytes().forEach {
        print(it and 0xff.toByte())
    }*/
}