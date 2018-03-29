package chess.abstracts

import chess.movesGen

class Engine(val position: AbstractPosition,
             private val search: AbstractSearch,
             var turn: Int) {
    init {
        movesGen()
    }

    fun getAIMove() = search.search(position, turn, 6)

    fun makeMove(from: Int, to: Int) {
        position.makeMove(from.toByte(), to.toByte())
        turn = 1 - turn
    }

    fun makeMove(move: Int) {
        position.makeMove(move, turn)
        turn = 1 - turn
    }

}