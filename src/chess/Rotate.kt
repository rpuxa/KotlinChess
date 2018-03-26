package chess

import chess.constants.BOARD_SIZE
import chess.constants.cordToBit
import chess.utils.checkBit
import chess.utils.setBit
import chess.utils.setBits

val ROTATE_90 = arrayOf(
        0, 8, 16, 24, 32, 40, 48, 56,
        1, 9, 17, 25, 33, 41, 49, 57,
        2, 10, 18, 26, 34, 42, 50, 58,
        3, 11, 19, 27, 35, 43, 51, 59,
        4, 12, 20, 28, 36, 44, 52, 60,
        5, 13, 21, 29, 37, 45, 53, 61,
        6, 14, 22, 30, 38, 46, 54, 62,
        7, 15, 23, 31, 39, 47, 55, 63
)
val ROTATE_45 = arrayOf(
        0, 2, 5, 9, 14, 20, 27, 35,
        1, 4, 8, 13, 19, 26, 34, 42,
        3, 7, 12, 18, 25, 33, 41, 48,
        6, 11, 17, 24, 32, 40, 47, 53,
        10, 16, 23, 31, 39, 46, 52, 57,
        15, 22, 30, 38, 45, 51, 56, 60,
        21, 29, 37, 44, 50, 55, 59, 62,
        28, 36, 43, 49, 54, 58, 61, 63
)

val ROTATE_MINUS45 = arrayOf(
        28, 21, 15, 10, 6, 3, 1, 0,
        36, 29, 22, 16, 11, 7, 4, 2,
        43, 37, 30, 23, 17, 12, 8, 5,
        49, 44, 38, 31, 24, 18, 13, 9,
        54, 50, 45, 39, 32, 25, 19, 14,
        58, 55, 51, 46, 40, 33, 26, 20,
        61, 59, 56, 52, 47, 41, 34, 27,
        63, 62, 60, 57, 53, 48, 42, 35
)


val SHIFT = Array(64, { 0 })
val SHIFT90 = Array(64, { 0 })
val SHIFT45 = arrayOf(
        0, 1, 3, 6, 10, 15, 21, 28,
        1, 3, 6, 10, 15, 21, 28, 36,
        3, 6, 10, 15, 21, 28, 36, 43,
        6, 10, 15, 21, 28, 36, 43, 49,
        10, 15, 21, 28, 36, 43, 49, 54,
        15, 21, 28, 36, 43, 49, 54, 58,
        21, 28, 36, 43, 49, 54, 58, 61,
        28, 36, 43, 49, 54, 58, 61, 63
)
val SHIFT_MINUS45 = arrayOf(
        28, 21, 15, 10, 6, 3, 1, 0,
        36, 28, 21, 15, 10, 6, 3, 1,
        43, 36, 28, 21, 15, 10, 6, 3,
        49, 43, 36, 28, 21, 15, 10, 6,
        54, 49, 43, 36, 28, 21, 15, 10,
        58, 54, 49, 43, 36, 28, 21, 15,
        61, 58, 54, 49, 43, 36, 28, 21,
        63, 61, 58, 54, 49, 43, 36, 28
)

fun calculateShifts() {
    for (cell in 0..63) {
        SHIFT[cell] = 8 * (cell / 8)
        SHIFT90[cell] = 8 * (cell % 8)
    }
}

val BISHOP_ATTACKS45 = Array(64, { Array(256, { 0L }) })
val BISHOP_ATTACKS_MINUS45 = Array(64, { Array(256, { 0L }) })
val ROOK_ATTACKS = Array(64, { Array(256, { 0L }) })
val ROOK_ATTACKS90 = Array(64, { Array(256, { 0L }) })

val WHITE_PAWNS_MOVE = LongArray(64)
val BLACK_PAWNS_MOVE = LongArray(64)
val WHITE_PAWNS_ATTACK = LongArray(64)
val BLACK_PAWNS_ATTACK = LongArray(64)

val KNIGHT_ATTACK = LongArray(64)
val KING_ATTACK = LongArray(64)

