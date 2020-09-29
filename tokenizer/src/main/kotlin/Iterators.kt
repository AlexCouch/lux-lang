import arrow.core.*

class TokenStream(val input: String): Iterator<Option<Token>>{
    private val tokens = arrayListOf<Token>()
    private var idx = -1
    val current get() = if(idx >= 0) tokens[idx].some() else none()
    private var checkpoints: ArrayList<Int> = arrayListOf()

    override fun hasNext(): Boolean = idx+1 < tokens.size
    override fun next() = if(hasNext()) tokens[++idx].some() else none()
    val peek get() = if(hasNext()) tokens[idx+1].some() else none()

    operator fun plus(token: Token) = tokens.add(token)

    fun checkpoint(){
        this.checkpoints.add(this.idx)
    }

    fun reset(){
        if(checkpoints.isEmpty()){
            return
        }
        this.idx = this.checkpoints.last()
    }
    fun popCheckpoint(){
        this.checkpoints.remove(this.checkpoints.lastIndex)
        this.reset()
    }
}

class Scanner(val input: String): Iterator<Option<Char>>{
    var idx = 0
        private set

    var current = input[idx].some()
        private set

    val peek get() = if(hasNext()) input[idx+1].some() else none()

    override fun hasNext(): Boolean = idx+1 < input.length
    override fun next(): Option<Char> {
        current = if(hasNext()) input[++idx].some() else none()
        return current
    }

}

fun main(){

}