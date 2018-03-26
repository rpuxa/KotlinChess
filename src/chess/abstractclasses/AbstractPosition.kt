package chess.abstractclasses

import chess.Move

interface AbstractPosition {

    val sort: AbstractSortingMoves

    fun getSortMoves(color: Int, onlyCaptures: Boolean) = sort.sort(getMoves(color, onlyCaptures))

    fun getMoves(color: Int, onlyCaptures: Boolean): Array<Move>

    fun makeMove(move: Move, color: Int)

    fun unmakeMove(move: Move)

    fun result(): Int
}