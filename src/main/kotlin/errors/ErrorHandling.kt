package errors

import Position
import TokenPos
import buildPrettyString
import java.io.File

data class ErrorLine(val start: TokenPos, val end: TokenPos)
data class Error(
    val message: String,
    val annotations: ArrayList<SourceAnnotation>
){
    override fun toString(): String =
        buildPrettyString {
            red{
                appendWithNewLine(message)
            }
            annotations.forEach {
                append(it.toString())
            }
        }
}

class ErrorLineBuilder{
    var start = TokenPos.default
    var end = TokenPos.default

    fun build() = ErrorLine(start, end)
}

class SourceOriginBuilder{
    var start = TokenPos.default
    var end = TokenPos.default
    var source = ""

    fun build() = SourceOrigin(start, end, source)
}

class SourceAnnotationBuilder{
    private var line: ErrorLine? = null
    private var source: SourceOrigin? = null

    var message = ""

    fun sourceOrigin(block: SourceOriginBuilder.()->Unit){
        val originBuilder = SourceOriginBuilder()
        originBuilder.block()
        source = originBuilder.build()
    }

    fun errorLine(block: ErrorLineBuilder.()->Unit){
        val errorLineBuilder = ErrorLineBuilder()
        errorLineBuilder.block()
        line = errorLineBuilder.build()
    }

    fun build() = SourceAnnotation(message, line!!, source!!)
}

class ErrorBuilder{
    private val annotations = ArrayList<SourceAnnotation>()

    var message = ""

    fun annotate(block: SourceAnnotationBuilder.()->Unit){
        val annotationBuilder = SourceAnnotationBuilder()
        annotationBuilder.block()
        annotations += annotationBuilder.build()
    }

    fun addAnnotation(sourceAnnotation: SourceAnnotation) = annotations.add(sourceAnnotation)

    fun build() = Error(message, annotations)
}

class ErrorHandling{
    private val errors = ArrayList<Error>()

    fun error(block: ErrorBuilder.()->Unit){
        val builder = ErrorBuilder()
        builder.block()
        errors += builder.build()
    }

    override fun toString(): String = buildPrettyString {
        errors.forEach {
            println(it.toString())
        }
    }
}

fun main(){
    val errorHandling = ErrorHandling()
    val src = File("test.txt").readText()
    val errorStart = TokenPos(Position(11, 13), 90, 2)
    val errorEnd = TokenPos(Position(11, 28), 105, 2)
    val causeStart = TokenPos(Position(6, 9), 45, 1)
    val causeEnd = TokenPos(Position(6, 12), 48, 1)
    errorHandling.error {
        message = "An error occurred during type checking"
        annotate {
            message = "struct field was supplied a String"
            errorLine {
                start = errorStart
                end = errorEnd
            }
            sourceOrigin {
                start = TokenPos(Position(11, 9), 86, 2)
                end = errorEnd
                source = src
            }
        }
        annotate {
            message = "but expected an Int"

            errorLine {
                start = causeStart
                end = causeEnd
            }
            sourceOrigin {
                start = TokenPos(Position(6, 5), 41, 1)
                end = causeEnd
                source = src
            }
        }
    }
    println(errorHandling.toString())
}