package chess.implementation

import chess.Move
import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.abstracts.AbstractSearch
import chess.constants.BLACK_WINS
import chess.constants.CONTINUE
import chess.constants.DRAW
import chess.constants.WHITE_WINS

class AlphaBetaSearch(evaluate: AbstractEvaluate) : AbstractSearch {
    var eval: AbstractEvaluate = evaluate

    override fun search(position: AbstractPosition, colorMove: Int, maxDepth: Int): Move {
        val moves = position.getSortMoves(colorMove, false)
        var betsMove: Move? = null
        for (depth in 0..maxDepth) {
            var alpha = -100000
            val beta = 100000
            for (move in moves) {
                position.makeMove(move, colorMove)
                val score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth)
                position.unmakeMove(move, colorMove)
                if (score > alpha) {
                    alpha = score
                    betsMove = move
                }
            }
        }
        return betsMove!!
    }

    private fun alphaBeta(position: AbstractPosition, alpha0: Int, beta: Int, colorMove: Int, depth: Int): Int {
        val result = position.result()
        if (result != CONTINUE) {
            if (result == BLACK_WINS)
                return -30000
            if (result == WHITE_WINS)
                return 30000
            if (result == DRAW)
                return 0
        }
        var alpha = alpha0
        if (depth <= 0)
            return -colorMove * eval.evaluate(position)
        val moves = position.getSortMoves(colorMove, false)
        for (move in moves) {
            position.makeMove(move, colorMove)
            val score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth - 1)
            position.unmakeMove(move, colorMove)
            if (score >= beta)
                return beta
            if (score > alpha)
                alpha = score
        }
        return alpha
    }
}