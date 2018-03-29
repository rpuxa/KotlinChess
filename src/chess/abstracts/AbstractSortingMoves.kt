package chess.abstracts

import chess.implementation.BitBoard

interface AbstractSortingMoves {
    fun sort(moves: Array<Int>, position: BitBoard): Array<Int>
}