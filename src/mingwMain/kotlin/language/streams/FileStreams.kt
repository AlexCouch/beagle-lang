package language.streams

import kotlinx.cinterop.*
import language.StringOutputStream
import platform.posix.*

interface FileStream : Closable{
    val path: String
    val targetFile: FILE
}

class FileOutputStream(override val path: String) : StringOutputStream(), FileStream, Flushable{
    override val stream: Array<Char> = arrayOf()

    override val targetFile: FILE = fopen(path, "w+")?.pointed ?: throw IllegalStateException("Could not open file with path $path")

    override fun write(c: Char): Int = fputc(c.toInt(), this.targetFile.ptr)

    override fun close(): Int = fclose(this.targetFile.ptr)

    override fun flush(): Int = fflush(this.targetFile.ptr)

}

class FileInputStream(override val path: String) : StringInputStream(), FileStream{
    override val targetFile: FILE = fopen(this.path, "r")?.pointed ?: throw IllegalStateException("Could not open file with path $path")

    override fun close(): Int = fclose(this.targetFile.ptr)
    override fun read(): Int{
        if(ftell(this.targetFile.ptr) == EOF){
            fseek(this.targetFile.ptr, 0, SEEK_SET)
        }
        return fgetc(this.targetFile.ptr)
    }
}