enum class PrettyColors(val ansi: String){
    RED("\u001B[31m"),
    BLUE("\u001B[34m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    WHITE("\u001B[30m"),
    BLACK("\u001B[37m"),
    UNDERLINE("\u001B[04m"),
    BOLD("\u001B[01m"),
    ITALIC("\u001B[03m"),
    RESET("\u001B[0m")
    ;
}

@ExperimentalStdlibApi
class PrettyPrinter{
    private var indentationLevel = 0
    private val sb = StringBuilder()

    @ExperimentalStdlibApi
    private val styleStack = ArrayDeque<PrettyColors>()

    init {
        styleStack.addLast(PrettyColors.RESET)
    }

    fun append(char: Char){
        append(char.toString())
    }

    fun append(long: Long){
        append(long.toString())
    }

    fun append(int: Int){
        append(int.toString())
    }

    fun append(short: Short){
        append(short.toString())
    }

    @ExperimentalUnsignedTypes
    fun append(short: UByte){
        append(short.toString())
    }

    fun append(byte: Byte){
        append(byte.toString())
    }

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
                (1..this.indentationLevel).forEach {
                    this.sb.append("\t")
                }
                sb.append(string)
            }
        }else{
            this.sb.append(string)
        }
    }

    @ExperimentalStdlibApi
    fun red(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.RED.ansi)
        styleStack.addLast(PrettyColors.RED)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun green(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.GREEN.ansi)
        styleStack.addLast(PrettyColors.GREEN)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun blue(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.BLUE.ansi)
        styleStack.addLast(PrettyColors.BLUE)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun yellow(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.YELLOW.ansi)
        styleStack.addLast(PrettyColors.YELLOW)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun white(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.WHITE.ansi)
        styleStack.addLast(PrettyColors.WHITE)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun black(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.BLACK.ansi)
        styleStack.addLast(PrettyColors.BLACK)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun bold(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.BOLD.ansi)
        styleStack.addLast(PrettyColors.BOLD)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun italics(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.ITALIC.ansi)
        styleStack.addLast(PrettyColors.ITALIC)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun underline(block: PrettyPrinter.()->Unit){
        this.append(PrettyColors.UNDERLINE.ansi)
        styleStack.addLast(PrettyColors.UNDERLINE)
        this.block()
        styleStack.removeLast()
        this.append(styleStack.last().ansi)
    }

    fun appendWithNewLine(string: String){
        this.append("$string\n")
    }

    fun indent(block: PrettyPrinter.() -> Unit){
        this.indentationLevel++
        this.block()
        this.indentationLevel--
    }

    fun indentN(n: Int, block: PrettyPrinter.()->Unit){
        indentationLevel+=n
        block()
        indentationLevel-=n
    }

    fun spaced(n: Int, block: PrettyPrinter.()->Unit){
        spaced(n)
        block()
    }

    fun spaced(n: Int){
        for(i in 0..n){
            append(" ")
        }
    }

    infix fun Char.padded(n: Int){
        this padded 0..n
    }

    infix fun Char.padded(range: IntRange){
        val padding = buildPrettyString {
            for (i in range) {
                append(this@padded)
            }
        }
        append(padding)
    }

    override fun toString(): String = this.sb.toString()

}

@ExperimentalStdlibApi
fun buildPrettyString(block: PrettyPrinter.()->Unit): String{
    val prettyPrinter = PrettyPrinter()
    prettyPrinter.block()
    return prettyPrinter.toString()
}