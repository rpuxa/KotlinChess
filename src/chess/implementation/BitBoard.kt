package chess.implementation

import chess.*
import chess.constants.*
import chess.abstracts.AbstractPosition
import chess.abstracts.AbstractSortingMoves
import chess.utils.*
import java.util.*
import kotlin.collections.HashMap

class BitBoard : AbstractPosition {
    val figures: Array<LongArray>
    private var sort: AbstractSortingMoves
    var hash = 0L
    private val moves = Array(100, { 0 })
    private var size = 0
    var lastMovingFigure = ArrayDeque<Int>()
    private var lastPositions: ArrayDeque<Long>
    private var zKeys: Array<Array<LongArray>>
    private var nullMoveZKey = 0L
    private val search: AlphaBetaSearch

    constructor(figures: Array<LongArray>, sort: AbstractSortingMoves, search: AlphaBetaSearch) {
        this.figures = figures
        this.sort = sort
        zKeys = Array(2, { Array(6, { LongArray(64) }) })
        // hashingPosition = HashMap()
        lastPositions = ArrayDeque()
        val random = Random()
        for (color in WHITE..BLACK)
            for (type in KING..PAWN)
                for (cell in 0..63)
                    zKeys[color][type][cell] = random.nextLong()
        nullMoveZKey = random.nextLong()
        this.search = search
    }

    constructor(bitBoard: BitBoard) {
        figures = arrayOf(bitBoard.figures[0].clone(), bitBoard.figures[1].clone(), bitBoard.figures[2].clone())
        hash = bitBoard.hash
        zKeys = bitBoard.zKeys
        lastPositions = ArrayDeque(bitBoard.lastPositions)
        nullMoveZKey = bitBoard.nullMoveZKey
        lastMovingFigure = ArrayDeque(bitBoard.lastMovingFigure)
        lastPositions = ArrayDeque(bitBoard.lastPositions)
        sort = bitBoard.sort
        search = bitBoard.search
    }


