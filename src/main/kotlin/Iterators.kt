class TokenStream: Iterator<Token>{
    private val tokens = arrayListOf<Token>()
    private var idx = -1
    val current get() = if(idx >= 0) tokens[idx] else null

    override fun hasNext(): Boolean = peek != null
    override fun next(): Token = tokens[++idx]
    val peek get() = tokens.getOrNull(idx+1)

    operator fun plus(token: Token) = tokens.add(token)
}

class Scanner(val input: String): Iterator<Char?>{
    var idx = 0
        private set

    val current get() = input.getOrNull(idx)
    val peek get() = input.getOrNull(idx+1)

    override fun hasNext(): Boolean = peek != null
    override fun next(): Char? = input.getOrNull(++idx)

}