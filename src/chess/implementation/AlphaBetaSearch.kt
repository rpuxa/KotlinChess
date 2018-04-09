package chess.implementation

import chess.RunJava
import chess.RunJava.COMPUTER_PLAY_BY
import chess.RunJava.PLAY_WITH_COMPUTER
import chess.abstracts.AbstractEvaluate
import chess.abstracts.AbstractPosition
import chess.abstracts.AbstractSearch
import chess.constants.*
import chess.utils.getVictim
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Semaphore

class AlphaBetaSearch(evaluate: AbstractEvaluate, countThreads: Int) : AbstractSearch {
    private var eval = evaluate
    private val countThreads = countThreads
    private val hashingScore = ConcurrentHashMap<Long, Long>()
    val hashingMoves = ConcurrentHashMap<Long, Int>()

    override fun search(position: AbstractPosition, colorMove: Int, maxDepth: Int): Int {
        RunJava.POSITIONS = 0
        if (position !is BitBoard)
            return 0
        eval.evaluate(position)
        val moves = position.getSortMoves(colorMove, false)
        val semaphore = Semaphore(1)
        var bestScore = 0
        var bestMove: Int? = null
        val results = IntArray(moves.size)
        for (depth in 1..maxDepth) {
            var alpha = -100000
            val beta = 100000
            var threadsAlive = 0
            for ((moveNumber, move) in moves.withIndex()) {
                Thread(Runnable {
                    val mn = moveNumber
                    val position1 = BitBoard(position)
                    position1.makeMove(move, colorMove)
                    val score = -alphaBeta(position1, -beta, -alpha, 1 - colorMove, 0, depth, false, true, false)
                    position1.unmakeMove(move, colorMove)
                    results[mn] = score
                    /* semaphore.acquire()
                    if (score > alpha) {
                        alpha = score
                        bestScore = alpha
                        bestMove = move
                    }
                    semaphore.release()*/
                    threadsAlive--
                }).start()
                threadsAlive++
                while (threadsAlive >= countThreads)
                    Thread.sleep(10)
            }
          //  hashMove(bestMove!!, position.hash)
            hashingScore.clear()
        }
        var max = Integer.MIN_VALUE
        for (i in 0 until results.size) {
            if (results[i] > max) {
                max = results[i]
                bestScore = max
                bestMove = moves[i]
            }
        }
        hashingMoves.clear()
        if (PLAY_WITH_COMPUTER)
            println("Оценка: ${(if (COMPUTER_PLAY_BY == WHITE) 1 else -1) * bestScore.toDouble() / 100}")
        else
            println("Оценка: ${(if (colorMove == WHITE) 1 else -1) * bestScore.toDouble() / 100}")
        return bestMove!!
    }

    private fun alphaBeta(position: BitBoard, alpha0: Int, beta: Int, colorMove: Int, depth: Int, maxDepth: Int, isNullMove: Boolean, noTaking: Boolean, isNegaScout: Boolean): Int {
        val result = position.result()
        if (result != CONTINUE) {
            if (result == BLACK_WINS || result == WHITE_WINS)
                return -30000 + depth
            if (result == DRAW)
                return 0
        }
        /* val res = position.getHashingScore(depth, colorMove)
         if (res != null)
             return res*/
        var alpha = alpha0
        if (depth >= maxDepth)
            return quies(position, colorMove, alpha0, beta) //(if (colorMove == WHITE) 1 else -1) * eval.evaluate(position)
        /*if (!isNullMove && noTaking && !position.isCheckTo(colorMove)) { //Puring
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

        }*/
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
                //   hashMove(move, position.hash)
                alpha = max
            }
            if (alpha >= beta) {
                break
            }
            moveNumber++
        }
        /*if (!isNullMove && !isNegaScout && max < beta)
            hashScore(max, depth, colorMove, position.hash)*/
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


    fun hashMove(move: Int, hash: Long) {
        hashingMoves.put(hash, move)
    }

    fun hashScore(score: Int, depth: Int, color: Int, hash: Long) {
        val result = hashingScore[hash]
        if (result == null || result and 0xFF > depth)
            hashingScore[hash] = (((score.toLong() shl 31) or depth.toLong()) shl 1) or color.toLong()
    }

    fun getHashingScore(depth: Int, color: Int, hash: Long): Int? {
        val result = hashingScore[hash] ?: return null
        if (result and 1 != color.toLong())
            return null
        val d = ((result ushr 1) and 0xFF).toInt()
        if (d != depth)
            return null
        return (result ushr 32).toInt()
    }

    fun getHashingMove(hash: Long) = hashingMoves[hash]
}