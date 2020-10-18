import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

@ExperimentalUnsignedTypes
sealed class DataType{
    @ExperimentalUnsignedTypes
    data class Byte(val data: UByte): DataType(){
        override fun and(other: DataType): Byte =
            when(other){
                is Byte -> Byte(other.data and data)
                is SignedByte -> Byte(other.data.toUByte() and data)
                is Word -> Byte(other.data2.data and data)
                is DoubleWord -> Byte(other.data2.data2.data and data)
                is QuadWord -> Byte(other.data2.data2.data2.data and data)
            }
        override fun or(other: DataType): Byte =
            when(other){
                is Byte -> Byte(other.data or data)
                is SignedByte -> Byte(other.data.toUByte() or data)
                is Word -> Byte(other.data2.data or data)
                is DoubleWord -> Byte(other.data2.data2.data or data)
                is QuadWord -> Byte(other.data2.data2.data2.data or data)
            }
        override fun xor(other: DataType): Byte =
            when(other){
                is Byte -> Byte(other.data xor data)
                is SignedByte -> Byte(other.data.toUByte() xor data)
                is Word -> Byte(other.data2.data xor data)
                is DoubleWord -> Byte(other.data2.data2.data xor data)
                is QuadWord -> Byte(other.data2.data2.data2.data xor data)
            }

        override fun inv(): Byte =
            Byte(data.inv())

        override fun shl(other: DataType): Byte =
            when(other){
                is Byte -> Byte((other.data.toInt() shl data.toInt()).toUByte())
                is SignedByte -> Byte((other.data.toInt() shl data.toInt()).toUByte())
                is Word -> Byte((other.data2.data.toInt() shl data.toInt()).toUByte())
                is DoubleWord -> Byte((other.data2.data2.data.toInt() shl data.toInt()).toUByte())
                is QuadWord -> Byte((other.data2.data2.data2.data.toInt() shl data.toInt()).toUByte())
            }

        override fun shr(other: DataType): Byte =
            when(other){
                is Byte -> Byte((data.toInt() shr other.data.toInt()).toUByte())
                is SignedByte -> Byte((data.toInt() shr other.data.toInt()).toUByte())
                is Word -> Byte((data.toInt() shr other.data2.data.toInt()).toUByte())
                is DoubleWord -> Byte((data.toInt() shr other.data2.data2.data.toInt()).toUByte())
                is QuadWord -> Byte((data.toInt() shr other.data2.data2.data2.data.toInt()).toUByte())
            }

        internal fun toUByte() =
            this.data

        override fun plus(other: DataType): Either<Byte, String> =
            when(other){
                is Byte -> {
                    val ubyte = other.toUByte()
                    if(data + ubyte > 0xFFu){
                        "Cannot add $other as sum is greater than max byte size".right()
                    }else{
                        Byte((data + ubyte).toUByte()).left()
                    }
                }
                is SignedByte -> {
                    val ubyte = other.data.toUByte()
                    if(data + ubyte > 0xFFu){
                        "Cannot add $other as sum is greater than max byte size".right()
                    }else{
                        Byte((data + ubyte).toUByte()).left()
                    }
                }
                is Word -> {
                    "Cannot add word to byte as word is too large to add to a byte. Try adding byte to word instead.".right()
                }
                is DoubleWord -> {
                    "Cannot add dword to byte as word is too large to add to a byte. Try adding byte to word instead.".right()
                }
                is QuadWord -> "Cannot add qword to byte as word is too large to add to a byte. Try adding byte to word instead.".right()
            }

        override fun minus(other: DataType): Either<DataType, String> =
            when(other){
                is Byte -> {
                    val unconvertedDiff = data - other.data
                    val maskedDiff = unconvertedDiff and 0xFF.toUInt()
                    val diff = maskedDiff.toUByte()
                    Byte(diff).left()
                }
                is SignedByte -> {
                    val maskedDiff = data.toByte() - other.data
                    val diff = (maskedDiff and 0xFF).toByte()
                    SignedByte(diff).left()
                }
                is Word -> {
                    "Cannot subtract word to byte as word is too large to add to a byte. Try subtracting byte from word instead.".right()
                }
                is DoubleWord -> {
                    "Cannot subtract dword to byte as dword is too large to add to a byte. Try subtracting byte from dword instead.".right()
                }
                is QuadWord -> "Cannot subtract qword to byte as qword is too large to add to a byte. Try subtract byte from qword instead.".right()
            }

