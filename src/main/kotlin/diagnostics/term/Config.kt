package diagnostics.term

import PrettyColors
import diagnostics.LabelStyle
import diagnostics.Severity

class Config(
    val displayStyle: DisplayStyle = DisplayStyle.Rich,
    val tabWidth: Int = 4,
    val styles: Styles = Styles(),
    val chars: Chars = Chars(),
    val startContextLines: Int = 3,
    val endContextLines: Int = 1
)

enum class DisplayStyle{
    Rich,
    Medium,
    Short
}

data class Styles(
    val headerBug: PrettyColors = PrettyColors.RED,
    val headerError: PrettyColors = PrettyColors.RED,
    val headerWarning: PrettyColors = PrettyColors.YELLOW,
    val headerNote: PrettyColors = PrettyColors.GREEN,
    val headerHelp: PrettyColors = PrettyColors.BLUE,
    val headerMessage: PrettyColors = PrettyColors.BOLD,

    val primaryLabelBug: PrettyColors = PrettyColors.RED,
    val primaryLabelError: PrettyColors = PrettyColors.RED,
    val primaryLabelWarning: PrettyColors = PrettyColors.YELLOW,
    val primaryLabelNote: PrettyColors = PrettyColors.GREEN,
    val primaryLabelHelp: PrettyColors = PrettyColors.BLUE,
    val secondaryLabel: PrettyColors = PrettyColors.BLUE,

    val lineNumber: PrettyColors = PrettyColors.BLUE,
    val sourceBorder: PrettyColors = PrettyColors.BLUE,
    val noteBullet: PrettyColors = PrettyColors.BLUE
){
    fun header(severity: Severity): PrettyColors =
        when(severity){
            Severity.Bug -> headerBug
            Severity.Error -> headerError
            Severity.Warning -> headerWarning
            Severity.Note -> headerNote
            Severity.Help -> headerHelp
        }

    fun label(severity: Severity, style: LabelStyle) =
        when{
            severity == Severity.Bug && style == LabelStyle.Primary -> primaryLabelBug
            severity == Severity.Error && style == LabelStyle.Primary -> primaryLabelError
            severity == Severity.Warning && style == LabelStyle.Primary -> primaryLabelWarning
            severity == Severity.Note && style == LabelStyle.Primary -> primaryLabelNote
            severity == Severity.Help && style == LabelStyle.Primary -> primaryLabelHelp
            style == LabelStyle.Secondary -> secondaryLabel
            else -> PrettyColors.WHITE
        }
}

data class Chars(
    val sourceBorderTopLeft: Char = '┌',
    val sourceBorderTop: Char = '─',
    val sourceBorderLeft: Char = '|',
    val sourceBorderLeftBreak: Char = '·',
    val noteBullet: Char = '=',
    val singlePrimaryCaret: Char = '^',
    val singleSecondaryCaret: Char = '-',
    val multiPrimaryCaretStart: Char = '^',
    val multiPrimaryCaretEnd: Char = '^',
    val multiSecondarCaretStart: Char = '\'',
    val multiSecondarCaretEnd: Char = '\'',
    val multiTopLeft: Char = '╭',
    val multiTop: Char = '-',
    val multiBottomLeft: Char = '╰',
    val multiBottom: Char = '-',
    val multiLeft: Char = '|',
    val pointerLeft: Char = '|'
)