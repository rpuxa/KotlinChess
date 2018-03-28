package chess.implementation

import chess.*
import chess.RunJava.count
import chess.abstracts.AbstractPosition
import chess.abstracts.AbstractSortingMoves
import chess.constants.*
import chess.utils.*
import java.util.*
import kotlin.collections.ArrayList

class BitBoard : AbstractPosition {
    val figures: Array<LongArray>
    private lateinit var sort: AbstractSortingMoves
    var hash = 0L
    private lateinit var hashingMoves: HashMap<Long, Array<Move>>
    private lateinit var hashingPosition: HashMap<Long, BitBoard>
    lateinit var lastPositions: ArrayDeque<Long>
    private lateinit var zKeys: Array<Array<LongArray>>

    constructor(figures: Array<LongArray>, sort: AbstractSortingMoves) {
        this.figures = figures
        this.sort = sort
        zKeys = Array(2, { Array(6, { LongArray(64) }) })
        hashingMoves = HashMap()
        hashingPosition = HashMap()
        lastPositions = ArrayDeque()
        val random = Random()
        for (color in WHITE..BLACK)
            for (type in KING..PAWN)
                for (cell in 0..63)
                    zKeys[color][type][cell] = random.nextLong()

    }

    constructor(bitBoard: BitBoard) {
        figures = arrayOf(bitBoard.figures[0].clone(), bitBoard.figures[1].clone(), bitBoard.figures[2].clone())
        hash = bitBoard.hash
    }


    companion object Positions {
        fun start(sort: AbstractSortingMoves): BitBoard {
            val bitBoard = empty(sort)
            for (i in HORIZONTAL_2)
                bitBoard.figures[WHITE][PAWN] = bitBoard.figures[WHITE][PAWN] setBit i
            for (i in HORIZONTAL_7)
                bitBoard.figures[BLACK][PAWN] = bitBoard.figures[BLACK][PAWN] setBit i
            bitBoard.figures[WHITE][ROOK] = bitBoard.figures[WHITE][ROOK].setBits(A1, H1)
            bitBoard.figures[BLACK][ROOK] = bitBoard.figures[BLACK][ROOK].setBits(A8, H8)

            bitBoard.figures[WHITE][KNIGHT] = bitBoard.figures[WHITE][KNIGHT].setBits(B1, G1)
            bitBoard.figures[BLACK][KNIGHT] = bitBoard.figures[BLACK][KNIGHT].setBits(B8, G8)

            bitBoard.figures[WHITE][BISHOP] = bitBoard.figures[WHITE][BISHOP].setBits(C1, F1)
            bitBoard.figures[BLACK][BISHOP] = bitBoard.figures[BLACK][BISHOP].setBits(C8, F8)

            bitBoard.figures[WHITE][QUEEN] = bitBoard.figures[WHITE][QUEEN].setBits(D1)
            bitBoard.figures[BLACK][QUEEN] = bitBoard.figures[BLACK][QUEEN].setBits(D8)

            bitBoard.figures[WHITE][KING] = bitBoard.figures[WHITE][KING].setBits(E1)
            bitBoard.figures[BLACK][KING] = bitBoard.figures[BLACK][KING].setBits(E8)

            for (type in KING..PAWN)
                for (color in WHITE..BLACK) {
                    bitBoard.figures[ALL][DEFAULT] = bitBoard.figures[ALL][DEFAULT] or bitBoard.figures[color][type]
                    bitBoard.figures[ALL][if (color == WHITE) ALL_WHITES else ALL_BLACKS] =
                            bitBoard.figures[ALL][if (color == WHITE) ALL_WHITES else ALL_BLACKS] or bitBoard.figures[color][type]
                }
            bitBoard.figures[ALL][ROTATED90] = bitBoard.figures[ALL][DEFAULT].to90()
            bitBoard.figures[ALL][ROTATED45] = bitBoard.figures[ALL][DEFAULT].to45()
            bitBoard.figures[ALL][ROTATED_MINUS45] = bitBoard.figures[ALL][DEFAULT].toMinus45()

            bitBoard.calculateHash()

            return bitBoard
        }

        private fun empty(sort: AbstractSortingMoves): BitBoard = BitBoard(arrayOf(
                LongArray(6),
                LongArray(6),
                LongArray(6)
        ), sort)
    }

    fun calculateHash() {
        for (color in WHITE..BLACK)
            for (type in KING..PAWN)
                for (cell in 0..63) {
                    if (figures[color][type] checkBit cell)
                        hash = hash xor zKeys[color][type][cell]
                }
    }

    private fun addMove(moves: ArrayList<Move>, from: Int, type: Int, mask0: Long, enemyColor: Int) {
        addMove(moves, from, type, mask0, enemyColor, false)
    }

