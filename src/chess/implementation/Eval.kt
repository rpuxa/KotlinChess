package chess.implementation

import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.constants.*
import chess.utils.bitCount
import chess.utils.getLowestBit
import chess.utils.zeroLowestBit

class Eval : AbstractEvaluate {

    override fun evaluate(position: AbstractPosition): Int {
        if (position !is BitBoard)
            return 0
        var score = 0
        for (color in 0..1) {
            val sign = if (color == WHITE) 1 else -1
            for (type in KING..PAWN) {
                //<editor-fold desc="Cost">
                score += sign * position.figures[color][type].bitCount() * FIGURES_COSTS[type]
                //</editor-fold>
            }
            for (type in 0..3) {
                //<editor-fold desc="Position">
                var board = position.figures[color][TYPES[type]]
                while (board != 0L) {
                    val cell = board.getLowestBit()
                    board = board.zeroLowestBit()
                    score += sign * if (type == 3)
                         PAWN_CELLS_COST[if (color == WHITE) 63 - cell else cell]
                    else
                         TYPE_CELLS_COST[type][cell]
                }
                //</editor-fold>
            }
        }

        return score
    }

    companion object Costs {
        val TYPES = arrayOf(KING, KNIGHT, BISHOP, PAWN)

        val FIGURES_COSTS = arrayOf(
                30000, //king
                500, //rook
                300, //knight
                320, //bishop
                900, //queen
                100 //pawn
        )

        private val PAWN_CELLS_COST = intArrayOf(
                0, 0, 0, 0, 0, 0, 0, 0,
                12, 16, 24, 32, 32, 24, 16, 12,
                12, 16, 24, 32, 32, 24, 16, 12,
                8, 12, 16, 24, 24, 16, 12, 8,
                6, 8, 12, 16, 16, 12, 8, 6,
                6, 8, 2, 10, 10, 2, 8, 6,
                4, 4, 4, 0, 0, 4, 4, 4,
                0, 0, 0, 0, 0, 0, 0, 0
        )

        private val KING_START_CELLS_COST = intArrayOf(
                5, 5, -4, -10, -10, -4, 5, 5,
                -4, -4, -8, -12, -12, -8, -4, -4,
                -12, -16, -20, -20, -20, -20, -16, -12,
                -16, -20, -24, -24, -24, -24, -20, -12,
                -16, -20, -24, -24, -24, -24, -20, -12,
                -12, -16, -20, -20, -20, -20, -16, -12,
                -4, -4, -8, -12, -12, -8, -4, -4,
                5, 5, -4, -10, -10, -4, 5, 5
        )

        private val KING_END_CELLS_COST = intArrayOf(
                0, 6, 12, 18, 18, 12, 6, 0,
                6, 12, 18, 24, 24, 18, 12, 6,
                12, 18, 24, 30, 30, 24, 18, 12,
                18, 24, 30, 36, 36, 30, 24, 18,
                18, 24, 30, 36, 36, 30, 24, 18,
                12, 18, 24, 30, 30, 24, 18, 12,
                6, 12, 18, 24, 24, 18, 12, 6,
                0, 6, 12, 18, 18, 12, 6, 0
        )

        private val KNIGHT_CELLS_COST = intArrayOf(
                0, 4, 8, 10, 10, 8, 4, 0,
                4, 8, 16, 20, 20, 16, 8, 4,
                8, 16, 20, 24, 24, 20, 16, 8,
                10, 20, 28, 32, 32, 28, 20, 10,
                10, 20, 28, 32, 32, 28, 20, 10,
                8, 16, 20, 24, 24, 20, 16, 8,
                4, 8, 16, 20, 20, 16, 8, 4,
                0, 4, 8, 10, 10, 8, 4, 0
        )

        private val BISHOP_CELLS_COST = intArrayOf(
                14, 14, 14, 14, 14, 14, 14, 14,
                14, 22, 18, 18, 18, 18, 22, 14,
                14, 18, 22, 22, 22, 22, 18, 14,
                14, 18, 22, 22, 22, 22, 18, 14,
                14, 18, 22, 22, 22, 22, 18, 14,
                14, 18, 22, 22, 22, 22, 18, 14,
                14, 22, 18, 18, 18, 18, 22, 14,
                14, 14, 14, 14, 14, 14, 14, 14
        )

        private val TYPE_CELLS_COST = arrayOf(
                KING_START_CELLS_COST,
                KNIGHT_CELLS_COST,
                BISHOP_CELLS_COST,
                PAWN_CELLS_COST
        )
    }
}