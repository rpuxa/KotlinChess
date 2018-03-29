package chess.implementation

import chess.abstracts.AbstractSortingMoves
import chess.constants.KING
import chess.constants.NONE
import chess.constants.PAWN
import chess.implementation.Eval.Costs.FIGURES_COSTS
import chess.utils.getType
import chess.utils.getVictim
import java.util.*

class SortMoves : AbstractSortingMoves {

    override fun sort(moves: Array<Int>): Array<Int> {
        //MW/LVA sort

        val mwlva = MWLVASort(moves)
        if (mwlva != null)
            return arrayOf(mwlva)


        return moves
    }


    private fun MWLVASort(moves: Array<Int>): Int? {
        Arrays.sort(moves, MWLVA_COMPARATOR)
        if (moves[0].getVictim() == KING)
            return moves[0]
        return null
    }

    companion object {
        private val MWLVA_ARRAY_COSTS = Array(6, { IntArray(6) })

        private val MWLVA_COMPARATOR = Comparator<Int> { move1: Int, move2: Int ->
            if (move1.getVictim() == NONE)
                return@Comparator if (move2.getVictim() == NONE) 0 else 1
            if (move2.getVictim() == NONE)
                return@Comparator -1
            MWLVA_ARRAY_COSTS[move2.getVictim()][move2.getType()] - MWLVA_ARRAY_COSTS[move1.getVictim()][move2.getType()]
        }

        init {
            for (victim in KING..PAWN)
                for (attacker in KING..PAWN) {
                    MWLVA_ARRAY_COSTS[victim][attacker] = 10_000_00 * FIGURES_COSTS[victim] - FIGURES_COSTS[attacker]
                }
        }
    }
}