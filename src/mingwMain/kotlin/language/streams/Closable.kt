package language.streams

interface Closable {
    fun close(): Int
}

interface Flushable{
    fun flush(): Int
}