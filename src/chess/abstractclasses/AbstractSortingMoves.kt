package chess.abstractclasses

import chess.Move

interface AbstractSortingMoves {
    fun sort(moves: Array<Move>): Array<Move>
}