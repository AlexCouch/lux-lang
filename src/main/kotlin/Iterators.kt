import arrow.core.*
import arrow.core.Either
import arrow.optics.Lens
import arrow.optics.Prism

class TokenStream(val input: String): Iterator<Option<Token>>{
    private val tokens = arrayListOf<Token>()
    private var idx = -1
    val current get() = if(idx >= 0) if(hasNext()) tokens[++idx].some() else none() else none()

    override fun hasNext(): Boolean = idx+1 < tokens.size
    override fun next() = if(hasNext()) tokens[++idx].some() else none()
    val peek get() = if(hasNext()) tokens[idx+1].some() else none()

    operator fun plus(token: Token) = tokens.add(token)
}

class Scanner(val input: String): Iterator<Option<Char>>{
    var idx = 0
        private set

    val current get() = if(hasNext()) input[idx].some() else none()
    val peek get() = if(hasNext()) input[idx+1].some() else none()

    override fun hasNext(): Boolean = idx+1 < input.length
    override fun next() = if(hasNext()) input[++idx].some() else none()

}

data class InputScanner(val input: String, val pos: Int)
val scanner = Prism(
    getOrModify = {scanner: InputScanner ->
        if(scanner.pos >= scanner.input.length) scanner.input[scanner.pos].some().left() else Either.right()
    },
    reverseGet = {scanner: InputScanner ->
        scanner
    }
)

fun main(){

}