fun movesGen() {
    calculateShifts()
    moveGenBishop()
    moveGenKing()
    moveGenKnight()
    moveGenPawns()
    moveGenRook()
}

fun moveGenBishop() {
    for (x in 0..7)
        for (y in 0..7) {
            val cell = BOARD_SIZE * y + x
            for (capture in 0L..255) {
                val board = (capture shl SHIFT45[cell]).from45()
                var attackCells = 0L
                val directions = arrayOf(
                        arrayOf(-1, 1),
                        arrayOf(1, -1)
                )
                for (dir in directions)
                    for (delta in 1..7) {
                        val x1 = dir[0] * delta + x
                        val y1 = dir[1] * delta + y
                        val bit = cordToBit(x1, y1)
                        if (isInBoard(x1, y1)) {
                            attackCells = attackCells setBit bit
                        }
                        if (board checkBit bit)
                            break
                    }
                BISHOP_ATTACKS45[cell][capture.toInt()] = attackCells
            }
            for (capture in 0L..255) {
                val board = (capture shl SHIFT_MINUS45[cell]).fromMinus45()
                var attackCells = 0L
                val directions = arrayOf(
                        arrayOf(1, 1),
                        arrayOf(-1, -1)
                )
                for (dir in directions)
                    for (delta in 1..7) {
                        val x1 = dir[0] * delta + x
                        val y1 = dir[1] * delta + y
                        val bit = cordToBit(x1, y1)
                        if (isInBoard(x1, y1)) {
                            attackCells = attackCells setBit bit
                        }
                        if (board checkBit bit)
                            break
                    }
                BISHOP_ATTACKS_MINUS45[cell][capture.toInt()] = attackCells
            }
        }
}

fun moveGenRook() {
    for (x in 0..7)
        for (y in 0..7) {
            val cell = BOARD_SIZE * y + x
            for (capture in 0L..255) {
                val board = capture shl SHIFT[cell]
                var attackCells = 0L
                val directions = arrayOf(
                        arrayOf(1, 0),
                        arrayOf(-1, 0)
                )
                for (dir in directions)
                    for (delta in 1..7) {
                        val x1 = dir[0] * delta + x
                        val y1 = dir[1] * delta + y
                        val bit = cordToBit(x1, y1)
                        if (isInBoard(x1, y1)) {
                            attackCells = attackCells setBit bit
                        }
                        if (board checkBit bit)
                            break
                    }
                ROOK_ATTACKS[cell][capture.toInt()] = attackCells
            }
            for (capture in 0L..255) {
                val board = (capture shl SHIFT90[cell]).from90()
                var attackCells = 0L
                val directions = arrayOf(
                        arrayOf(0, 1),
                        arrayOf(0, -1)
                )
                for (dir in directions)
                    for (delta in 1..7) {
                        val x1 = dir[0] * delta + x
                        val y1 = dir[1] * delta + y
                        val bit = cordToBit(x1, y1)
                        if (isInBoard(x1, y1)) {
                            attackCells = attackCells setBit bit
                        }
                        if (board checkBit bit)
                            break
                    }
                ROOK_ATTACKS90[cell][capture.toInt()] = attackCells
            }
        }
}


