package chess.abstractclasses

import chess.Move

class Engine(private val position: AbstractPosition,
             private val search: AbstractSearch,
             var turn: Int) {

    fun makeAIMove() = search.search(position, turn, 6)

    fun makeMove(move: Move) {
        position.makeMove(move, turn)
        turn = 1 - turn
    }

}