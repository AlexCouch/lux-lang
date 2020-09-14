import arrow.core.Either
import arrow.core.None
import arrow.core.Some
import errors.ErrorHandling
import parser.ModuleParser
import passes.PreSSATransformation
import passes.symbolResolution.SymbolResolutionPass
import passes.symbolResolution.SymbolTable
import passes.typecheck.TypeCheckingPass
import java.io.File

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
    val parser = ModuleParser(file.nameWithoutExtension, errorHandler)
    val tokenstream = lexer.tokenize()
    val moduleAST = when(val ast = parser.parse(tokenstream)){
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
    println(ir.toPrettyString())
    val typeck = TypeCheckingPass()
    val typeCheckedModule = when(val result = typeck.visitModule(ir, symbolTable)){
        is Either.Left -> result.a
        is Either.Right -> {
            println(errorHandler)
            return
        }
    }
    println(typeCheckedModule.toPrettyString())
//    val ssaTransformer = PreSSATransformation()
//    val ssaSymbolTable = SymbolTable()
//    val ssaIR = ssaTransformer.visitModule(typeCheckedModule, ssaSymbolTable)
//    println(ssaIR.toPrettyString())
//    val graph = CFG()
//    val cfgPass = CFGPass()
//    val cfgModule = cfgPass.visitModule(ssaIR, graph)
//    graph.toGraph()
}