fun moveGenPawns() {
    for (x in 0..7)
        for (y in 0..7) {
            val cell = cordToBit(x, y)
            when (y) {
                1 -> WHITE_PAWNS_MOVE[cell] = WHITE_PAWNS_MOVE[cell] setBit cordToBit(x, y + 2)
                6 -> BLACK_PAWNS_MOVE[cell] = BLACK_PAWNS_MOVE[cell] setBit cordToBit(x, y - 2)
            }
            WHITE_PAWNS_MOVE[cell] = WHITE_PAWNS_MOVE[cell] setBit cordToBit(x, y + 1)
            BLACK_PAWNS_MOVE[cell] = BLACK_PAWNS_MOVE[cell] setBit cordToBit(x, y - 1)
            when (x) {
                0 -> {
                    WHITE_PAWNS_ATTACK[cell] = WHITE_PAWNS_ATTACK[cell] setBit cordToBit(1, y + 1)
                    BLACK_PAWNS_ATTACK[cell] = BLACK_PAWNS_ATTACK[cell] setBit cordToBit(1, y - 1)
                }
                7 -> {
                    WHITE_PAWNS_ATTACK[cell] = WHITE_PAWNS_ATTACK[cell] setBit cordToBit(6, y + 1)
                    BLACK_PAWNS_ATTACK[cell] = BLACK_PAWNS_ATTACK[cell] setBit cordToBit(6, y - 1)
                }

                else -> {
                    BLACK_PAWNS_ATTACK[cell] = BLACK_PAWNS_ATTACK[cell].setBits(
                            cordToBit(x - 1, y - 1),
                            cordToBit(x + 1, y - 1)
                    )
                    WHITE_PAWNS_ATTACK[cell] = BLACK_PAWNS_ATTACK[cell].setBits(
                            cordToBit(x - 1, y + 1),
                            cordToBit(x + 1, y + 1)
                    )
                }
            }
        }
}

fun moveGenKnight() {
    val directions = arrayOf(
            arrayOf(1, 2),
            arrayOf(-1, 2),
            arrayOf(1, -2),
            arrayOf(-1, -2),
            arrayOf(2, 1),
            arrayOf(2, -1),
            arrayOf(-2, 1),
            arrayOf(-2, -1)
    )
    for (x in 0..7)
        for (y in 0..7) {
            val cell = cordToBit(x, y)
            for (d in directions)
                if (isInBoard(x + d[0], y + d[1])) {
                    KNIGHT_ATTACK[cell] = KNIGHT_ATTACK[cell] setBit cordToBit(x + d[0], y + d[1])
                }
        }
}

fun moveGenKing() {
        val directions = arrayOf(
                arrayOf(-1, 0),
                arrayOf(-1, 1),
                arrayOf(0, 1),
                arrayOf(1, 1),
                arrayOf(1, 0),
                arrayOf(1, -1),
                arrayOf(0, -1),
                arrayOf(-1, -1)
        )
        for (x in 0..7)
            for (y in 0..7) {
                val cell = cordToBit(x, y)
                for (d in directions)
                    if (isInBoard(x + d[0], y + d[1])) {
                        KING_ATTACK[cell] = KING_ATTACK[cell] setBit cordToBit(x + d[0], y + d[1])
                    }
            }
}

fun Long.to45(): Long {
    var newBoard = 0L
    for (bit in 0..63) {
        if (this checkBit bit)
            newBoard = newBoard setBit ROTATE_45[bit]
    }
    return newBoard
}

fun Long.to90(): Long {
    var newBoard = 0L
    for (bit in 0..63) {
        if (this checkBit bit)
            newBoard = newBoard setBit ROTATE_90[bit]
    }
    return newBoard
}

fun Long.toMinus45(): Long {
    var newBoard = 0L
    for (bit in 0..63) {
        if (this checkBit bit)
            newBoard = newBoard setBit ROTATE_MINUS45[bit]
    }
    return newBoard
}

private fun Long.from45(): Long {
    var newBoard = 0L
    for (bit in 0..63) {
        if (this checkBit bit)
            newBoard = newBoard setBit ROTATE_45.indexOf(bit)
    }
    return newBoard
}

private fun Long.from90(): Long {
    var newBoard = 0L
    for (bit in 0..63) {
        if (this checkBit bit)
            newBoard = newBoard setBit ROTATE_90.indexOf(bit)
    }
    return newBoard
}

private fun Long.fromMinus45(): Long {
    var newBoard = 0L
    for (bit in 0..63) {
        if (this checkBit bit)
            newBoard = newBoard setBit ROTATE_MINUS45.indexOf(bit)
    }
    return newBoard
}

private fun isInBoard(x: Int, y: Int) = x >= 0 && y >= 0 && x < BOARD_SIZE && y < BOARD_SIZE