    companion object Positions {
        fun start(sort: AbstractSortingMoves, search: AlphaBetaSearch): BitBoard {
            val bitBoard = empty(sort, search)
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

        fun empty(sort: AbstractSortingMoves, search: AlphaBetaSearch): BitBoard = BitBoard(arrayOf(
                LongArray(6),
                LongArray(6),
                LongArray(6)
        ), sort, search)
    }

    fun calculateHash() {
        for (color in WHITE..BLACK)
            for (type in KING..PAWN)
                for (cell in 0..63) {
                    if (figures[color][type] checkBit cell)
                        hash = hash xor zKeys[color][type][cell]
                }
    }

    private fun addMove(from: Int, type: Int, mask0: Long, enemyColor: Int) {
        addMove(from, type, mask0, enemyColor, false)
    }

    private fun addMove(from: Int, type: Int, mask0: Long, enemyColor: Int, promotion: Boolean) {
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
                    moves[size] = createMove(from, cell, type, killed, figure)
                    size++
                }
            } else {
                moves[size] = createMove(from, cell, type, killed, 0)
                size++
            }

        }
    }

    override fun getSortMoves(color: Int, onlyCaptures: Boolean): Array<Int> {
//        val hashedMoves = hashingMoves[hash]
//        if (hashedMoves == null || !equals(hashingPosition[hash]))
        return sort.sort(getMoves(color, onlyCaptures), this)
//        Arrays.sort(hashedMoves, {move1: Move, move2: Move ->
//            move2.score - move1.score
//        })
//        return hashedMoves
    }

    override fun getMoves(color: Int, onlyCaptures: Boolean): Array<Int> {
        size = 0
        val enemyColor = 1 - color
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
                                addMove(cell, type, WHITE_PAWNS_MOVE[cell] and figures[ALL][DEFAULT].inv(), enemyColor, cell >= A7)
                            addMove(cell, type, WHITE_PAWNS_ATTACK[cell] and enemyFigures, enemyColor, cell >= A7)
                        } else {
                            if (!(figures[ALL][DEFAULT] checkBit (cell - BOARD_SIZE)) && !onlyCaptures)
                                addMove(cell, type, BLACK_PAWNS_MOVE[cell] and figures[ALL][DEFAULT].inv(), enemyColor, cell <= H2)
                            addMove(cell, type, BLACK_PAWNS_ATTACK[cell] and enemyFigures, enemyColor, cell <= H2)
                        }
                    KING -> {
                        addMove(cell, type, if (onlyCaptures) KING_ATTACK[cell] and enemyFigures else KING_ATTACK[cell] and ourFiguresReverse, enemyColor)
                        if (!onlyCaptures) {
                            if (figures[ALL][DEFAULT] and CASTLE[color][LONG_CASTLE] == 0L) {
                                if (color == 0 && cell == E1 && figures[0][ROOK] checkBit A1) {
                                    moves[size] = createMove(E1, C1, KING, NONE, 0)
                                    size++
                                } else if (color == 1 && cell == E8 && figures[1][ROOK] checkBit A8) {
                                    moves[size] = createMove(E8, C8, KING, NONE, 0)
                                    size++
                                }
                            }
                            if (CASTLE[color][SHORT_CASTLE] and figures[ALL][DEFAULT] == 0L) {
                                if (color == 0 && cell == E1 && figures[0][ROOK] checkBit H1) {
                                    moves[size] = createMove(E1, G1, KING, NONE, 0)
                                    size++
                                } else if (color == 1 && cell == E8 && figures[1][ROOK] checkBit H8) {
                                    moves[size] = createMove(E8, G8, KING, NONE, 0)
                                    size++
                                }
                            }
                        }
                    }
                    KNIGHT -> addMove(cell, type, if (onlyCaptures) KNIGHT_ATTACK[cell] and enemyFigures else KNIGHT_ATTACK[cell] and ourFiguresReverse, enemyColor)
                    BISHOP -> {
                        val attack = BISHOP_ATTACKS45[cell][((figures[ALL][ROTATED45] ushr SHIFT45[cell]) and 255).toInt()] or BISHOP_ATTACKS_MINUS45[cell][((figures[ALL][ROTATED_MINUS45] ushr SHIFT_MINUS45[cell]) and 255).toInt()]
                        addMove(cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse, enemyColor)
                    }
                    ROOK -> {
                        val attack = ROOK_ATTACKS[cell][((figures[ALL][DEFAULT] ushr SHIFT[cell]) and 255).toInt()] or ROOK_ATTACKS90[cell][((figures[ALL][ROTATED90] ushr SHIFT90[cell]) and 255).toInt()]
                        addMove(cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse, enemyColor)
                    }
                    QUEEN -> {
                        val attack = BISHOP_ATTACKS45[cell][((figures[ALL][ROTATED45] ushr SHIFT45[cell]) and 255).toInt()] or BISHOP_ATTACKS_MINUS45[cell][((figures[ALL][ROTATED_MINUS45] ushr SHIFT_MINUS45[cell]) and 255).toInt()] or ROOK_ATTACKS[cell][((figures[ALL][DEFAULT] ushr SHIFT[cell]) and 255).toInt()] or ROOK_ATTACKS90[cell][((figures[ALL][ROTATED90] ushr SHIFT90[cell]) and 255).toInt()]
                        addMove(cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse, enemyColor)
                    }
                }
            }
        }

        return Arrays.copyOf(moves, size)
    }

    override fun makeMove(from: Byte, to: Byte) {
        val color = if (figures[ALL][ALL_WHITES] checkBit from.toInt()) WHITE else BLACK
        var killed = NONE
        for (t in KING..PAWN)
            if ((1L shl to.toInt()) and figures[1 - color][t] != 0L) {
                killed = t
                break
            }
        var type = 0
        for (t in KING..PAWN)
            if (figures[color][t] checkBit from.toInt()) {
                type = t
                break
            }
        makeMove(createMove(from.toInt(), to.toInt(), type, killed, 0), color)
    }

    override fun makeMove(move: Int, color: Int) {
        val type = move.getType()
        val from = move.getFrom()
        val to = move.getTo()
        val promotion = move.getPromotion()
        val victim = move.getVictim()

        hash = hash xor zKeys[color][type][from] xor zKeys[color][type][to]
        if (victim != NONE)
            hash = hash xor zKeys[1 - color][victim][to]
        lastPositions.addFirst(hash)
        lastMovingFigure.addFirst(to)

        val allColor = ALL_WHITES + color
        //<editor-fold desc="Castling ">
        if (type == KING && Math.abs(from - to) == 2) {
            val castle = when (to) {
                C1, C8 -> LONG_CASTLE
                G1, G8 -> SHORT_CASTLE
                else -> -1
            }
            figures[color][ROOK] = figures[color][ROOK] xor CASTLE_MASK[color][castle][ROOK]
            figures[color][KING] = figures[color][KING] xor CASTLE_MASK[color][castle][KING]

            figures[ALL][DEFAULT] = figures[ALL][DEFAULT] xor CASTLE_MASK[color][castle][ROOK] xor CASTLE_MASK[color][castle][KING]
            figures[ALL][ROTATED90] = figures[ALL][ROTATED90] xor (CASTLE_MASK[color][castle][ROOK] xor CASTLE_MASK[color][castle][KING]).to90()
            figures[ALL][ROTATED45] = figures[ALL][ROTATED45] xor (CASTLE_MASK[color][castle][ROOK] xor CASTLE_MASK[color][castle][KING]).to45()
            figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] xor (CASTLE_MASK[color][castle][ROOK] xor CASTLE_MASK[color][castle][KING]).toMinus45()
            figures[ALL][allColor] = figures[ALL][allColor] xor CASTLE_MASK[color][castle][ROOK] xor CASTLE_MASK[color][castle][KING]
            return
        }
        //</editor-fold>

        figures[color][type] = figures[color][type] zeroBit from
        if (promotion == 0)
            figures[color][type] = figures[color][type] setBit to
        else
            figures[color][promotion] = figures[color][promotion] setBit to

        figures[ALL][DEFAULT] = figures[ALL][DEFAULT] zeroBit from
        figures[ALL][ROTATED90] = figures[ALL][ROTATED90] zeroBit ROTATE_90[from]
        figures[ALL][ROTATED45] = figures[ALL][ROTATED45] zeroBit ROTATE_45[from]
        figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] zeroBit ROTATE_MINUS45[from]
        figures[ALL][allColor] = figures[ALL][allColor] zeroBit from

        figures[ALL][DEFAULT] = figures[ALL][DEFAULT] setBit to
        figures[ALL][ROTATED90] = figures[ALL][ROTATED90] setBit ROTATE_90[to]
        figures[ALL][ROTATED45] = figures[ALL][ROTATED45] setBit ROTATE_45[to]
        figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] setBit ROTATE_MINUS45[to]
        figures[ALL][allColor] = figures[ALL][allColor] setBit to

        val enemyColor = 1 - color
        if (victim != NONE) {
            figures[enemyColor][victim] = figures[enemyColor][victim] zeroBit to
            figures[ALL][ALL_WHITES + enemyColor] = figures[ALL][ALL_WHITES + enemyColor] zeroBit to
        }
    }

    override fun unmakeMove(move: Int, color: Int) {
        val type = move.getType()
        val from = move.getFrom()
        val to = move.getTo()
        val promotion = move.getPromotion()
        val victim = move.getVictim()
        hash = hash xor zKeys[color][type][from] xor zKeys[color][type][to]
        if (victim != NONE)
            hash = hash xor zKeys[1 - color][victim][to]
        lastPositions.pollFirst()
        lastMovingFigure.pollFirst()

        val allColor = ALL_WHITES + color
        //<editor-fold desc="Castling">
        if (type == KING && Math.abs(from - to) == 2) {
            val castle = when (to) {
                C1, C8 -> LONG_CASTLE
                G1, G8 -> SHORT_CASTLE
                else -> -1
            }
            figures[color][ROOK] = figures[color][ROOK] xor CASTLE_MASK[color][castle][ROOK]
            figures[color][KING] = figures[color][KING] xor CASTLE_MASK[color][castle][KING]

            figures[ALL][DEFAULT] = figures[ALL][DEFAULT] xor CASTLE_MASK[color][castle][ROOK] xor CASTLE_MASK[color][castle][KING]
            figures[ALL][ROTATED90] = figures[ALL][ROTATED90] xor CASTLE_MASK[color][castle][ROOK].to90() xor CASTLE_MASK[color][castle][KING].to90()
            figures[ALL][ROTATED45] = figures[ALL][ROTATED45] xor CASTLE_MASK[color][castle][ROOK].to45() xor CASTLE_MASK[color][castle][KING].to45()
            figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] xor CASTLE_MASK[color][castle][ROOK].toMinus45() xor CASTLE_MASK[color][castle][KING].toMinus45()
            figures[ALL][allColor] = figures[ALL][allColor] xor CASTLE_MASK[color][castle][ROOK] xor CASTLE_MASK[color][castle][KING]
            return
        }
        //</editor-fold>

        figures[color][type] = figures[color][type] setBit from
        if (promotion == 0)
            figures[color][type] = figures[color][type] zeroBit to
        else
            figures[color][promotion] = figures[color][promotion] zeroBit to

        figures[ALL][DEFAULT] = figures[ALL][DEFAULT] setBit from
        figures[ALL][ROTATED90] = figures[ALL][ROTATED90] setBit ROTATE_90[from]
        figures[ALL][ROTATED45] = figures[ALL][ROTATED45] setBit ROTATE_45[from]
        figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] setBit ROTATE_MINUS45[from]
        figures[ALL][allColor] = figures[ALL][allColor] setBit from

        figures[ALL][allColor] = figures[ALL][allColor] zeroBit to

        val enemyColor = 1 - color
        if (victim != NONE) {
            figures[enemyColor][victim] = figures[enemyColor][victim] setBit to
            figures[ALL][ALL_WHITES + enemyColor] = figures[ALL][ALL_WHITES + enemyColor] setBit to
        } else {
            figures[ALL][DEFAULT] = figures[ALL][DEFAULT] zeroBit to
            figures[ALL][ROTATED90] = figures[ALL][ROTATED90] zeroBit ROTATE_90[to]
            figures[ALL][ROTATED45] = figures[ALL][ROTATED45] zeroBit ROTATE_45[to]
            figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] zeroBit ROTATE_MINUS45[to]
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
        val move = createMove(from, to, 0, 0, 0)
        for (m in getMoves(color, false)) {
            if (move.getFrom() == m.getFrom() && move.getTo() == m.getTo())
                return true
        }
        return false
    }

    fun isCheckMate(to: Int): Boolean {
        for (move in getMoves(to, false)) {
            var eaten = false
            makeMove(move, to)
            for (m in getMoves(1 - to, true))
                if (m.getVictim() == KING) {
                    eaten = true
                    break
                }
            unmakeMove(move, to)
            if (!eaten)
                return false
        }
        return true
    }

    fun isCheckTo(color: Int) = getMoves(1 - color, true).any { it.getVictim() == KING }

    fun setNullMoveZKey() {
        hash = hash xor nullMoveZKey
    }

    fun getHashingMove() = search.hashingMoves[hash]


}

