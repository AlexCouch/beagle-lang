package language

import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.core.*
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.dump
import kotlinx.serialization.internal.HexConverter
import language.lexer.LexerStateManager
import language.lexer.bytecode.TokenBytecode
import language.streams.FileInputStream

@ImplicitReflectionSerializer
fun main() = runBlocking<Unit>{
    val file = FileInputStream("test.bg")
    val lexerStateManager = LexerStateManager(file)

    launch{
        lexerStateManager.start()
        lexerStateManager.module.tokenStream.close()
    }
    val tokenBytecode = TokenBytecode()
    flow{
        val bytecode = buildPacket {
            runBlocking {
                lexerStateManager.module.tokenStream.consumeEach {
                    val bytecodeDump = tokenBytecode.dump(it)
                    this@buildPacket.writeFully(bytecodeDump, 0, bytecodeDump.size)
                }
            }
        }
        emit(bytecode)
    }.collect{
        val bytecode = it.readBytes()
        println(HexConverter.printHexBinary(it.copy().readBytes()))
        val tokens = tokenBytecode.loadAll(bytecode)
        tokens.forEach(::println)
    }

    /*val output = buildPacket {
        this.writeStringUtf8("Hello. World!")
        this.writeByte(0xc2.toByte())
    }
    val string = buildString{
        var byte = output.readByte()
        println((0xc2 and 0xff).toByte())
        while(byte != (0xc2 and 0xff).toByte()){
            println(HexConverter.toHexString(byte.toInt() and 0xff))
            this.append(byte.toChar())
            byte = output.readByte()
        }
    }
    println(string)*/
}