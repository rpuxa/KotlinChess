package chess.implementation

import chess.RunJava
import chess.RunJava.COMPUTER_PLAY_BY
import chess.RunJava.PLAY_WITH_COMPUTER
import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.abstracts.AbstractSearch
import chess.constants.*
import chess.implementation.Eval.Costs.FIGURES_COSTS
import chess.utils.getVictim

class AlphaBetaSearch(evaluate: AbstractEvaluate, countThreads: Int) : AbstractSearch {
    private var eval = evaluate
    private val countThreads = countThreads

    override fun search(position: AbstractPosition, colorMove: Int, maxDepth: Int): Int {
        RunJava.POSITIONS = 0
        if (position !is BitBoard)
            return 0
        eval.evaluate(position)
        val moves = position.getSortMoves(colorMove, false)
        var bestScore = 0
        var betsMove: Int? = null
            for (depth in 1..maxDepth) {
                var alpha = -100000
                val beta = 100000
                var threadsAlive = 0
                for (move in moves) {
                    Thread(Runnable {
                        val position = BitBoard()
                        position.makeMove(move, colorMove)
                        val score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, 0, depth, false, true, false)
                        position.unmakeMove(move, colorMove)
                        if (score > alpha) {
                            alpha = score
                            bestScore = alpha
                            betsMove = move
                        }
                        threadsAlive--
                    }).start()
                    threadsAlive++
                    while (threadsAlive >= countThreads)
                        Thread.sleep(10)
                }
                position.hashMove(betsMove!!)
                position.clearHashScore()
            }
        position.clearHashMoves()
        if (PLAY_WITH_COMPUTER)
            println("Оценка: ${(if (COMPUTER_PLAY_BY == WHITE) 1 else -1) * bestScore.toDouble() / 100}")
        else
            println("Оценка: ${(if (colorMove == WHITE) 1 else -1) * bestScore.toDouble() / 100}")
        return betsMove!!
    }

    private fun alphaBeta(position: BitBoard, alpha0: Int, beta: Int, colorMove: Int, depth: Int, maxDepth: Int, isNullMove: Boolean, noTaking: Boolean, isNegaScout: Boolean): Int {
        val result = position.result()
        if (result != CONTINUE) {
            if (result == BLACK_WINS || result == WHITE_WINS)
                return -30000 + depth
            if (result == DRAW)
                return 0
        }
        val res = position.getHashingScore(depth, colorMove)
        if (res != null)
            return res
        var alpha = alpha0
        if (depth >= maxDepth)
            return quies(position, colorMove, alpha0, beta) //(if (colorMove == WHITE) 1 else -1) * eval.evaluate(position)
        if (!isNullMove && noTaking && !position.isCheckTo(colorMove)) { //Puring
            val evaluate = (if (colorMove == WHITE) -1 else 1) * eval.evaluate(position)
            if (maxDepth - depth <= 2 && evaluate - 50 >= beta) //Futility Puring
                return evaluate
            if (maxDepth -  depth <= 4 && evaluate - FIGURES_COSTS[QUEEN] >= beta) //Razoring
                return evaluate
            position.setNullMoveZKey()
            if (depth >= 2 && -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth + 3, maxDepth, true, true, isNegaScout) >= beta) { // Null Move
                position.setNullMoveZKey()
                return beta
            }
            position.setNullMoveZKey()

        }
        var max = Integer.MIN_VALUE
        val moves = position.getSortMoves(colorMove, false)
        var moveNumber = 0
        for (move in moves) {
            position.makeMove(move, colorMove)
            val score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth + 1, maxDepth, isNullMove, move.getVictim() == NONE, isNegaScout)
            //NegaScout
           /*if (moveNumber == 0 || beta - alpha == 1)
             score = -alphaBeta(position, -beta, -alpha, 1 - colorMove, depth + 1, maxDepth, isNullMove, move.getVictim() == NONE)
            else {
                var tmp = -alphaBeta(position, -(alpha + 1), -alpha, 1 - colorMove, depth + 1, maxDepth, isNullMove, move.getVictim() == NONE)
                if (tmp > alpha && tmp < beta) {
                    tmp = -alphaBeta(position, -beta, -tmp, 1 - colorMove, depth + 1, maxDepth, isNullMove, move.getVictim() == NONE)
                }
                score = tmp
            }*/
            position.unmakeMove(move, colorMove)
            if (score > max)
                max = score
            if (max > alpha) {
                position.hashMove(move)
                alpha = max
            }
            if (alpha >= beta) {
                break
            }
            moveNumber++
        }
        if (!isNullMove && !isNegaScout && max < beta)
            position.hashScore(max, depth, colorMove)
        return max
    }

    private fun quies(position: BitBoard, colorMove: Int, alpha0: Int, beta: Int): Int {
        var alpha = alpha0
        val score = (if (colorMove == WHITE) 1 else -1) * eval.evaluate(position)
        if (score > alpha)
            alpha = score
        for (move in position.getSortMoves(colorMove, true)) {
            position.makeMove(move, colorMove)
            val result = -quies(position, 1 - colorMove, -beta, -alpha)
            position.unmakeMove(move, colorMove)
            if (result > alpha)
                alpha = result
            if (alpha >= beta)
                break
        }
        RunJava.POSITIONS++
        return alpha
    }
}