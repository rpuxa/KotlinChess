package chess.abstracts

import chess.Move

interface AbstractSortingMoves {
    fun sort(moves: Array<Move>): Array<Move>
}