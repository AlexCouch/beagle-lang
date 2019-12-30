package language

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.core.BytePacketBuilder
import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import language.lexer.LexerStateManager
import language.lexer.Token
import language.serializer.bytestream.*
import language.streams.FileInputStream
import language.streams.ScannerInputStream
import language.streams.StringInputStream
import language.streams.StringLiteralInputStream
import kotlin.experimental.and

fun main() = runBlocking<Unit>{
    val file = FileInputStream("test.bg")
    val lexerStateManager = LexerStateManager(file)

    launch{
        lexerStateManager.start()
        lexerStateManager.module.tokenStream.close()
    }
    launch{
        val bytecode = buildPacket {
            runBlocking {
                lexerStateManager.module.tokenStream.consumeEach {
                    this@buildPacket.writePacket(it.toBytes())
                }
            }
        }
        val str = bytecode.readBytes().joinToString {
            (0xff and it.toInt()).toString(16).padStart(2, '0')
        }
        println(str)
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