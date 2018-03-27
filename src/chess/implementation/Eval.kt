package chess.implementation

import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.constants.KING
import chess.constants.PAWN
import chess.constants.WHITE
import chess.utils.bitCount

class Eval : AbstractEvaluate {

    companion object Costs {
        val FIGURES_COSTS = arrayOf(
                30000, //king
                500, //rook
                300, //knight
                320, //bishop
                900, //queen
                100 //pawn
        )
    }

    override fun evaluate(position: AbstractPosition): Int {
        if (position !is BitBoard)
            return 0
        var score = 0
        for (color in 0..1) {
            val sign = if (color == WHITE) 1 else -1
            for (type in KING..PAWN) {
                score += sign * position.figures[color][type].bitCount() * FIGURES_COSTS[type]
            }
        }
        return score
    }
}