        override fun times(other: DataType): Either<Byte, String> =
            when(other){
                is Byte -> {
                    val product = other.data * data
                    if(product > 0xFFu){
                        "Cannot multiply $this by $other because it's product is greater than 0xFF".right()
                    }else{
                        Byte(product.toUByte()).left()
                    }
                }
                is SignedByte -> {
                    val product = other.data.toUByte() * data
                    if(product > 0xFFu){
                        "Cannot multiply $this by $other because it's product is greater than 0xFF".right()
                    }else{
                        Byte(product.toUByte()).left()
                    }
                }
                is Word -> {
                    "Cannot multiply byte by word as word is too large to multiply a byte.".right()
                }
                is DoubleWord -> {
                    "Cannot multiply byte by dword as dword is too large to add to a byte.".right()
                }
                is QuadWord -> "Cannot multiply byte by qword as qword is too large to add to a byte.".right()
            }

        override fun div(other: DataType): Either<DataType, String> =
            when(other){
                is Byte -> {
                    val quotient = (data / other.data )
                    Byte(quotient.toUByte()).left()
                }
                is SignedByte -> {
                    val quotient = (data.toByte() / other.data)
                    SignedByte(quotient.toByte()).left()
                }
                is Word -> {
                    "Cannot divide byte by word as word is too large to divide a byte.".right()
                }
                is DoubleWord -> {
                    "Cannot divide byte by dword as dword is too large to divide a byte.".right()
                }
                is QuadWord -> "Cannot divide byte by qword as qword is too large to divide a byte.".right()
            }

