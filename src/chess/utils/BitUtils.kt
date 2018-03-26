package chess.utils


infix fun Long.checkBit(to: Int) = ((this shr to) and 1) != 0L

infix fun Long.setBit(to: Int) = this or (1L shl to)

fun Long.setBits(vararg bits: Int): Long {
    var result = this
    for (i in bits)
        result = result or (1L shl i)
    return result
}

infix fun Long.zeroBit(to: Int) = this and (1L shl to).inv()

fun Long.zeroBits(bits: Collection<Int>) {
    for (i in bits)
        this and (1L shl i).inv()
}

infix fun Long.swapBit(to: Int) = this xor (1L shl to)

val bitPosition = arrayOf(
        0,  1, 48,  2, 57, 49, 28,  3,
        61, 58, 50, 42, 38, 29, 17,  4,
        62, 55, 59, 36, 53, 51, 43, 22,
        45, 39, 33, 30, 24, 18, 12,  5,
        63, 47, 56, 27, 60, 41, 37, 16,
        54, 35, 52, 21, 44, 32, 23, 11,
        46, 26, 40, 15, 34, 20, 31, 10,
        25, 14, 19,  9, 13,  8,  7,  6
)

fun Long.getLowestBit(): Int {
    return bitPosition[(((this and -this) * 0x03F79D71B4CB0A89L) ushr 58).toInt()]
}

fun Long.zeroLowestBit() = this and (this - 1)


