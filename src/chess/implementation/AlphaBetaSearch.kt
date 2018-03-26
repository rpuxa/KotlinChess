package chess.implementation

import chess.Move
import chess.abstractclasses.AbstractEvaluate
import chess.abstractclasses.AbstractPosition
import chess.abstractclasses.AbstractSearch

class AlphaBetaSearch : AbstractSearch {
    lateinit var eval: AbstractEvaluate

    override fun search(position: AbstractPosition, colorMove: Int, maxDepth: Int): Move {
        for (depth in 1..maxDepth) {

        }
    }

    fun alphaBeta(position: AbstractPosition, alpha0: Int, beta: Int, colorMove: Int, depth: Int): Int {
        var alpha = alpha0
        if (depth <= 0)
            return eval.evaluate(position)
        val moves = position.getMoves(colorMove, false)
        for (move in moves) {
            position.makeMove(move, colorMove)
            val score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth - 1)
            position.unmakeMove(move)
            if (score >= beta)
                return beta
            if (score > alpha)
                alpha = score
        }
        return alpha
    }
}