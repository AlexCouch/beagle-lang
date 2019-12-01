package language

import language.streams.Closable
import language.streams.FileStream
import language.streams.Flushable

interface OutputStream<T>{
    val stream: Array<T>
    fun write(c: T): Int
}

abstract class StringOutputStream : OutputStream<Char>{
    open fun writeStr(str: String): Int{
        for(s in str){
            val res = this.write(s)
            if(res != 0) return res
        }
        return 0
    }

    open fun writeByteArray(byteArray: Array<Byte>): Int{
        for(b in byteArray){
            val res = this.write(b.toChar())
            if (res != 0) return res
        }

        return 0
    }

    open fun writeCharArray(charArray: Array<Char>): Int{
        for(c in charArray){
            val res = this.write(c)
            if(res != 0) return res
        }
        return 0
    }
}

