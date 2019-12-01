package language.streams

import kotlinx.cinterop.*
import platform.posix.EOF
import platform.posix.scanf

class ScannerInputStream : InputStream<String>(){
    override fun read(): String{
        val ret = nativeHeap.allocArray<ByteVar>(65535)
        scanf("%s", ret)
        return ret.toKString()
    }
}