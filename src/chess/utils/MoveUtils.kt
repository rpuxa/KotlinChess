package chess.utils


fun createMove(from: Int, to: Int, type: Int, victim: Int, promotion: Int): Int {
    var n = from
    n = n shl 6
    n = n or to
    n = n shl 6
    n = n or type
    n = n shl 3
    n = n or victim
    n = n shl 3
    n = n or promotion
    n = n shl 3
    return n
}

fun Int.getFrom() = (this ushr 21) and 0b111111

fun Int.getTo() = (this ushr 15) and 0b111111

fun Int.getType() = (this ushr 9) and 0b111

fun Int.getVictim() = (this ushr 6) and 0b111

fun Int.getPromotion() = (this ushr 3) and 0b111

fun Int.getScore() = this and 0b111

fun Int.setScore(score: Int) = this or score
