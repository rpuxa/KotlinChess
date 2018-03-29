package chess.implementation

import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.abstracts.AbstractSearch
import chess.constants.*
import chess.utils.getVictim
import com.sun.org.apache.xpath.internal.operations.Bool

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
                    val score = -alphaBeta(position as BitBoard, -beta, -alpha, 1 - colorMove, depth, false, true)
                    position.unmakeMove(move, colorMove)
                    if (score > alpha) {
                        alpha = score
                        betsMove = move
                    }

                }
            }
        return betsMove!!
    }

    private fun alphaBeta(position: BitBoard, alpha0: Int, beta: Int, colorMove: Int, depth: Int, isNullMove: Boolean, canDoNullMove: Boolean): Int {
        val result = position.result()
        if (result != CONTINUE) {
            if (result == BLACK_WINS || result == WHITE_WINS)
                return -30000 - depth
            if (result == DRAW)
                return 0
        }
        var alpha = alpha0
        if (depth <= 0)
            return (if (colorMove == WHITE) 1 else -1) * eval.evaluate(position)
        if (!isNullMove && canDoNullMove && initDepth - depth >= 2 && !position.isCheckTo(colorMove) && -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth - 3, true, true) >= beta)
            return beta
        val moves = position.getSortMoves(colorMove, false)
        for (move in moves) {
            position.makeMove(move, colorMove)
            val score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth - 1, isNullMove, move.getVictim() == NONE)
            position.unmakeMove(move, colorMove)
            if (score >= beta) {
                position.putHash(move)
                return beta
            }
            if (score > alpha)
                alpha = score
        }
        return alpha
    }
}