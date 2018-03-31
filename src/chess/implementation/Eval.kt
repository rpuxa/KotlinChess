package chess.implementation

import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.constants.*
import chess.utils.bitCount
import chess.utils.getLowestBit
import chess.utils.setBit
import chess.utils.zeroLowestBit

class Eval : AbstractEvaluate {

    override fun evaluate(position: AbstractPosition): Int {
        if (position !is BitBoard)
            return 0
        var material = 0
        var score = 0
        var beginScore = 0
        var endScore = 0
        for (color in 0..1) {
            val enemyColor = 1 - color
            val sign = if (color == WHITE) 1 else -1
            //<editor-fold desc="Cost">
            for (type in ROOK..PAWN) {

                val m = position.figures[color][type].bitCount() * FIGURES_COSTS[type]
                score += sign * 32 * m
                material += m
            }
            //</editor-fold>

            //<editor-fold desc="Position">
            for (type in KING..QUEEN) {

                var board = position.figures[color][type]
                while (board != 0L) {
                    val cell = if (color == WHITE) board.getLowestBit() else board.getLowestBit() xor 0x38
                    board = board.zeroLowestBit()
                    beginScore += sign * SCORES[BEGIN][type][cell]
                    endScore += sign * SCORES[END][type][cell]
                }
            }
            val kingVertical = position.figures[color][KING].getLowestBit() % 8
            var pawns = position.figures[color][PAWN]
            while (pawns != 0L) {
                val cell = if (color == WHITE) pawns.getLowestBit() else pawns.getLowestBit() xor 0x38
                pawns = pawns.zeroLowestBit()
                beginScore += when {
                    kingVertical < 3 -> sign * PAWN_BEGIN_KING_LEFT[cell]
                    kingVertical > 4 -> sign * PAWN_BEGIN_KING_RIGHT[cell]
                    else -> sign * PAWN_BEGIN_KING_CENTER[cell]
                }
                endScore += sign * PAWN_END_SCORE[cell]
                if ((position.figures[enemyColor][PAWN] and if (color == WHITE) PERSPECTIVE_WHITE_PAWNS_MASK[cell] else PERSPECTIVE_BLACK_PAWNS_MASK[cell xor 0x38]) == 0L)
                    score += sign * PERSPECTIVE_PAWNS_SCORE[cell]
            }
            //</editor-fold>

        }
        val phase = material / INITIAL_MATERIAL

        return ((phase * beginScore + (1 - phase) * endScore + score) / 32.0).toInt()
    }

