package language.streams

import platform.posix.EOF

abstract class InputStream<T>{
    abstract fun read(): T
}

abstract class StringInputStream : InputStream<Int>(){
    fun readStr(): String{
        var ret = ""
        do{
            val c = this.read()
            ret += c.toChar()
        } while(c != EOF)
        return ret.subSequence(0..ret.length-2).toString()
    }
    fun readCharArray(): CharArray = this.readStr().toCharArray()
    fun readByteArray(): ByteArray = this.readStr().encodeToByteArray()
}

class StringLiteralInputStream(private val string: String) : StringInputStream(){
    private var position = -1
        set(p){
            field = if(p >= this.string.length){
                 -1
            }else{
                p
            }
        }

    override fun read(): Int {
        this.position++
        return if(position == -1) EOF else this.string[this.position].toInt()
    }

}

