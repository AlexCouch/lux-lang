package diagnostics

import arrow.core.*
import kotlin.math.min

typealias FileId = String
typealias Name = String
typealias Source = String

interface Files{
    val id: FileId
    val name: Name
    val source: Source

    fun name(id: FileId): Option<Name>
    fun source(id: FileId): Option<Source>
    fun lineIndex(id: FileId, index: Int): Option<Int>
    fun lineNumber(id: FileId, lineIndex: Int): Option<Int> = Some(lineIndex + 1)
    fun colonNumber(id: FileId, lineIndex: Int, byteIndex: Int): Option<Int>{
        val source = when(val src = this.source(id)){
            is Some -> src.t
            is None -> return src
        }
        val linerange = when(val range = this.lineRange(id, lineIndex)){
            is Some -> range.t
            is None -> return range
        }
        val columnIndex = columnIndex(source, linerange, byteIndex)
        return Some(columnIndex + 1)
    }
    fun lineRange(id: FileId, lineIndex: Int): Option<IntRange>
}

data class Location(val line: Int, val column: Int)

fun columnIndex(source: String, lineRange: IntRange, byteIndex: Int): Int =
    min(byteIndex, min(lineRange.last, source.length))

fun lineStarts(source: Source): List<Int> = arrayListOf(0).apply { "\n".toRegex().findAll(source).forEach { add(it.range.first + 1) } }

data class SimpleFile(
    override val name: Name,
    override val source: Source,
    val lineStarts: List<Int> = lineStarts(source)
): Files{
    fun lineStart(lineIndex: Int) =
        when{
            lineIndex < lineStarts.size -> lineStarts[lineIndex].some()
            lineIndex == lineStarts.size -> source.length.some()
            else -> none()
        }

    override val id: FileId = ""

    override fun name(id: FileId): Option<Name> =
        this.name.some()

    override fun source(id: FileId): Option<Source> =
        this.source.some()

    override fun lineIndex(id: FileId, index: Int): Option<Int> =
        this.lineStarts.binarySearch { index }.some()

    override fun lineRange(id: FileId, lineIndex: Int): Option<IntRange> {
        val start = when(val result = lineStart(lineIndex)){
            is Some -> result.t
            is None -> return result
        }
        val nextStart = when(val result = lineStart(lineIndex + 1)){
            is Some -> result.t
            is None -> return result
        }
        return (start..nextStart).some()
    }
}

data class SimpleFiles(val files: ArrayList<SimpleFile>): Files{
    override val id: FileId
        get() = TODO("Not yet implemented")
    override val name: Name
        get() = TODO("Not yet implemented")
    override val source: Source
        get() = TODO("Not yet implemented")

    override fun name(id: FileId): Option<Name> {
        TODO("Not yet implemented")
    }

    override fun source(id: FileId): Option<Source> {
        TODO("Not yet implemented")
    }

    override fun lineIndex(id: FileId, index: Int): Option<Int> {
        TODO("Not yet implemented")
    }

    override fun lineRange(id: FileId, lineIndex: Int): Option<IntRange> {
        TODO("Not yet implemented")
    }

}

fun main(){
    val column = columnIndex("hello world", 2..13, 2 + 0)
    println(column)
}

