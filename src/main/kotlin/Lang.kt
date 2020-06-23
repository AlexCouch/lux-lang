import java.io.File

sealed class Either<out T>{
    data class Some<T>(val t: T): Either<T>()
    object None : Either<Nothing>()

    fun unwrap() =
        if(this is Some<*>){
            this.t
        }else{
            throw IllegalStateException("Attempted to unwrap Either but was None")
        }
}

fun main(args: Array<String>){
    if(args.isEmpty()){
        println("Must provide a file to interpret")
        return
    }
    val file = File(args[0])
    val src = file.readText()
    val lexer = Lexer(src)
    val parser = Parser()
    val tokenstream = lexer.tokenize()
    val moduleAST = parser.parseModule(tokenstream)
    moduleAST.assignParents()
//    println(moduleAST)
    val vm = VM(file.nameWithoutExtension)
    vm.start(moduleAST)
}