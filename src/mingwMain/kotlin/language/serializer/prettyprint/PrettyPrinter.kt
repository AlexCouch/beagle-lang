package language.serializer.prettyprint

class PrettyPrinter{
    private var indentationLevel = 0
    private val sb = StringBuilder()

    fun append(string: String){
        if(indentationLevel > 0){
            if(string.contains("\n")) {
                val lines = string.split(Regex("(?<=\n)"))
                for(line in lines.withIndex()){
                    if(line.value.isNotBlank()) {
                        (1..this.indentationLevel).forEach {
                            this.sb.append("\t")
                        }
                    }
                    this.sb.append(line.value)
                }
            }else{
                sb.append(string)
            }
        }else{
            this.sb.append(string)
        }
    }

    fun appendWithNewLine(string: String){
        this.append("$string\n")
    }

    fun indent(block: PrettyPrinter.() -> Unit){
        this.indentationLevel++
        this.block()
        this.indentationLevel--
    }

    override fun toString(): String = this.sb.toString()

}

fun buildPrettyString(block: PrettyPrinter.()->Unit): String{
    val prettyPrinter = PrettyPrinter()
    prettyPrinter.block()
    return prettyPrinter.toString()
}
