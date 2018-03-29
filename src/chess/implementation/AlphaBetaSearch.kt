package chess.implementation

import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.abstracts.AbstractSearch
import chess.constants.*

class AlphaBetaSearch(evaluate: AbstractEvaluate) : AbstractSearch {
    var eval: AbstractEvaluate = evaluate
    var initDepth = 0

    override fun search(position: AbstractPosition, colorMove: Int, maxDepth: Int): Int {
        val moves = position.getSortMoves(colorMove, false)
        var betsMove: Int? = null
            for (depth in 0..maxDepth) {
                var alpha = -100000
                val beta = 100000
                for (move in moves) {
                    position.makeMove(move, colorMove)
                    initDepth = depth
                    val score = -alphaBeta(position as BitBoard, -beta, -alpha, 1 - colorMove, depth, false)
                    position.unmakeMove(move, colorMove)
                    if (score > alpha) {
                        alpha = score
                        betsMove = move
                    }

                }
            }
        return betsMove!!
    }

    private fun alphaBeta(position: BitBoard, alpha0: Int, beta: Int, colorMove: Int, depth: Int, isNullMove: Boolean): Int {
        val result = position.result()
        val sign = if (colorMove == WHITE) 1 else -1
        if (result != CONTINUE) {
            if (result == BLACK_WINS)
                return sign * (-30000 - depth)
            if (result == WHITE_WINS)
                return sign * (30000 + depth)
            if (result == DRAW)
                return 0
        }
        var alpha = alpha0
        if (depth <= 0)
            return sign * eval.evaluate(position)
        if (!isNullMove && initDepth - depth >= 2 && -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth - 3, true) >= beta)
            return beta
        val moves = position.getSortMoves(colorMove, false)
        position.putHash(moves)
        for (move in moves) {
            position.makeMove(move, colorMove)
            val score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth - 1, isNullMove)
            //move.score = score
            position.unmakeMove(move, colorMove)
            if (score >= beta) {
                return beta
            }
            if (score > alpha)
                alpha = score
        }
        return alpha
    }
}