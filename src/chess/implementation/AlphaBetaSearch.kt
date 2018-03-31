package chess.implementation

import chess.RunJava.COMPUTER_PLAY_BY
import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.abstracts.AbstractSearch
import chess.constants.*
import chess.implementation.Eval.Costs.FIGURES_COSTS
import chess.utils.getVictim

class AlphaBetaSearch(evaluate: AbstractEvaluate) : AbstractSearch {
    var eval: AbstractEvaluate = evaluate
    var initDepth = 0

    override fun search(position: AbstractPosition, colorMove: Int, maxDepth: Int): Int {
        if (position !is BitBoard)
            return 0
        eval.evaluate(position)
        var bestScore = 0
        val moves = position.getSortMoves(colorMove, false)
        var betsMove: Int? = null
            for (depth in 0..maxDepth) {
                var alpha = -100000
                val beta = 100000
                for (move in moves) {
                    position.makeMove(move, colorMove)
                    initDepth = depth
                    val score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth, false, true)
                    position.unmakeMove(move, colorMove)
                    if (score > alpha) {
                        alpha = score
                        bestScore = alpha
                        betsMove = move
                    }

                }
            }
        position.clearHash()
        println("Оценка: ${(if (COMPUTER_PLAY_BY == WHITE) 1 else -1) * bestScore.toDouble() / 100}")
        return betsMove!!
    }

    private fun alphaBeta(position: BitBoard, alpha0: Int, beta: Int, colorMove: Int, depth: Int, isNullMove: Boolean, noTaking: Boolean): Int {
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
        if (!isNullMove && noTaking && !position.isCheckTo(colorMove)) { //Puring
            val evaluate = eval.evaluate(position)
            if (depth <= 2 && evaluate - 50 >= beta) //Futility Puring
                return beta
            if (depth <= 4 && evaluate - FIGURES_COSTS[QUEEN] >= beta) //Razoring
                return beta
            if (initDepth - depth >= 2 && -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth - 3, true, true) >= beta) // Null Move
                return beta
        }
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