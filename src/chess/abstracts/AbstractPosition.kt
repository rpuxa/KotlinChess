package chess.abstracts

import chess.Move

interface AbstractPosition {

    val sort: AbstractSortingMoves

    fun getSortMoves(color: Int, onlyCaptures: Boolean) = sort.sort(getMoves(color, onlyCaptures))

    fun getMoves(color: Int, onlyCaptures: Boolean): Array<Move>

    fun makeMove(from: Int, to: Int)

    fun makeMove(move: Move, color: Int)

    fun unmakeMove(move: Move, color: Int)

    fun result(): Int
}