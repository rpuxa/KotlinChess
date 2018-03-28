package chess.abstracts

import chess.Move
import chess.movesGen

class Engine(val position: AbstractPosition,
             private val search: AbstractSearch,
             var turn: Int) {
    init {
        movesGen()
    }

    fun getAIMove() = search.search(position, turn, 5)

    fun makeMove(from: Int, to: Int) {
        position.makeMove(from, to)
        turn = 1 - turn
    }

    fun makeMove(move: Move) {
        position.makeMove(move, turn)
        turn = 1 - turn
    }

}