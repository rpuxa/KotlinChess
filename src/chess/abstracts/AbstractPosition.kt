package chess.abstracts

import chess.Move

interface AbstractPosition {

    fun getSortMoves(color: Int, onlyCaptures: Boolean): Array<Move>

    fun getMoves(color: Int, onlyCaptures: Boolean): Array<Move>

    fun makeMove(from: Int, to: Int)

    fun makeMove(move: Move, color: Int)

    fun unmakeMove(move: Move, color: Int)

    fun result(): Int
}