    private fun addMove(moves: ArrayList<Move>, from: Int, type: Int, mask0: Long, enemyColor: Int, promotion: Boolean) {
        var mask = mask0
        while (mask != 0L) {
            val cell = mask.getLowestBit()
            val power = mask.getPowerOfLowestBit()
            mask = mask.zeroLowestBit()
            var killed = NONE
            for (t in KING..PAWN)
                if (power and figures[enemyColor][t] != 0L) {
                    killed = t
                    break
                }
            if (promotion) {
                for (figure in ROOK..QUEEN) {
                    moves.add(Move(from, cell, type, killed, figure))
                }
            } else
                moves.add(Move(from, cell, type, killed, 0))
        }
    }

    override fun getSortMoves(color: Int, onlyCaptures: Boolean): Array<Move> {
//        val hashedMoves = hashingMoves[hash]
//        if (hashedMoves == null || !equals(hashingPosition[hash]))
        return sort.sort(getMoves(color, onlyCaptures))
//        Arrays.sort(hashedMoves, {move1: Move, move2: Move ->
//            move2.score - move1.score
//        })
//        return hashedMoves
    }

    override fun getMoves(color: Int, onlyCaptures: Boolean): Array<Move> {
        val enemyColor = 1 - color
        val moves = ArrayList<Move>()
        val ourFigures = figures[ALL][if (color == WHITE) ALL_WHITES else ALL_BLACKS]
        val enemyFigures = figures[ALL][if (color == WHITE) ALL_BLACKS else ALL_WHITES]
        val ourFiguresReverse = ourFigures.inv()
        for (type in KING..PAWN) {
            var board = figures[color][type]
            while (board != 0L) {
                val cell = board.getLowestBit()
                board = board.zeroLowestBit()
                when (type) {
                    PAWN ->
                        if (color == WHITE) {
                            if (!(figures[ALL][DEFAULT] checkBit (cell + BOARD_SIZE)) && !onlyCaptures)
                                addMove(moves, cell, type, WHITE_PAWNS_MOVE[cell] and figures[ALL][DEFAULT].inv(), enemyColor, cell >= A7)
                            addMove(moves, cell, type, WHITE_PAWNS_ATTACK[cell] and enemyFigures, enemyColor, cell >= A7)
                        } else {
                            if (!(figures[ALL][DEFAULT] checkBit (cell - BOARD_SIZE)) && !onlyCaptures)
                                addMove(moves, cell, type, BLACK_PAWNS_MOVE[cell] and figures[ALL][DEFAULT].inv(), enemyColor, cell <= H2)
                            addMove(moves, cell, type, BLACK_PAWNS_ATTACK[cell] and enemyFigures, enemyColor, cell <= H2)
                        }
                    KING -> addMove(moves, cell, type, if (onlyCaptures) KING_ATTACK[cell] and enemyFigures else KING_ATTACK[cell] and ourFiguresReverse, enemyColor)
                    KNIGHT -> addMove(moves, cell, type, if (onlyCaptures) KNIGHT_ATTACK[cell] and enemyFigures else KNIGHT_ATTACK[cell] and ourFiguresReverse, enemyColor)
                    BISHOP -> {
                        val attack = BISHOP_ATTACKS45[cell][((figures[ALL][ROTATED45] ushr SHIFT45[cell]) and 255).toInt()] or BISHOP_ATTACKS_MINUS45[cell][((figures[ALL][ROTATED_MINUS45] ushr SHIFT_MINUS45[cell]) and 255).toInt()]
                        addMove(moves, cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse, enemyColor)
                    }
                    ROOK -> {
                        val attack = ROOK_ATTACKS[cell][((figures[ALL][DEFAULT] ushr SHIFT[cell]) and 255).toInt()] or ROOK_ATTACKS90[cell][((figures[ALL][ROTATED90] ushr SHIFT90[cell]) and 255).toInt()]
                        addMove(moves, cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse, enemyColor)
                    }
                    QUEEN -> {
                        val attack = BISHOP_ATTACKS45[cell][((figures[ALL][ROTATED45] ushr SHIFT45[cell]) and 255).toInt()] or BISHOP_ATTACKS_MINUS45[cell][((figures[ALL][ROTATED_MINUS45] ushr SHIFT_MINUS45[cell]) and 255).toInt()] or ROOK_ATTACKS[cell][((figures[ALL][DEFAULT] ushr SHIFT[cell]) and 255).toInt()] or ROOK_ATTACKS90[cell][((figures[ALL][ROTATED90] ushr SHIFT90[cell]) and 255).toInt()]
                        addMove(moves, cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse, enemyColor)
                    }
                }
            }
        }
        return moves.toTypedArray()
    }

    override fun makeMove(from: Int, to: Int) {
        val color = if (figures[ALL][ALL_WHITES] checkBit from) WHITE else BLACK
        var killed = NONE
        for (t in KING..PAWN)
            if ((1L shl to) and figures[1 - color][t] != 0L) {
                killed = t
                break
            }
        var type = 0
        for (t in KING..PAWN)
            if (figures[color][t] checkBit from) {
                type = t
                break
            }
        makeMove(Move(from, to, type, killed, 0), color)
    }

