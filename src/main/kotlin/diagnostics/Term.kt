package diagnostics

import arrow.core.Option
import buildPrettyString
import diagnostics.term.Config
import diagnostics.term.Renderer
import java.io.Writer

sealed class RenderError{
    object FileMissing: RenderError(){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString{
                append("File missing")
            }
    }
    object InvalidIndex: RenderError(){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString{
                append("Invalid index")
            }
    }
    data class IO(val error: Error): RenderError(){
        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString{
                append(error.localizedMessage)
            }
    }

    companion object{
        fun from(error: Error) = IO(error)
    }
}

fun emit(writer: Writer, config: Config, files: Files, diagnostic: Diagnostic): Option<RenderError> {
    val renderer = Renderer(writer, config)
    return diagnostic.render(files, renderer)
}