import arrow.core.None
import arrow.core.Some
import errors.ErrorHandling
import passes.PreSSATransformation
import passes.cfg.CFG
import passes.cfg.CFGPass
import passes.symbolResolution.SymbolResolutionPass
import passes.symbolResolution.SymbolTable
import passes.typecheck.TypeCheckingPass
import java.io.File

sealed class Either<out T>{
    data class Some<T>(val t: T): Either<T>()

    val isNone get() = this is None
    val isSome get() = this is Some

    object None : Either<Nothing>()
    fun unwrap() =
        if(this is Some<T>){
            this.t
        }else{
            throw IllegalStateException("Attempted to unwrap Either but was None")
        }

    infix fun orelse(other: Any?) = if(this.isNone) other else this
}

@ExperimentalStdlibApi
fun main(args: Array<String>){
    if(args.isEmpty()){
        println("Must provide a file to interpret")
        return
    }
    val file = File(args[0])
    val src = file.readText()
    val errorHandler = ErrorHandling()
    val lexer = Lexer(src)
    val parser = Parser(file.nameWithoutExtension, errorHandler)
    val tokenstream = lexer.tokenize()
    val moduleAST = when(val ast = parser.parseModule(tokenstream)){
        is Some -> ast.t
        is None -> {
            println(errorHandler)
            return
        }
    }
    moduleAST.assignParents()
    println(moduleAST)
//    val vm = VM(file.nameWithoutExtension)
//    vm.start(moduleAST)
    val symbolTable = SymbolTable()
    val astLowering = SymbolResolutionPass()
    val ir = astLowering.visitModule(moduleAST, symbolTable)
    val typeck = TypeCheckingPass()
    val typeCheckedModule = typeck.visitModule(ir, symbolTable)
    val ssaTransformer = PreSSATransformation()
    val ssaSymbolTable = SymbolTable()
    val ssaIR = ssaTransformer.visitModule(typeCheckedModule, ssaSymbolTable)
    println(ssaIR)
    val graph = CFG()
    val cfgPass = CFGPass()
    val cfgModule = cfgPass.visitModule(ssaIR, graph)
//    graph.toGraph()
}