import arrow.core.None
import arrow.core.Some
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import java.io.File

@ExperimentalStdlibApi
fun main(args: Array<String>){
    val parser = ArgParser("lux-vm")
    val inputFile by parser.option(ArgType.String, shortName="f", description="The file to be executed by the virtual machine. If the extension is lasm, then it'll be assembled into the output file -o then ran in the virtual machine. If the extension is lexe, then it will be executed immediately and the output arg will be ignored.").required()
    val outputFile by parser.option(ArgType.String, shortName="o", description = "The file that the executable will be called when written to disk for reuse and distribution.")
    parser.parse(args)

    val file = File(inputFile)
    if(!file.exists()){
        println("$inputFile doesn't exist!")
        return
    }
    val exec = when(file.extension){
        "lasm" -> {
            val asm = ASMLoader(file)
            val exec = when(val result = asm.executable){
                is Some -> result.t
                is None -> return
            }
            println(exec)
            if(outputFile == null){
                println("-o argument is required with *.lasm input file")
            }
            val out = File(outputFile)
            if(!out.exists()){
                if(!out.createNewFile()){
                    println("Could not create file \"out.lexe\"")
                }
            }else{
                exec.writeToFile(out)
            }
            exec
        }
        "lexe" -> {
            Executable(file.readBytes())
        }
        else -> {
            println("Unrecognized extension ${file.extension}: Please use either lasm or lexe")
            return
        }
    }
    val vm = VM(exec)
    vm.run()
}