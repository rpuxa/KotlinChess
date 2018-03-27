package chess.implementation

import chess.Move
import chess.abstracts.AbstractSortingMoves

class NullSort : AbstractSortingMoves {
    override fun sort(moves: Array<Move>) = moves
}