    companion object Costs {
        val BEGIN = 0
        val END = 1

        val FIGURES_COSTS = arrayOf(
                30000, //king
                650, //rook
                423, //knight
                423, //bishop
                1268, //queen
                100 //pawn
        )

        private val PAWN_BEGIN_KING_CENTER = arrayOf(
                0,0,0,0,0,0,0,0,
                0,0,0,181,181,0,0,0,
                -543,-181,0,181,181,0,-181,-543,
                -543,-181,0,181,181,0,-181,-543,
                -543,-181,0,255,255,0,-181,-543,
                -543,-181,0,181,181,0,-181,-543,
                -543,-181,0,181,181,0,-181,-543,
                -543,-181,0,181,181,0,-181,-543
        )

        private val PAWN_BEGIN_KING_RIGHT = arrayOf(
                0,0,0,0,0,0,0,0,
                0,0,0,181,181,0,0,0,
                0,0,0,181,181,0,-181,-543,
                0,0,0,181,181,0,-181,-543,
                0,0,0,255,255,0,-181,-543,
                0,0,0,181,181,0,-181,-543,
                0,0,0,181,181,0,-181,-543,
                0,0,0,181,181,0,-181,-543
        )

        private val PAWN_BEGIN_KING_LEFT = arrayOf(
                0,0,0,0,0,0,0,0,
                0,0,0,181,181,0,0,0,
                -543,-181,0,181,181,0,0,0,
                -543,-181,0,181,181,0,0,0,
                -543,-181,0,181,181,0,0,0,
                -543,-181,0,181,181,0,0,0,
                -543,-181,0,181,181,0,0,0,
                -543,-181,0,181,181,0,0,0
        )

        private val PAWN_END_SCORE = arrayOf(
                0,0,0,0,0,0,0,0,
                0,0,0,181,181,0,0,0,
                0,0,0,181,181,0,0,0,
                0,0,0,181,181,0,0,0,
                0,0,0,255,255,0,0,0,
                0,0,0,255,255,0,0,0,
                0,0,0,255,255,0,0,0,
                0,0,0,255,255,0,0,0
        )

        private val PERSPECTIVE_PAWNS_SCORE = arrayOf(
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,
                181,181,181,181,181,181,181,181,
                255,255,255,255,255,255,255,255,
                1600,1600,1600,1600,1600,1600,1600,1600,
                3200,3200,3200,3200,3200,3200,3200,3200,
                0,0,0,0,0,0,0,0
        )

        private val KING_BEGIN_SCORE = arrayOf(
                1407,1876,938,0,0,938,1876,1407,
                1407,1876,938,0,0,938,1876,1407,
                1407,1876,938,0,0,938,1876,1407,
                1407,1876,938,0,0,938,1876,1407,
                1407,1876,938,0,0,938,1876,1407,
                1407,1876,938,0,0,938,1876,1407,
                1407,1876,938,0,0,938,1876,1407,
                1407,1876,938,0,0,938,1876,1407
        )

        private val KING_END_SCORE = arrayOf(
                -2406,-1604,-1203,-802,-802,-1203,-1604,-2406,
                -1604,-802,-401,0,0,-401,-802,-1604,
                -1203,-401,0,401,401,0,-401,-1203,
                -802,0,401,802, 802,401,0,-802,
                -802,0,401,802, 802,401,0,-802,
                -1203,-401,0,401,401,0,-401,-1203,
                -1604,-802,-401,0,0,-401,-802,-1604,
                -2406,-1604,-1203,-802,-802,-1203,-1604,-2406
        )

        private val KNIGHT_BEGIN_SCORE = arrayOf(
                -3492, -2798, -2104, -1757, -1757, -2104, -2798, -3492,
                -2440, -1746, -1052, -705, -705, -1052, -1746, -2440,
                -1388, -694, 0, 347, 347, 0, -694, -1388,
                -683, 11, 705, 1052, 1052, 705, 11, -683,
                -325, 369, 1063, 1410, 1410, 1063, 369, -325,
                -314, 380, 1074, 1421, 1421, 1074, 380, -314,
                -1366, -672, 22, 369, 369, 22, -672, -1366,
                -5618, -1724, -1030, -683, -683, -1030, -1724, -5618
        )

        private val KNIGHT_END_SCORE = arrayOf(
                -448, -336, -224, -168, -168, -224, -336, -448,
                -336, -224, -112, -56, -56, -122, -224, -336,
                -224, -112, 0, 56, 56, 0, -112, -224,
                -168, -56, 56, 112, 112, 56, -56, -168,
                -168, -56, 56, 112, 112, 56, -56, -168,
                -224, -112, 0, 56, 56, 0, -112, -224,
                -336, -224, -112, -56, -56, -122, -224, -336,
                -448, -336, -224, -168, -168, -224, -336, -448
        )

        private val BISHOP_END_SCORE = arrayOf(
                -294, -196, -147, -98, -98, -147, -196, -294,
                -196, 98, -49, 0, 0, -49, -98, -196,
                -147, -49, 0, 49, 49, 0, -49, -147,
                -98, 0, 49, 98, 98, 49, 0, -98,
                -98, 0, 49, 98, 98, 49, 0, -98,
                -147, -49, 0, 49, 49, 0, -49, -147,
                -196, 98, -49, 0, 0, -49, -98, -196,
                -294, -196, -147, -98, -98, -147, -196, -294
        )

        private val BISHOP_BEGIN_SCORE = arrayOf(
                -755, -839, -692, -545, -545, -692, -839, -755,
                -588, 84, -147, 0, 0, -147, 84, -588,
                -441, -147, 378, 147, 147, 378, -147, -441,
                -294, 0, 147, 672, 672, 147, 0, -294,
                -294, 0, 147, 672, 672, 147, 0, -294,
                -441, -147, 378, 147, 147, 378, -147, -441,
                -588, 84, -147, 0, 0, -147, 84, -588,
                -504, -588, -441, -294, -294, -441, -588, -504
        )

        private val ROOK_BEGIN_SCORE = arrayOf(
                -208, -104, 0, 104, 104, 0, -104, -208,
                -208, -104, 0, 104, 104, 0, -104, -208,
                -208, -104, 0, 104, 104, 0, -104, -208,
                -208, -104, 0, 104, 104, 0, -104, -208,
                -208, -104, 0, 104, 104, 0, -104, -208,
                -208, -104, 0, 104, 104, 0, -104, -208,
                -208, -104, 0, 104, 104, 0, -104, -208,
                -208, -104, 0, 104, 104, 0, -104, -208

        )
        private val ROOK_END_SCORE = arrayOf(
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0,
                0,0,0,0,0,0,0,0
        )

        private val QUEEN_BEGIN_SCORE = arrayOf(
                -789,-593,-495,-397,-397,-495,-593,-789,
                -392,-196, -98,   0,   0, -98,-196,-392,
                -294, -98,   0,  98,  98,   0, -98,-294,
                -196,   0,  98, 196, 196,  98,   0,-196,
                -196,   0,  98, 196, 196,  98,   0,-196,
                -294, -98,   0,  98,  98,   0, -98,-294,
                -392,-196, -98,   0,   0, -98,-196,-392,
                -588,-392,-294,-196,-196,-294,-392,-588
        )

        private val QUEEN_END_SCORE = arrayOf(
                -648,-432,-324,-216,-216,-324,-432,-648,
                -432,-216,-108,   0,   0,-108,-216,-432,
                -324,-108,   0, 108, 108,   0,-108,-324,
                -216,   0, 108, 216, 216, 108,   0,-216,
                -216,   0, 108, 216, 216, 108,   0,-216,
                -324,-108,   0, 108, 108,   0,-108,-324,
                -432,-216,-108,   0,   0,-108,-216,-432,
                -648,-432,-324,-216,-216,-324,-432,-648
        )

        private val SCORES = arrayOf(
                arrayOf(
                        KING_BEGIN_SCORE,
                        ROOK_BEGIN_SCORE,
                        KNIGHT_BEGIN_SCORE,
                        BISHOP_BEGIN_SCORE,
                        QUEEN_BEGIN_SCORE
                ),
                arrayOf(
                        KING_END_SCORE,
                        ROOK_END_SCORE,
                        KNIGHT_END_SCORE,
                        BISHOP_END_SCORE,
                        QUEEN_END_SCORE
                )
        )

        private val PERSPECTIVE_WHITE_PAWNS_MASK = Array(64, { 0L })
        private val PERSPECTIVE_BLACK_PAWNS_MASK = Array(64, { 0L })

        private val INITIAL_MATERIAL = 2.0 * (FIGURES_COSTS[ROOK] * 2 + FIGURES_COSTS[KNIGHT] * 2 + FIGURES_COSTS[BISHOP] * 2 + FIGURES_COSTS[QUEEN] + FIGURES_COSTS[PAWN] * 8)

        init {
            for (x in 0..7)
                for (y in 1..6) {
                    for (delta in 1..8) {
                        if (delta + y < 7) {
                            PERSPECTIVE_WHITE_PAWNS_MASK[cordToBit(x, y)] = PERSPECTIVE_WHITE_PAWNS_MASK[cordToBit(x, y)] setBit cordToBit(x, delta + y)
                            if (x != 0)
                                PERSPECTIVE_WHITE_PAWNS_MASK[cordToBit(x, y)] = PERSPECTIVE_WHITE_PAWNS_MASK[cordToBit(x, y)] setBit cordToBit(x, delta + y) - 1
                            if (x != 7)
                                PERSPECTIVE_WHITE_PAWNS_MASK[cordToBit(x, y)] = PERSPECTIVE_WHITE_PAWNS_MASK[cordToBit(x, y)] setBit cordToBit(x, delta + y) + 1
                        }
                    }

                    for (delta in -8..-1) {
                        if (delta + y > 0) {
                            PERSPECTIVE_BLACK_PAWNS_MASK[cordToBit(x, y)] = PERSPECTIVE_BLACK_PAWNS_MASK[cordToBit(x, y)] setBit cordToBit(x, delta + y)
                            if (x != 0)
                                PERSPECTIVE_BLACK_PAWNS_MASK[cordToBit(x, y)] = PERSPECTIVE_BLACK_PAWNS_MASK[cordToBit(x, y)] setBit cordToBit(x, delta + y) - 1
                            if (x != 7)
                                PERSPECTIVE_BLACK_PAWNS_MASK[cordToBit(x, y)] = PERSPECTIVE_BLACK_PAWNS_MASK[cordToBit(x, y)] setBit cordToBit(x, delta + y) + 1
                        }
                    }
                }
        }
    }
}