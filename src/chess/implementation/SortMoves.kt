package chess.implementation

import chess.Move
import chess.abstracts.AbstractSortingMoves
import chess.constants.KING
import chess.constants.NONE
import chess.constants.PAWN
import chess.implementation.Eval.Costs.FIGURES_COSTS
import java.util.*

class SortMoves : AbstractSortingMoves {

    override fun sort(moves: Array<Move>): Array<Move> {
        //MW/LVA sort
        val mwlva = MWLVASort(moves)
        if (mwlva != null)
            return arrayOf(mwlva)



        return moves
    }


    fun MWLVASort(moves: Array<Move>): Move? {
         Arrays.sort(moves, MWLVA_COMPARATOR)
        if (moves[0].victim == KING)
            return moves[0]
        return null
    }

    companion object {
        private val MWLVA_ARRAY_COSTS = Array(6, { IntArray(6) })

        private val MWLVA_COMPARATOR = Comparator<Move>{move1: Move, move2: Move ->
            if (move1.victim == NONE)
                Integer.MAX_VALUE
            MWLVA_ARRAY_COSTS[move2.victim][move2.type] - MWLVA_ARRAY_COSTS[move1.victim][move2.type]
        }}

        init {
            for (victim in KING..PAWN)
                for (attacker in KING..PAWN) {
                    MWLVA_ARRAY_COSTS[victim][attacker] = 10_000_00 * FIGURES_COSTS[victim] - FIGURES_COSTS[attacker]
                }
        }
    }
}