        override fun toByte(): Byte = this
        override fun toWord(): Word = Word(Byte(0.toUByte()), this)
        override fun toDouble(): DoubleWord = DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), this))
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), Byte(0.toUByte()))), DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), this)))
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> (this.data - other.data).toInt()
                is SignedByte -> (this.data.toByte() - other.data)
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

        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append("0x")
                append(data.toString(16))
            }
    }
    @ExperimentalUnsignedTypes
    data class SignedByte(val data: kotlin.Byte): DataType(){
        override fun and(other: DataType): SignedByte =
            when(other){
                is Byte -> SignedByte(other.data.toByte() and data)
                is SignedByte -> SignedByte(other.data and data)
                is Word -> SignedByte(other.data2.data.toByte() and data)
                is DoubleWord -> SignedByte(other.data2.data2.data.toByte() and data)
                is QuadWord -> SignedByte(other.data2.data2.data2.data.toByte() and data)
            }
        override fun or(other: DataType): SignedByte =
            when(other){
                is Byte -> SignedByte(other.data.toByte() or data)
                is SignedByte -> SignedByte(other.data or data)
                is Word -> SignedByte(other.data2.data.toByte() or data)
                is DoubleWord -> SignedByte(other.data2.data2.data.toByte() or data)
                is QuadWord -> SignedByte(other.data2.data2.data2.data.toByte() or data)
            }
        override fun xor(other: DataType): SignedByte =
            when(other){
                is Byte -> SignedByte(other.data.toByte() xor data)
                is SignedByte -> SignedByte(other.data xor data)
                is Word -> SignedByte(other.data2.data.toByte() xor data)
                is DoubleWord -> SignedByte(other.data2.data2.data.toByte() xor data)
                is QuadWord -> SignedByte(other.data2.data2.data2.data.toByte() xor data)
            }

        override fun inv(): SignedByte =
            SignedByte(data.inv())

        override fun shl(other: DataType): SignedByte =
            when(other){
                is Byte -> SignedByte((other.data.toInt() shl data.toInt()).toByte())
                is SignedByte -> SignedByte(other.data xor data)
                is Word -> SignedByte((other.data2.data.toInt() shl data.toInt()).toByte())
                is DoubleWord -> SignedByte((other.data2.data2.data.toInt() shl data.toInt()).toByte())
                is QuadWord -> SignedByte((other.data2.data2.data2.data.toInt() shl data.toInt()).toByte())
            }

        override fun shr(other: DataType): SignedByte =
            when(other){
                is Byte -> SignedByte((data.toInt() shr other.data.toInt()).toByte())
                is SignedByte -> SignedByte((data.toInt() shr other.data.toInt()).toByte())
                is Word -> SignedByte((data.toInt() shr other.data2.data.toInt()).toByte())
                is DoubleWord -> SignedByte((data.toInt() shr other.data2.data2.data.toInt()).toByte())
                is QuadWord -> SignedByte((data.toInt() shr other.data2.data2.data2.data.toInt()).toByte())
            }

        private fun toUByte() =
            this.data

        override fun plus(other: DataType): Either<SignedByte, String> =
            when(other){
                is Byte -> {
                    val ubyte = other.data.toByte()
                    if(data + ubyte > 0xFF){
                        "Cannot add $other as sum is greater than max byte size".right()
                    }else{
                        SignedByte((data + ubyte).toByte()).left()
                    }
                }
                is SignedByte -> {
                    val ubyte = other.data
                    if(data + ubyte > 0xFF){
                        "Cannot add $other as sum is greater than max byte size".right()
                    }else{
                        SignedByte((data + ubyte).toByte()).left()
                    }
                }
                is Word -> {
                    "Cannot add word to byte as word is too large to add to a byte. Try adding byte to word instead.".right()
                }
                is DoubleWord -> {
                    "Cannot add dword to byte as word is too large to add to a byte. Try adding byte to word instead.".right()
                }
                is QuadWord -> "Cannot add qword to byte as word is too large to add to a byte. Try adding byte to word instead.".right()
            }

        override fun minus(other: DataType): Either<SignedByte, String> =
            when(other){
                is Byte -> {
                    val unconvertedDiff = data - other.data.toByte()
                    val maskedDiff = unconvertedDiff and 0xFF
                    val diff = maskedDiff.toByte()
                    SignedByte(diff).left()
                }
                is SignedByte -> {
                    val unconvertedDiff = data - other.data
                    val maskedDiff = unconvertedDiff and 0xFF
                    val diff = maskedDiff.toByte()
                    SignedByte(diff).left()
                }
                is Word -> {
                    "Cannot subtract word to byte as word is too large to add to a byte. Try subtracting byte from word instead.".right()
                }
                is DoubleWord -> {
                    "Cannot subtract dword to byte as dword is too large to add to a byte. Try subtracting byte from dword instead.".right()
                }
                is QuadWord -> "Cannot subtract qword to byte as qword is too large to add to a byte. Try subtract byte from qword instead.".right()
            }

        override fun times(other: DataType): Either<SignedByte, String> =
            when(other){
                is Byte -> {
                    val product = other.data.toByte() * data
                    if(product > 0xFF){
                        "Cannot multiply $this by $other because it's product is greater than 0xFF".right()
                    }else{
                        SignedByte(product.toByte()).left()
                    }
                }
                is SignedByte -> {
                    val product = other.data * data
                    if(product > 0xFF){
                        "Cannot multiply $this by $other because it's product is greater than 0xFF".right()
                    }else{
                        SignedByte(product.toByte()).left()
                    }
                }
                is Word -> {
                    "Cannot multiply byte by word as word is too large to multiply a byte.".right()
                }
                is DoubleWord -> {
                    "Cannot multiply byte by dword as dword is too large to add to a byte.".right()
                }
                is QuadWord -> "Cannot multiply byte by qword as qword is too large to add to a byte.".right()
            }

        override fun div(other: DataType): Either<SignedByte, String> =
            when(other){
                is Byte -> {
                    val quotient = (data / other.data.toByte())
                    SignedByte(quotient.toByte()).left()
                }
                is SignedByte -> {
                    val quotient = (data / other.data)
                    SignedByte(quotient.toByte()).left()
                }
                is Word -> {
                    "Cannot divide byte by word as word is too large to divide a byte.".right()
                }
                is DoubleWord -> {
                    "Cannot divide byte by dword as dword is too large to divide a byte.".right()
                }
                is QuadWord -> "Cannot divide byte by qword as qword is too large to divide a byte.".right()
            }

        override fun toByte(): Byte = Byte(data.toUByte())
        override fun toWord(): Word = Word(Byte(0.toUByte()), toByte())
        override fun toDouble(): DoubleWord = DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), toByte()))
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), Byte(0.toUByte()))), DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), toByte())))
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> (this.data - other.data.toByte())
                is SignedByte -> (this.data - other.data)
                is Word -> ((this.data - other.data2.data.toByte()) + (this.data - other.data1.data.toByte()))
                is DoubleWord ->
                    ((this.data - other.data2.data2.data.toByte()) +
                        (this.data - other.data2.data1.data.toByte()) +
                        (this.data - other.data1.data1.data.toByte()) +
                        (this.data - other.data2.data2.data.toByte()))
                is QuadWord ->
                    ((this.data - other.data2.data2.data2.data.toByte()) +
                        (this.data - other.data2.data2.data1.data.toByte()) +
                        (this.data - other.data2.data1.data2.data.toByte()) +
                        (this.data - other.data2.data1.data1.data.toByte()) +
                        (this.data - other.data1.data1.data1.data.toByte()) +
                        (this.data - other.data1.data2.data1.data.toByte()) +
                        (this.data - other.data1.data1.data2.data.toByte()) +
                        (this.data - other.data1.data2.data2.data.toByte()))
            }
        }

        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append("0x")
                append(data.toString(16))
            }
    }
    @ExperimentalUnsignedTypes
    data class Word(val data1: Byte, val data2: Byte): DataType(){
        override fun and(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, other and data2)
                is SignedByte -> Word(data1, other.toByte() and data2)
                is Word -> Word(data1 and other.data1, data2 and other.data2)
                is DoubleWord -> Word(other.data2.data1 and data1, other.data2.data2 and data2)
                is QuadWord -> Word(other.data2.data2.data1 and data1, other.data2.data2.data2 and data2)
            }
        override fun or(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, other or data2)
                is SignedByte -> Word(data1, other.toByte() or data2)
                is Word -> Word(data1 or other.data1, data2 or other.data2)
                is DoubleWord -> Word(other.data2.data1 or data1, other.data2.data2 or data2)
                is QuadWord -> Word(other.data2.data2.data1 or data1, other.data2.data2.data2 or data2)
            }
        override fun xor(other: DataType): Word =
            when(other){
                is Byte -> Word(data1, other xor data2)
                is SignedByte -> Word(data1, other.toByte() xor data2)
                is Word -> Word(data1 xor other.data1, data2 xor other.data2)
                is DoubleWord -> Word(other.data2.data1 xor data1, other.data2.data2 xor data2)
                is QuadWord -> Word(other.data2.data2.data1 xor data1, other.data2.data2.data2 xor data2)
            }

        override fun inv(): Word =
            Word(data1.inv(), data2.inv())

        override fun shl(other: DataType): Word =
            when(other){
                is Byte -> Word(other shl data1, other shl data2)
                is SignedByte -> Word(other.toByte() shl data1, other.toByte() shl data2)
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
                is SignedByte -> Word(other.toByte() shr data1.toByte(), other.toByte() shr data2)
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

        override fun plus(other: DataType): Either<Word, String> {
            val int = (data1.data.toUInt() shl 8) or data2.data.toUInt()
            return when (other) {
                is Byte -> {
                    val sum = int + other.data
                    if (sum > 0xFFFFu) {
                        "Cannot add $other to $this as the sum is greater than size word.".right()
                    } else {
                        val higher = (sum shr 8) and 0xFFu
                        val lower = sum and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is SignedByte -> {
                    val sum = int + other.data.toUInt()
                    if (sum > 0xFFFFu) {
                        "Cannot add $other to $this as the sum is greater than size word.".right()
                    } else {
                        val higher = (sum shr 8) and 0xFFu
                        val lower = sum and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int + otherInt
                    if (sum > 0xFFFFu) {
                        "Cannot add $other to $this as the sum is greater than size word.".right()
                    } else {
                        val higher = (sum shr 8) and 0xFFu
                        val lower = sum and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is DoubleWord -> {
                    "Cannot add word to dword as dword is too large to add a word.".right()
                }
                is QuadWord ->
                    "Cannot add word to qword as qword is too large to add a word.".right()

            }
        }

        override fun minus(other: DataType): Either<Word, String> {
            val int = (data1.data.toUInt() shl 8) or data2.data.toUInt()
            return when (other) {
                is Byte -> {
                    val diff = int - other.data
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is SignedByte -> {
                    val diff = int - other.data.toUInt()
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is Word -> {
                    val otherInt = (other.data1.data.toUInt() shl 8) or other.data2.data.toUInt()
                    val diff = int - otherInt
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is DoubleWord -> {
                    "Cannot subtract word from dword as dword is too large to add a word.".right()
                }
                is QuadWord ->
                    "Cannot subtract word from qword as qword is too large to add a word.".right()
            }
        }

        override fun times(other: DataType): Either<Word, String> {
            val int = (data1.data.toUInt() shl 8) or data2.data.toUInt()
            return when (other) {
                is Byte -> {
                    val diff = int * other.data
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is SignedByte -> {
                    val diff = int * other.data.toUInt()
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is Word -> {
                    val otherInt = (other.data1.data.toUInt() shl 8) or other.data2.data.toUInt()
                    val diff = int * otherInt
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is DoubleWord -> {
                    "Cannot multiply word by dword as dword is too large to multiply a word.".right()
                }
                is QuadWord ->
                    "Cannot divide qword from word as qword is too large to divide a word by.".right()
            }
        }

        override fun div(other: DataType): Either<Word, String> {
            val int = (data1.data.toUInt() shl 8) or data2.data.toUInt()
            return when (other) {
                is Byte -> {
                    val diff = int * other.data
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is SignedByte -> {
                    val diff = int * other.data.toUInt()
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val diff = int * otherInt
                    if (diff > 0xFFFFu) {
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    } else {
                        val higher = (diff shr 8) and 0xFFu
                        val lower = diff and 0xFFu
                        Word(Byte(higher.toUByte()), Byte(lower.toUByte())).left()
                    }
                }
                is DoubleWord -> {
                    "Cannot divide word by dword as dword is too large to divide a word.".right()
                }
                is QuadWord ->
                    "Cannot divide word by qword as qword is too large to divide a word by.".right()
            }
        }

        override fun toByte(): Byte = data2
        override fun toWord(): Word = this
        override fun toDouble(): DoubleWord = DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), this)
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), Byte(0.toUByte()))), DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), this))
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> (this.data2.data - other.data).toInt()
                is SignedByte -> (this.data2.data - other.data.toUByte()).toInt()
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

        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append("0x")
                val num = (data1.data.toUInt() shl 8) or data2.data.toUInt()
                append((num and 0xFFFFu).toString(16))
            }
    }
    @ExperimentalUnsignedTypes
    data class DoubleWord(val data1: Word, val data2: Word): DataType(){
        override fun and(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, (Word(Byte(0xff.toUByte()), Byte(0xff.toUByte())) or other) and data2.data2)
                is SignedByte -> DoubleWord(data1, (Word(Byte(0xff.toUByte()), Byte(0xff.toUByte())) or other) and data2.data2)
                is Word -> DoubleWord(data1, data2 and other)
                is DoubleWord -> DoubleWord(other.data1 and data1, other.data2 and data2)
                is QuadWord -> DoubleWord(other.data2.data1 and data1, other.data2.data2 and data2)
            }
        override fun or(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 or other)
                is SignedByte -> DoubleWord(data1, data2 or other)
                is Word -> DoubleWord(data1, data2 or other.data2)
                is DoubleWord -> DoubleWord(other.data1 or data1, other.data2 or data2)
                is QuadWord -> DoubleWord(other.data2.data1 or data1, other.data2.data2 or data2)
            }
        override fun xor(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 xor other)
                is SignedByte -> DoubleWord(data1, data2 xor other)
                is Word -> DoubleWord(data1, data2 xor other)
                is DoubleWord -> DoubleWord(other.data1 xor data1, other.data2 xor data2)
                is QuadWord -> DoubleWord(other.data2.data1 xor data1, other.data2.data2 xor data2)
            }

        override fun inv(): DoubleWord =
            DoubleWord(data1.inv(), data2.inv())

        override fun shl(other: DataType): DoubleWord =
            when(other){
                is Byte -> DoubleWord(data1, data2 shl other)
                is SignedByte -> DoubleWord(data1, data2 shl other)
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
                is SignedByte -> DoubleWord(data1, data2 shr other)
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

        override fun plus(other: DataType): Either<DoubleWord, String> {
            val int = (data1.data1.data.toUInt() shl 24) or
                    (data1.data2.data.toUInt() shl 16) or
                    (data2.data1.data.toUInt() shl 8) or
                    data2.data2.data.toUInt()
            return when (other) {
                is Byte -> {
                    val sum = int + other.data
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot add $other to $this as the sum is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is SignedByte -> {
                    val sum = int + other.data.toUInt()
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot add $other to $this as the sum is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int + otherInt
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot add $other from $this as the sum is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is DoubleWord -> {
                    val otherInt = (other.data1.data1.data.toUInt() shl 24) or
                            (other.data1.data2.data.toUInt() shl 16) or
                            (other.data2.data1.data.toUInt() shl 8) or
                            other.data2.data2.data.toUInt()
                    val sum = int + otherInt
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot add $other to $this as the sum is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is QuadWord ->
                    "Cannot add $other to $this as qword is too large to add to a dword.".right()
            }
        }

        override fun minus(other: DataType): Either<DoubleWord, String>{
            val int = (data1.data1.data.toUInt() shl 24) or
                    (data1.data2.data.toUInt() shl 16) or
                    (data2.data1.data.toUInt() shl 8) or
                    data2.data2.data.toUInt()
            return when(other){
                is Byte -> {
                    val sum = int - other.data
                    if(sum > 0xFFFFFFFFu){
                        "Cannot subtract $other from $this as the sum is greater than size dword.".right()
                    }else{
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is SignedByte -> {
                    val sum = int - other.data.toUByte()
                    if(sum > 0xFFFFFFFFu){
                        "Cannot subtract $other from $this as the sum is greater than size dword.".right()
                    }else{
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int - otherInt
                    if(sum > 0xFFFFFFFFu){
                        "Cannot subtract $other from $this as the diff is greater than size word.".right()
                    }else{
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is DoubleWord -> {
                    val otherInt = (other.data1.data1.data.toUInt() shl 24) or
                            (other.data1.data2.data.toUInt() shl 16) or
                            (other.data2.data1.data.toUInt() shl 8) or
                            other.data2.data2.data.toUInt()
                    val sum = int - otherInt
                    if(sum > 0xFFFFFFFFu){
                        "Cannot subtract $other from $this as the sum is greater than size dword.".right()
                    }else{
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is QuadWord ->
                    "Cannot subtract $other from $this as qword is too big to add to dword".right()
            }
        }


        override fun times(other: DataType): Either<DoubleWord, String> {
            val int = (data1.data1.data.toUInt() shl 24) or
                    (data1.data2.data.toUInt() shl 16) or
                    (data2.data1.data.toUInt() shl 8) or
                    data2.data2.data.toUInt()
            return when (other) {
                is Byte -> {
                    val sum = int * other.data
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot multiply $other by $this as the product is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is SignedByte -> {
                    val sum = int * other.data.toUInt()
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot multiply $other by $this as the product is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int * otherInt
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot multiply $other by $this as the product is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is DoubleWord -> {
                    val otherInt = (other.data1.data1.data.toUInt() shl 24) or
                            (other.data1.data2.data.toUInt() shl 16) or
                            (other.data2.data1.data.toUInt() shl 8) or
                            other.data2.data2.data.toUInt()
                    val sum = int * otherInt
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot multiply $other by $this as the product is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is QuadWord ->
                    "Cannot subtract $other from $this as qword is too big to add to dword".right()
            }
        }

        override fun div(other: DataType): Either<DoubleWord, String> {
            val int = (data1.data1.data.toUInt() shl 24) or
                    (data1.data2.data.toUInt() shl 16) or
                    (data2.data1.data.toUInt() shl 8) or
                    data2.data2.data.toUInt()
            return when (other) {
                is Byte -> {
                    val sum = int / other.data
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot divide $this by $other as the quotient is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is SignedByte -> {
                    val sum = int / other.data.toUInt()
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot divide $this by $other as the quotient is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int / otherInt
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot divide $this by $other as the quotient is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is DoubleWord -> {
                    val otherInt = other.data1.data1.data.toUInt() or
                            other.data1.data2.data.toUInt() or
                            other.data2.data1.data.toUInt() or
                            other.data2.data2.data.toUInt()
                    val sum = int / otherInt
                    if (sum > 0xFFFFFFFFu) {
                        "Cannot divide $this by $other as the quotient is greater than size dword.".right()
                    } else {
                        val highest = (sum shr 24) and 0xFFu
                        val higher = (sum shr 16) and 0xFFu
                        val lower = (sum shr 8) and 0xFFu
                        val lowest = sum and 0xFFu
                        DoubleWord(
                            Word(
                                Byte(highest.toUByte()),
                                Byte(higher.toUByte())
                            ),
                            Word(
                                Byte(lower.toUByte()),
                                Byte(lowest.toUByte())
                            )
                        ).left()
                    }
                }
                is QuadWord ->
                    "Cannot divide $other by $this as qword is too big to divide dword by.".right()
            }
        }

        override fun toByte(): Byte = data2.data2
        override fun toWord(): Word = data2
        override fun toDouble(): DoubleWord = this
        override fun toQuad(): QuadWord = QuadWord(DoubleWord(Word(Byte(0.toUByte()), Byte(0.toUByte())), Word(Byte(0.toUByte()), Byte(0.toUByte()))), this)
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> (this.data2.data2.data - other.data).toInt()
                is SignedByte -> (this.data2.data2.data - other.data.toUByte()).toInt()
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

        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append("0x")
                val num = (data1.data1.data.toUInt() shl 24) or
                        (data1.data2.data.toUInt() shl 16) or
                        (data2.data1.data.toUInt() shl 8) or
                        data2.data2.data.toUInt()
                append(num.toString(16))
            }
    }
    @ExperimentalUnsignedTypes
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
                is SignedByte -> QuadWord(data1,
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
                is SignedByte -> QuadWord(data1,
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
                is SignedByte -> QuadWord(data1,
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
                is SignedByte -> QuadWord(data1,
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
                is SignedByte -> QuadWord(data1,
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

        override fun plus(other: DataType): Either<QuadWord, String>{
            val int = (data1.data1.data1.data.toULong() shl 54) or
                    (data1.data1.data2.data.toULong() shl 48) or
                    (data1.data2.data1.data.toULong() shl 40) or
                    (data1.data2.data2.data.toULong() shl 36) or
                    (data2.data1.data1.data.toULong() shl 24) or
                    (data2.data1.data2.data.toULong() shl 16) or
                    (data2.data2.data1.data.toULong() shl 8) or
                    data2.data2.data2.data.toULong()
            return when(other){
                is Byte -> {
                    val sum = int + other.data
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot add $this to $other as the sum is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is SignedByte -> {
                    val sum = int + other.data.toUByte()
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot add $this to $other as the sum is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int + otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot add $this to $other as the sum is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is DoubleWord -> {
                    val otherInt = other.data1.data1.data.toUInt() or
                            other.data1.data2.data.toUInt() or
                            other.data2.data1.data.toUInt() or
                            other.data2.data2.data.toUInt()
                    val sum = int + otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot add $this to $other as the sum is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is QuadWord ->{
                    val otherInt = (other.data1.data1.data1.data.toUInt() shl 54) or
                            (other.data1.data1.data2.data.toUInt() shl 48) or
                            (other.data1.data2.data1.data.toUInt() shl 40) or
                            (other.data1.data2.data2.data.toUInt() shl 36) or
                            (other.data2.data1.data1.data.toUInt() shl 24) or
                            (other.data2.data1.data2.data.toUInt() shl 16) or
                            (other.data2.data2.data1.data.toUInt() shl 8) or
                            other.data2.data2.data2.data.toUInt()
                    val sum = int + otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot add $this to $other as the sum is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
            }
        }

        override fun minus(other: DataType): Either<QuadWord, String>{
            val int = (data1.data1.data1.data.toULong() shl 54) or
                    (data1.data1.data2.data.toULong() shl 48) or
                    (data1.data2.data1.data.toULong() shl 40) or
                    (data1.data2.data2.data.toULong() shl 36) or
                    (data2.data1.data1.data.toULong() shl 24) or
                    (data2.data1.data2.data.toULong() shl 16) or
                    (data2.data2.data1.data.toULong() shl 8) or
                    data2.data2.data2.data.toULong()
            return when(other){
                is Byte -> {
                    val sum = int - other.data
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot add $this to $other as the sum is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is SignedByte -> {
                    val sum = int - other.data.toUByte()
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot add $this to $other as the sum is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int - otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot subtract $this from $other as the difference is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is DoubleWord -> {
                    val otherInt = other.data1.data1.data.toUInt() or
                            other.data1.data2.data.toUInt() or
                            other.data2.data1.data.toUInt() or
                            other.data2.data2.data.toUInt()
                    val sum = int - otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot subtract $this from $other as the difference is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is QuadWord ->{
                    val otherInt = other.data1.data1.data1.data.toUInt() or
                            other.data1.data1.data2.data.toUInt() or
                            other.data1.data2.data1.data.toUInt() or
                            other.data1.data2.data2.data.toUInt() or
                            other.data2.data1.data1.data.toUInt() or
                            other.data2.data1.data2.data.toUInt() or
                            other.data2.data2.data1.data.toUInt() or
                            other.data2.data2.data2.data.toUInt()
                    val sum = int - otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot subtract $this from $other as the difference is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
            }
        }

        override fun times(other: DataType): Either<QuadWord, String>{
            val int = (data1.data1.data1.data.toULong() shl 54) or
                    (data1.data1.data2.data.toULong() shl 48) or
                    (data1.data2.data1.data.toULong() shl 40) or
                    (data1.data2.data2.data.toULong() shl 36) or
                    (data2.data1.data1.data.toULong() shl 24) or
                    (data2.data1.data2.data.toULong() shl 16) or
                    (data2.data2.data1.data.toULong() shl 8) or
                    data2.data2.data2.data.toULong()
            return when(other){
                is Byte -> {
                    val sum = int * other.data
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot multiply $this by $other as the product is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is SignedByte -> {
                    val sum = int * other.data.toUByte()
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot multiply $this by $other as the product is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int * otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot multiply $this by $other as the product is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is DoubleWord -> {
                    val otherInt = other.data1.data1.data.toUInt() or
                            other.data1.data2.data.toUInt() or
                            other.data2.data1.data.toUInt() or
                            other.data2.data2.data.toUInt()
                    val sum = int * otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot multiply $this by $other as the product is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is QuadWord ->{
                    val otherInt = other.data1.data1.data1.data.toUInt() or
                            other.data1.data1.data2.data.toUInt() or
                            other.data1.data2.data1.data.toUInt() or
                            other.data1.data2.data2.data.toUInt() or
                            other.data2.data1.data1.data.toUInt() or
                            other.data2.data1.data2.data.toUInt() or
                            other.data2.data2.data1.data.toUInt() or
                            other.data2.data2.data2.data.toUInt()
                    val sum = int * otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot multiply $this by $other as the product is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
            }
        }

        override fun div(other: DataType): Either<QuadWord, String>{
            val int = (data1.data1.data1.data.toULong() shl 54) or
                    (data1.data1.data2.data.toULong() shl 48) or
                    (data1.data2.data1.data.toULong() shl 40) or
                    (data1.data2.data2.data.toULong() shl 36) or
                    (data2.data1.data1.data.toULong() shl 24) or
                    (data2.data1.data2.data.toULong() shl 16) or
                    (data2.data2.data1.data.toULong() shl 8) or
                    data2.data2.data2.data.toULong()
            return when(other){
                is Byte -> {
                    val sum = int / other.data
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot divide $this by $other as the quotient is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is SignedByte -> {
                    val sum = int / other.data.toUByte()
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot divide $this by $other as the quotient is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is Word -> {
                    val otherInt = other.data1.data.toUInt() or other.data2.data.toUInt()
                    val sum = int / otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot divide $this by $other as the quotient is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is DoubleWord -> {
                    val otherInt = other.data1.data1.data.toUInt() or
                            other.data1.data2.data.toUInt() or
                            other.data2.data1.data.toUInt() or
                            other.data2.data2.data.toUInt()
                    val sum = int / otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot divide $this by $other as the quotient is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
                is QuadWord ->{
                    val otherInt = other.data1.data1.data1.data.toUInt() or
                            other.data1.data1.data2.data.toUInt() or
                            other.data1.data2.data1.data.toUInt() or
                            other.data1.data2.data2.data.toUInt() or
                            other.data2.data1.data1.data.toUInt() or
                            other.data2.data1.data2.data.toUInt() or
                            other.data2.data2.data1.data.toUInt() or
                            other.data2.data2.data2.data.toUInt()
                    val sum = int / otherInt
                    if(sum > 0xFFFFFFFFFFFFFFFFu){
                        "Cannot divide $this by $other as the quotient is greater than size qword.".right()
                    }else{
                        val highest = (sum shr 54) and 0xFFu
                        val higher = (sum shr 48) and 0xFFu
                        val middleMost = (sum shr 40) and 0xFFu
                        val middle = (sum shr 36) and 0xFFu
                        val middleLeast = (sum shr 24) and 0xFFu
                        val lower = (sum shr 16) and 0xFFu
                        val lowest = (sum shr 8) and 0xFFu
                        val least = sum and 0xFFu
                        QuadWord(
                            DoubleWord(
                                Word(
                                    Byte(highest.toUByte()),
                                    Byte(higher.toUByte())
                                ),
                                Word(
                                    Byte(middleMost.toUByte()),
                                    Byte(middle.toUByte())
                                )
                            ),
                            DoubleWord(
                                Word(
                                    Byte(middleLeast.toUByte()),
                                    Byte(lower.toUByte())
                                ),
                                Word(
                                    Byte(lowest.toUByte()),
                                    Byte(least.toUByte())
                                )
                            )
                        ).left()
                    }
                }
            }
        }

        override fun toByte(): Byte = data2.data2.data2
        override fun toDouble(): DoubleWord = data2
        override fun toWord(): Word = data2.data2
        override fun toQuad(): QuadWord = this
        override fun compareTo(other: DataType): Int {
            return when(other){
                is Byte -> (this.data2.data2.data2.data - other.data).toInt()
                is SignedByte -> (this.data2.data2.data2.data - other.data.toUByte()).toInt()
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

        @ExperimentalStdlibApi
        override fun toString(): String =
            buildPrettyString {
                append("0x")
                val num = (data1.data1.data1.data.toULong() shl 54) or
                        (data1.data1.data2.data.toULong() shl 48) or
                        (data1.data2.data1.data.toULong() shl 40) or
                        (data1.data2.data2.data.toULong() shl 36) or
                        (data2.data1.data1.data.toULong() shl 24) or
                        (data2.data1.data2.data.toULong() shl 16) or
                        (data2.data2.data1.data.toULong() shl 8) or
                        data2.data2.data2.data.toULong()
                append(num.toString(16))
            }
    }

    abstract infix fun shl(other: DataType): DataType
    abstract infix fun shr(other: DataType): DataType
    abstract infix fun and(other: DataType): DataType
    abstract infix fun or(other: DataType): DataType
    abstract infix fun xor(other: DataType): DataType
    abstract fun inv(): DataType
    abstract fun plus(other: DataType): Either<DataType, String>
    abstract fun minus(other: DataType): Either<DataType, String>
    abstract fun times(other: DataType): Either<DataType, String>
    abstract fun div(other: DataType): Either<DataType, String>
    abstract operator fun compareTo(other: DataType): Int

    abstract fun toByte(): Byte
    abstract fun toWord(): Word
    abstract fun toDouble(): DoubleWord
    abstract fun toQuad(): QuadWord

    abstract override fun toString(): String

}