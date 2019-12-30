package language.serializer.prettyprint

class PrettyPrinter{
    private var indentationLevel = 0
    private val sb = StringBuilder()

    fun append(string: String){
        println(this.indentationLevel)
        if(indentationLevel > 0){
            if(string.contains("\n")) {
                val lines = string.split("\n")
                lines.withIndex().forEach { (index, it) ->
                    (1..this.indentationLevel).forEach { _ ->
                        this.sb.append("\t")
                    }
                    sb.append(it)
                    if (index < lines.size - 1) {
                        sb.append("\n")
                    }
                }
            }else{
                (1..this.indentationLevel).forEach{_ ->
                    this.sb.append("\t")
                }
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
