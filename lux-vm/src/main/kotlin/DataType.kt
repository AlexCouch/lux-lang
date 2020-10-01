import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

sealed class DataType{
    data class Byte(val data: kotlin.Byte): DataType(){
        override fun and(other: DataType): Byte =
            when(other){
                is Byte -> Byte(other.data and data)
                is Word -> Byte(other.data2.data and data)
                is DoubleWord -> Byte(other.data2.data2.data and data)
                is QuadWord -> Byte(other.data2.data2.data2.data and data)
            }
        override fun or(other: DataType): Byte =
            when(other){
                is Byte -> Byte(other.data or data)
                is Word -> Byte(other.data2.data or data)
                is DoubleWord -> Byte(other.data2.data2.data or data)
                is QuadWord -> Byte(other.data2.data2.data2.data or data)
            }
        override fun xor(other: DataType): Byte =
            when(other){
                is Byte -> Byte(other.data xor data)
                is Word -> Byte(other.data2.data xor data)
                is DoubleWord -> Byte(other.data2.data2.data xor data)
                is QuadWord -> Byte(other.data2.data2.data2.data xor data)
            }

        override fun shl(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() shl data.toInt()).toByte())
                is Word -> Byte((other.data2.data.toInt() shl data.toInt()).toByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() shl data.toInt()).toByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() shl data.toInt()).toByte())
            }

        override fun shr(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() shr data.toInt()).toByte())
                is Word -> Byte((other.data2.data.toInt() shr data.toInt()).toByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() shr data.toInt()).toByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() shr data.toInt()).toByte())
            }

        override fun plus(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() + data.toInt()).toByte())
                is Word -> Byte((other.data2.data.toInt() + data.toInt()).toByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() + data.toInt()).toByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() + data.toInt()).toByte())
            }

        override fun minus(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() - data.toInt()).toByte())
                is Word -> Byte((other.data2.data.toInt() - data.toInt()).toByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() - data.toInt()).toByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() - data.toInt()).toByte())
            }

        override fun times(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() * data.toInt()).toByte())
                is Word -> Byte((other.data2.data.toInt() * data.toInt()).toByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() * data.toInt()).toByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() * data.toInt()).toByte())
            }

        override fun div(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() / data.toInt()).toByte())
                is Word -> Byte((other.data2.data.toInt() / data.toInt()).toByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() / data.toInt()).toByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() / data.toInt()).toByte())
            }

        override fun toByte(): Byte = this
        override fun toWord(): Word = Word(Byte(0), this)
        override fun toDouble(): DoubleWord = DoubleWord(Word(Byte(0), Byte(0)), Word(Byte(0), this))
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0), Byte(0)), Word(Byte(0), Byte(0))), DoubleWord(Word(Byte(0), Byte(0)), Word(Byte(0), this)))
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> this.data - other.data
                is Word -> (this.data - other.data2.data) + (this.data - other.data1.data)
                is DoubleWord ->
                    (this.data - other.data2.data2.data) +
                            (this.data - other.data2.data1.data) +
                            (this.data - other.data1.data1.data) +
                            (this.data - other.data2.data2.data)
                is QuadWord ->
                    (this.data - other.data2.data2.data2.data) +
                            (this.data - other.data2.data2.data1.data) +
                            (this.data - other.data2.data1.data2.data) +
                            (this.data - other.data2.data1.data1.data) +
                            (this.data - other.data1.data1.data1.data) +
                            (this.data - other.data1.data2.data1.data) +
                            (this.data - other.data1.data1.data2.data) +
                            (this.data - other.data1.data2.data2.data)
            }
        }
    }
    data class Word(val data1: Byte, val data2: Byte): DataType(){
        override fun and(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, other and data2)
                is Word -> Word(data1 and other.data1, data2 and other.data2)
                is DoubleWord -> Word(other.data2.data1 and data1, other.data2.data2 and data2)
                is QuadWord -> Word(other.data2.data2.data1 and data1, other.data2.data2.data2 and data2)
            }
        override fun or(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, other or data2)
                is Word -> Word(data1 or other.data1, data2 or other.data2)
                is DoubleWord -> Word(other.data2.data1 or data1, other.data2.data2 or data2)
                is QuadWord -> Word(other.data2.data2.data1 or data1, other.data2.data2.data2 or data2)
            }
        override fun xor(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, other xor data2)
                is Word -> Word(data1 xor other.data1, data2 xor other.data2)
                is DoubleWord -> Word(other.data2.data1 xor data1, other.data2.data2 xor data2)
                is QuadWord -> Word(other.data2.data2.data1 xor data1, other.data2.data2.data2 xor data2)
            }

        override fun shl(other: DataType): Word =
            when(other){
                is Byte -> Word(other shl data1, other shl data2)
                is Word -> Word((other.data1 shl data1) or (other.data2 shl data1), (other.data1 shl data2) or (other.data2 shl data2))
                is DoubleWord -> Word(
                    (other.data2.data1 shl data1) or (other.data2.data2 shl data1),
                    (other.data2.data1 shl data2) or (other.data2.data2 shl data2)
                )
                is QuadWord -> Word(
                    (other.data2.data2.data1 shl data1) or (other.data2.data2.data2 shl data1),
                    (other.data2.data2.data2 shl data2) or (other.data2.data2.data2 shl data2)
                )
            }

        override fun shr(other: DataType): Word =
            when(other){
                is Byte -> Word(other shr data1, other shr data2)
                is Word -> Word((other.data1 shr data1) or (other.data2 shr data1), (other.data1 shr data2) or (other.data2 shr data2))
                is DoubleWord -> Word(
                    (other.data2.data1 shr data1) or (other.data2.data2 shr data1),
                    (other.data2.data1 shr data2) or (other.data2.data2 shr data2)
                )
                is QuadWord -> Word(
                    (other.data2.data2.data1 shr data1) or (other.data2.data2.data2 shr data1),
                    (other.data2.data2.data2 shr data2) or (other.data2.data2.data2 shr data2)
                )
            }

        override fun plus(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, (other + data2))
                is Word -> Word((other.data1 + data1), other.data2 + data2)
                is DoubleWord -> Word((other.data2.data1 + data1), (other.data2.data2 + data2))
                is QuadWord -> Word((other.data2.data2.data1 + data1), (other.data2.data2.data2 + data2))
            }

        override fun minus(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, (other - data2))
                is Word -> Word((other.data1 - data1), other.data2 - data2)
                is DoubleWord -> Word((other.data2.data1 - data1), (other.data2.data2 - data2))
                is QuadWord -> Word((other.data2.data2.data1 - data1), (other.data2.data2.data2 - data2))
            }

        override fun times(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, (other * data2))
                is Word -> Word((other.data1 * data1), other.data2 * data2)
                is DoubleWord -> Word((other.data2.data1 * data1), (other.data2.data2 * data2))
                is QuadWord -> Word((other.data2.data2.data1 * data1), (other.data2.data2.data2 * data2))
            }

        override fun div(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, (other + data2))
                is Word -> Word((other.data1 / data1), other.data2 / data2)
                is DoubleWord -> Word((other.data2.data1 / data1), (other.data2.data2 / data2))
                is QuadWord -> Word((other.data2.data2.data1 / data1), (other.data2.data2.data2 / data2))
            }

        override fun toByte(): Byte = data2
        override fun toWord(): Word = this
        override fun toDouble(): DoubleWord = DoubleWord(Word(Byte(0), Byte(0)), this)
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0), Byte(0)), Word(Byte(0), Byte(0))), DoubleWord(Word(Byte(0), Byte(0)), this))
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> this.data2.data - other.data
                is Word -> (this.data2.data - other.data2.data) + (this.data1.data - other.data1.data)
                is DoubleWord ->
                    (this.data2.data - other.data2.data2.data) +
                            (this.data2.data - other.data2.data1.data) +
                            (this.data2.data - other.data1.data1.data) +
                            (this.data2.data - other.data2.data2.data)
                is QuadWord ->
                    (this.data2.data - other.data2.data2.data2.data) +
                            (this.data2.data - other.data2.data2.data1.data) +
                            (this.data2.data - other.data2.data1.data2.data) +
                            (this.data2.data - other.data2.data1.data1.data) +
                            (this.data1.data - other.data1.data1.data1.data) +
                            (this.data1.data - other.data1.data2.data1.data) +
                            (this.data1.data - other.data1.data1.data2.data) +
                            (this.data1.data - other.data1.data2.data2.data)
            }
        }
    }
    data class DoubleWord(val data1: Word, val data2: Word): DataType(){
        override fun and(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, (Word(Byte(0xff.toByte()), Byte(0xff.toByte())) or other) and data2.data2)
                is Word -> DoubleWord(data1, data2 and other)
                is DoubleWord -> DoubleWord(other.data1 and data1, other.data2 and data2)
                is QuadWord -> DoubleWord(other.data2.data1 and data1, other.data2.data2 and data2)
            }
        override fun or(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 or other)
                is Word -> DoubleWord(data1, data2 or other.data2)
                is DoubleWord -> DoubleWord(other.data1 or data1, other.data2 or data2)
                is QuadWord -> DoubleWord(other.data2.data1 or data1, other.data2.data2 or data2)
            }
        override fun xor(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 xor other)
                is Word -> DoubleWord(data1, data2 xor other)
                is DoubleWord -> DoubleWord(other.data1 xor data1, other.data2 xor data2)
                is QuadWord -> DoubleWord(other.data2.data1 xor data1, other.data2.data2 xor data2)
            }

        override fun shl(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 shl other)
                is Word -> DoubleWord(data1, other shl data2)
                is DoubleWord -> DoubleWord(
                    other.data1 shl data1,
                    other.data2 shl data2
                )
                is QuadWord -> DoubleWord(
                    other.data2.data1 shl data1,
                    other.data2.data2 shl data2
                )
            }

        override fun shr(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 shr other)
                is Word -> DoubleWord(data1, other shr data2)
                is DoubleWord -> DoubleWord(
                    other.data1 shr data1,
                    other.data2 shr data2
                )
                is QuadWord -> DoubleWord(
                    other.data2.data1 shr data1,
                    other.data2.data2 shr data2
                )
            }

        override fun plus(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 + other)
                is Word -> DoubleWord(data1, other + data2)
                is DoubleWord -> DoubleWord((other.data1 + data1), (other.data2 + data2))
                is QuadWord -> DoubleWord((other.data2.data1 + data1), (other.data2.data2 + data2))
            }

        override fun minus(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 - other)
                is Word -> DoubleWord(data1, other - data2)
                is DoubleWord -> DoubleWord((other.data1 - data1), (other.data2 - data2))
                is QuadWord -> DoubleWord((other.data2.data1 - data1), (other.data2.data2 - data2))
            }

        override fun times(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 * other)
                is Word -> DoubleWord(data1, other * data2)
                is DoubleWord -> DoubleWord((other.data1 * data1), (other.data2 * data2))
                is QuadWord -> DoubleWord((other.data2.data1 * data1), (other.data2.data2 * data2))
            }

        override fun div(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 / other)
                is Word -> DoubleWord(data1, other / data2)
                is DoubleWord -> DoubleWord((other.data1 / data1), (other.data2 / data2))
                is QuadWord -> DoubleWord((other.data2.data1 / data1), (other.data2.data2 / data2))
            }

        override fun toByte(): Byte = data2.data2
        override fun toWord(): Word = data2
        override fun toDouble(): DoubleWord = this
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0), Byte(0)), Word(Byte(0), Byte(0))), this)
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> this.data2.data2.data - other.data
                is Word -> (this.data2.data2.data - other.data2.data) + (this.data2.data2.data - other.data1.data)
                is DoubleWord ->
                    (this.data2.data2.data - other.data2.data2.data) +
                            (this.data2.data2.data - other.data2.data1.data) +
                            (this.data2.data1.data - other.data1.data1.data) +
                            (this.data2.data1.data - other.data2.data2.data)
                is QuadWord ->
                    (this.data2.data2.data - other.data2.data2.data2.data) +
                            (this.data2.data2.data - other.data2.data2.data1.data) +
                            (this.data2.data1.data - other.data2.data1.data2.data) +
                            (this.data2.data1.data - other.data2.data1.data1.data) +
                            (this.data1.data1.data - other.data1.data1.data1.data) +
                            (this.data1.data2.data - other.data1.data2.data1.data) +
                            (this.data1.data1.data - other.data1.data1.data2.data) +
                            (this.data1.data2.data - other.data1.data2.data2.data)
            }
        }

    }
    data class QuadWord(val data1: DoubleWord, val data2: DoubleWord): DataType(){
        override fun and(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                        DoubleWord(
                            Word(
                                Byte(0xff.toByte()),
                                Byte(0xff.toByte())
                            ),
                            Word(
                                    Byte(0xff.toByte()),
                                    Byte(0xff.toByte())
                                ) or other
                        ) and data2.data2)
                is Word -> QuadWord(data1, data2 and other)
                is DoubleWord -> QuadWord(data1, other and data2)
                is QuadWord -> QuadWord(other.data1 and data1, other.data2 and data2)
            }
        override fun or(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ),
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ) or other
                    ) or data2.data2)
                is Word -> QuadWord(data1, data2 or other)
                is DoubleWord -> QuadWord(data1, other or data2)
                is QuadWord -> QuadWord(other.data1 or data1, other.data2 or data2)
            }
        override fun xor(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ),
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ) or other
                    ) xor data2.data2)
                is Word -> QuadWord(data1, data2 xor other)
                is DoubleWord -> QuadWord(data1, other xor data2)
                is QuadWord -> QuadWord(other.data1 xor data1, other.data2 xor data2)
            }

        override fun shl(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ),
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ) or other
                    ) shl data2.data2)
                is Word -> QuadWord(data1, data2 shl other)
                is DoubleWord -> QuadWord(data1, other shl data2)
                is QuadWord -> QuadWord(other.data1 shl data1, other.data2 shl data2)
            }

        override fun shr(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ),
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ) or other
                    ) shr data2.data2)
                is Word -> QuadWord(data1, data2 shr other)
                is DoubleWord -> QuadWord(data1, other shr data2)
                is QuadWord -> QuadWord(other.data1 shr data1, other.data2 shr data2)
            }

        override fun plus(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ),
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ) or other
                    ) + data2.data2)
                is Word -> QuadWord(data1, data2 + other)
                is DoubleWord -> QuadWord(data1, other + data2)
                is QuadWord -> QuadWord(other.data1 + data1, other.data2 + data2)
            }

        override fun minus(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ),
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ) or other
                    ) - data2.data2)
                is Word -> QuadWord(data1, data2 - other)
                is DoubleWord -> QuadWord(data1, other - data2)
                is QuadWord -> QuadWord(other.data1 - data1, other.data2 - data2)
            }

        override fun times(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ),
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ) or other
                    ) * data2.data2)
                is Word -> QuadWord(data1, data2 * other)
                is DoubleWord -> QuadWord(data1, other * data2)
                is QuadWord -> QuadWord(other.data1 * data1, other.data2 * data2)
            }

        override fun div(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ),
                        Word(
                            Byte(0xff.toByte()),
                            Byte(0xff.toByte())
                        ) or other
                    ) / data2.data2)
                is Word -> QuadWord(data1, data2 / other)
                is DoubleWord -> QuadWord(data1, other / data2)
                is QuadWord -> QuadWord(other.data1 / data1, other.data2 / data2)
            }

        override fun toByte(): Byte = data2.data2.data2
        override fun toDouble(): DoubleWord = data2
        override fun toWord(): Word = data2.data2
        override fun toQuad(): QuadWord = this
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> this.data2.data2.data2.data - other.data
                is Word -> (this.data2.data2.data2.data - other.data2.data) + (this.data2.data2.data1.data - other.data1.data)
                is DoubleWord ->
                    (this.data2.data2.data2.data - other.data2.data2.data) +
                    (this.data2.data2.data1.data - other.data2.data1.data) +
                    (this.data2.data1.data1.data - other.data1.data1.data) +
                    (this.data2.data1.data2.data - other.data2.data2.data)
                is QuadWord ->
                    (this.data2.data2.data2.data - other.data2.data2.data2.data) +
                            (this.data2.data2.data1.data - other.data2.data2.data1.data) +
                            (this.data2.data1.data2.data - other.data2.data1.data2.data) +
                            (this.data2.data1.data1.data - other.data2.data1.data1.data) +
                            (this.data1.data1.data1.data - other.data1.data1.data1.data) +
                            (this.data1.data2.data1.data - other.data1.data2.data1.data) +
                            (this.data1.data1.data2.data - other.data1.data1.data2.data) +
                            (this.data1.data2.data2.data - other.data1.data2.data2.data)
            }
        }

    }

    abstract infix fun shl(other: DataType): DataType
    abstract infix fun shr(other: DataType): DataType
    abstract infix fun and(other: DataType): DataType
    abstract infix fun or(other: DataType): DataType
    abstract infix fun xor(other: DataType): DataType
    abstract operator fun plus(other: DataType): DataType
    abstract operator fun minus(other: DataType): DataType
    abstract operator fun times(other: DataType): DataType
    abstract operator fun div(other: DataType): DataType
    abstract operator fun compareTo(other: DataType): Int

    abstract fun toByte(): Byte
    abstract fun toWord(): Word
    abstract fun toDouble(): DoubleWord
    abstract fun toQuad(): QuadWord

}