    override fun makeMove(move: Move, color: Int) {
        hash = hash xor zKeys[color][move.type][move.from] xor zKeys[color][move.type][move.to]
        if (move.victim != NONE)
            hash = hash xor zKeys[1 - color][move.victim][move.to]
        lastPositions.addFirst(hash)

        val allColor = ALL_WHITES + color
        figures[color][move.type] = figures[color][move.type] zeroBit move.from
        if (move.promotion == 0)
            figures[color][move.type] = figures[color][move.type] setBit move.to
        else
            figures[color][move.promotion] = figures[color][move.promotion] setBit move.to

        figures[ALL][DEFAULT] = figures[ALL][DEFAULT] zeroBit move.from
        figures[ALL][ROTATED90] = figures[ALL][ROTATED90] zeroBit ROTATE_90[move.from]
        figures[ALL][ROTATED45] = figures[ALL][ROTATED45] zeroBit ROTATE_45[move.from]
        figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] zeroBit ROTATE_MINUS45[move.from]
        figures[ALL][allColor] = figures[ALL][allColor] zeroBit move.from

        figures[ALL][DEFAULT] = figures[ALL][DEFAULT] setBit move.to
        figures[ALL][ROTATED90] = figures[ALL][ROTATED90] setBit ROTATE_90[move.to]
        figures[ALL][ROTATED45] = figures[ALL][ROTATED45] setBit ROTATE_45[move.to]
        figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] setBit ROTATE_MINUS45[move.to]
        figures[ALL][allColor] = figures[ALL][allColor] setBit move.to

        val enemyColor = 1 - color
        if (move.victim != NONE) {
            figures[enemyColor][move.victim] = figures[enemyColor][move.victim] zeroBit move.to
            figures[ALL][ALL_WHITES + enemyColor] = figures[ALL][ALL_WHITES + enemyColor] zeroBit move.to
        }
    }

    override fun unmakeMove(move: Move, color: Int) {
        hash = hash xor zKeys[color][move.type][move.from] xor zKeys[color][move.type][move.to]
        if (move.victim != NONE)
            hash = hash xor zKeys[1 - color][move.victim][move.to]
        lastPositions.pollFirst()

        val allColor = ALL_WHITES + color
        figures[color][move.type] = figures[color][move.type] setBit move.from
        if (move.promotion == 0)
            figures[color][move.type] = figures[color][move.type] zeroBit move.to
        else
            figures[color][move.promotion] = figures[color][move.promotion] zeroBit move.to

        figures[ALL][DEFAULT] = figures[ALL][DEFAULT] setBit move.from
        figures[ALL][ROTATED90] = figures[ALL][ROTATED90] setBit ROTATE_90[move.from]
        figures[ALL][ROTATED45] = figures[ALL][ROTATED45] setBit ROTATE_45[move.from]
        figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] setBit ROTATE_MINUS45[move.from]
        figures[ALL][allColor] = figures[ALL][allColor] setBit move.from

        figures[ALL][allColor] = figures[ALL][allColor] zeroBit move.to

        val enemyColor = 1 - color
        if (move.victim != NONE) {
            figures[enemyColor][move.victim] = figures[enemyColor][move.victim] setBit move.to
            figures[ALL][ALL_WHITES + enemyColor] = figures[ALL][ALL_WHITES + enemyColor] setBit move.to
        } else {
            figures[ALL][DEFAULT] = figures[ALL][DEFAULT] zeroBit move.to
            figures[ALL][ROTATED90] = figures[ALL][ROTATED90] zeroBit ROTATE_90[move.to]
            figures[ALL][ROTATED45] = figures[ALL][ROTATED45] zeroBit ROTATE_45[move.to]
            figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] zeroBit ROTATE_MINUS45[move.to]
        }
    }

    override fun result(): Int {
        var isFirst = true
        for (pos in lastPositions) {
            if (!isFirst && pos == hash)
                return DRAW
            isFirst = false
        }
        if (figures[WHITE][KING] == 0L)
            return BLACK_WINS
        if (figures[BLACK][KING] == 0L)
            return WHITE_WINS
        return CONTINUE
    }

    fun isMoveLegal(from: Int, to: Int, color: Int): Boolean {
        val move = Move(from, to, 0, 0, 0)
        for (m in getMoves(color, false)) {
            if (move == m)
                return true
        }
        return false
    }

    fun isCheckMate(to: Int): Boolean {
        for (move in getMoves(to, false)) {
            var eaten = false
            makeMove(move, to)
            for (m in getMoves(1 - to, true))
                if (m.victim == KING) {
                    eaten = true
                    break
                }
            unmakeMove(move, to)
            if (!eaten)
                return false
        }
        return true
    }

    fun equals(bitBoard: BitBoard) = Arrays.equals(figures[WHITE], bitBoard.figures[WHITE]) && Arrays.equals(figures[BLACK], bitBoard.figures[BLACK])



    fun putHash(sortMoves: Array<Move>) {
        count++
           hashingMoves.put(hash, sortMoves)
//        hashingPosition[hash] = BitBoard(this)
    }

}

