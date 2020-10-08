import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

sealed class DataType{
    data class Byte(val data: kotlin.UByte): DataType(){
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

        override fun inv(): Byte =
            Byte(data.inv())

        override fun shl(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() shl data.toInt()).toUByte())
                is Word -> Byte((other.data2.data.toInt() shl data.toInt()).toUByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() shl data.toInt()).toUByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() shl data.toInt()).toUByte())
            }

        override fun shr(other: DataType): Byte =
            when(other){
                is Byte -> Byte((data.toInt() shr other.data.toInt()).toUByte())
                is Word -> Byte((data.toInt() shr other.data2.data.toInt()).toUByte())
                is DoubleWord -> Byte((data.toInt() shr other.data2.data2.data.toInt()).toUByte())
                is QuadWord -> Byte((data.toInt() shr other.data2.data2.data2.data.toInt()).toUByte())
            }

        override fun plus(other: DataType): Byte =
            when(other){
                is Byte -> {
                    Byte(((other.data + data) and 0xFF.toUInt()).toUByte())
                }
                is Word -> Byte((other.data2.data.toInt() + data.toInt()).toUByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() + data.toInt()).toUByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() + data.toInt()).toUByte())
            }

        @ExperimentalUnsignedTypes
        override fun minus(other: DataType): Byte =
            when(other){
                is Byte -> {
                    val unconvertedDiff = data - other.data
                    val maskedDiff = unconvertedDiff and 0xFF.toUInt()
                    val diff = maskedDiff.toUByte()
                    Byte(diff)
                }
                is Word -> Byte((other.data2.data.toInt() - data.toInt()).toUByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() - data.toInt()).toUByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() - data.toInt()).toUByte())
            }

        override fun times(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() * data.toInt()).toUByte())
                is Word -> Byte((other.data2.data.toInt() * data.toInt()).toUByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() * data.toInt()).toUByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() * data.toInt()).toUByte())
            }

        override fun div(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() / data.toInt()).toUByte())
                is Word -> Byte((other.data2.data.toInt() / data.toInt()).toUByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() / data.toInt()).toUByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() / data.toInt()).toUByte())
            }

        override fun toByte(): Byte = this
        override fun toWord(): Word = Word(Byte(0.toUByte()), this)
        override fun toDouble(): DoubleWord = DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), this))
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), Byte(0.toUByte()))), DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), this)))
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> (this.data - other.data).toInt()
                is Word -> ((this.data - other.data2.data) + (this.data - other.data1.data)).toInt()
                is DoubleWord ->
                    ((this.data - other.data2.data2.data) +
                            (this.data - other.data2.data1.data) +
                            (this.data - other.data1.data1.data) +
                            (this.data - other.data2.data2.data)).toInt()
                is QuadWord ->
                    ((this.data - other.data2.data2.data2.data) +
                            (this.data - other.data2.data2.data1.data) +
                            (this.data - other.data2.data1.data2.data) +
                            (this.data - other.data2.data1.data1.data) +
                            (this.data - other.data1.data1.data1.data) +
                            (this.data - other.data1.data2.data1.data) +
                            (this.data - other.data1.data1.data2.data) +
                            (this.data - other.data1.data2.data2.data)).toInt()
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

        override fun inv(): Word =
            Word(data1.inv(), data2.inv())

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
        override fun toDouble(): DoubleWord = DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), this)
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), Byte(0.toUByte()))), DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), this))
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> (this.data2.data - other.data).toInt()
                is Word -> ((this.data2.data - other.data2.data) + (this.data1.data - other.data1.data)).toInt()
                is DoubleWord ->
                    ((this.data2.data - other.data2.data2.data) +
                            (this.data2.data - other.data2.data1.data) +
                            (this.data2.data - other.data1.data1.data) +
                            (this.data2.data - other.data2.data2.data)).toInt()
                is QuadWord ->
                    ((this.data2.data - other.data2.data2.data2.data) +
                            (this.data2.data - other.data2.data2.data1.data) +
                            (this.data2.data - other.data2.data1.data2.data) +
                            (this.data2.data - other.data2.data1.data1.data) +
                            (this.data1.data - other.data1.data1.data1.data) +
                            (this.data1.data - other.data1.data2.data1.data) +
                            (this.data1.data - other.data1.data1.data2.data) +
                            (this.data1.data - other.data1.data2.data2.data)).toInt()
            }
        }
    }
    data class DoubleWord(val data1: Word, val data2: Word): DataType(){
        override fun and(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, (Word(Byte(0xff.toUByte()), Byte(0xff.toUByte())) or other) and data2.data2)
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

        override fun inv(): DoubleWord =
            DoubleWord(data1.inv(), data2.inv())

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
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), Byte(0.toUByte()))), this)
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> (this.data2.data2.data - other.data).toInt()
                is Word -> ((this.data2.data2.data - other.data2.data) + (this.data2.data2.data - other.data1.data)).toInt()
                is DoubleWord ->
                    ((this.data2.data2.data - other.data2.data2.data) +
                            (this.data2.data2.data - other.data2.data1.data) +
                            (this.data2.data1.data - other.data1.data1.data) +
                            (this.data2.data1.data - other.data2.data2.data)).toInt()
                is QuadWord ->
                    ((this.data2.data2.data - other.data2.data2.data2.data) +
                            (this.data2.data2.data - other.data2.data2.data1.data) +
                            (this.data2.data1.data - other.data2.data1.data2.data) +
                            (this.data2.data1.data - other.data2.data1.data1.data) +
                            (this.data1.data1.data - other.data1.data1.data1.data) +
                            (this.data1.data2.data - other.data1.data2.data1.data) +
                            (this.data1.data1.data - other.data1.data1.data2.data) +
                            (this.data1.data2.data - other.data1.data2.data2.data)).toInt()
            }
        }

    }
    data class QuadWord(val data1: DoubleWord, val data2: DoubleWord): DataType(){
        override fun and(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                        DoubleWord(
                            Word(
                                Byte(0xff.toUByte()),
                                Byte(0xff.toUByte())
                            ),
                            Word(
                                    Byte(0xff.toUByte()),
                                    Byte(0xff.toUByte())
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
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ),
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
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
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ),
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ) or other
                    ) xor data2.data2)
                is Word -> QuadWord(data1, data2 xor other)
                is DoubleWord -> QuadWord(data1, other xor data2)
                is QuadWord -> QuadWord(other.data1 xor data1, other.data2 xor data2)
            }

        override fun inv(): QuadWord =
            QuadWord(data1.inv(), data2.inv())

        override fun shl(other: DataType): QuadWord =
            when(other){
                is Byte -> QuadWord(data1,
                    DoubleWord(
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ),
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
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
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ),
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
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
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ),
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
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
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ),
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
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
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ),
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
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
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
                        ),
                        Word(
                            Byte(0xff.toUByte()),
                            Byte(0xff.toUByte())
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
                is Byte -> (this.data2.data2.data2.data - other.data).toInt()
                is Word -> ((this.data2.data2.data2.data - other.data2.data) + (this.data2.data2.data1.data - other.data1.data)).toInt()
                is DoubleWord ->
                    ((this.data2.data2.data2.data - other.data2.data2.data) +
                    (this.data2.data2.data1.data - other.data2.data1.data) +
                    (this.data2.data1.data1.data - other.data1.data1.data) +
                    (this.data2.data1.data2.data - other.data2.data2.data)).toInt()
                is QuadWord ->
                    ((this.data2.data2.data2.data - other.data2.data2.data2.data) +
                            (this.data2.data2.data1.data - other.data2.data2.data1.data) +
                            (this.data2.data1.data2.data - other.data2.data1.data2.data) +
                            (this.data2.data1.data1.data - other.data2.data1.data1.data) +
                            (this.data1.data1.data1.data - other.data1.data1.data1.data) +
                            (this.data1.data2.data1.data - other.data1.data2.data1.data) +
                            (this.data1.data1.data2.data - other.data1.data1.data2.data) +
                            (this.data1.data2.data2.data - other.data1.data2.data2.data)).toInt()
            }
        }

    }

    abstract infix fun shl(other: DataType): DataType
    abstract infix fun shr(other: DataType): DataType
    abstract infix fun and(other: DataType): DataType
    abstract infix fun or(other: DataType): DataType
    abstract infix fun xor(other: DataType): DataType
    abstract fun inv(): DataType
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