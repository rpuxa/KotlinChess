package chess.constants


const val WHITE = 0
const val BLACK = 1
const val ALL = 2

const val DEFAULT = 0
const val ROTATED90 = 1
const val ROTATED45 = 2
const val ROTATED_MINUS45 = 3
const val ALL_WHITES = 4
const val ALL_BLACKS = 5

const val NONE = 6
const val KING = 0
const val ROOK = 1
const val KNIGHT = 2
const val BISHOP = 3
const val QUEEN = 4
const val PAWN = 5


const val COUNT = 12
const val BOARD_SIZE = 8

const val CONTINUE = 0
const val BLACK_WINS = 1
const val WHITE_WINS = 2
const val DRAW = 3


val HORIZONTAL_2 = 8..15
val HORIZONTAL_7 = 48..55

const val A1 = 0
const val B1 = 1
const val C1 = 2
const val D1 = 3
const val E1 = 4
const val F1 = 5
const val G1 = 6
const val H1 = 7
const val H2 = 15
const val A7 = 48
const val A8 = 56
const val B8 = 57
const val C8 = 58
const val D8 = 59
const val E8 = 60
const val F8 = 61
const val G8 = 62
const val H8 = 63


fun cordToBit(x: Int, y: Int) = x + BOARD_SIZE * y

