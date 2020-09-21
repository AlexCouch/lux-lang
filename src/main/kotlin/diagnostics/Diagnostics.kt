package diagnostics

import arrow.core.Option
import arrow.core.none
import diagnostics.term.Config
import diagnostics.term.Renderer
import java.io.Writer

enum class Severity{
    Note,
    Help,
    Warning,
    Error,
    Bug
    ;
}

enum class LabelStyle{
    Primary, Secondary
}

data class Label(val style: LabelStyle, val id: FileId, val range: IntRange, val message: String = ""){
    companion object{
        fun primary(fileId: FileId, range: IntRange): Label =
            Label(LabelStyle.Primary, fileId, range)

        fun secondary(fileId: FileId, range: IntRange): Label =
            Label(LabelStyle.Secondary, fileId, range)

        fun withMessage(fileId: FileId, range: IntRange, message: String): Label =
            Label(LabelStyle.Primary, fileId, range, message)
    }
}

data class Diagnostic(
    val severity: Severity,
    var code: Option<String> = none(),
    var message: String = "",
    val labels: ArrayList<Label> = arrayListOf(),
    val notes: ArrayList<String> = arrayListOf()
){
    fun render(files: Files, renderer: Renderer): Option<RenderError>{
        return none()
    }

    companion object{
        fun bug(): Diagnostic = Diagnostic(Severity.Bug)
        fun error(): Diagnostic = Diagnostic(Severity.Error)
        fun warning(): Diagnostic = Diagnostic(Severity.Warning)
        fun note(): Diagnostic = Diagnostic(Severity.Note)
        fun help(): Diagnostic = Diagnostic(Severity.Help)
    }
}