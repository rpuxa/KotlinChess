package chess.implementation

import chess.*
import chess.abstractclasses.AbstractPosition
import chess.abstractclasses.AbstractSortingMoves
import chess.constants.*
import chess.utils.*

class BitBoard(private val figures: Array<LongArray>, override val sort: AbstractSortingMoves) : AbstractPosition {

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

            return bitBoard
        }

        private fun empty(sort: AbstractSortingMoves): BitBoard = BitBoard(arrayOf(
                LongArray(6),
                LongArray(6),
                LongArray(6)
        ), sort)

    }

    private fun addMove(moves: ArrayList<Move>, from: Int, type: Int, mask0: Long) {
        addMove(moves, from, type, mask0, false)
    }

    private fun addMove(moves: ArrayList<Move>, from: Int, type: Int, mask0: Long, promotion: Boolean) {
        var mask = mask0
        while (mask != 0L) {
            val cell = mask.getLowestBit()
            mask = mask.zeroLowestBit()
            if (promotion) {
                for (figure in ROOK..QUEEN) {
                    moves.add(Move(from, cell, type, figure))
                }
            } else
                moves.add(Move(from, cell, type, 0))
        }
    }

    override fun getMoves(color: Int, onlyCaptures: Boolean): Array<Move> {
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
                            if (!(board checkBit (cell + BOARD_SIZE)) && !onlyCaptures)
                                addMove(moves, cell, type, WHITE_PAWNS_MOVE[cell] and figures[ALL][DEFAULT].inv())
                            addMove(moves, cell, type, WHITE_PAWNS_ATTACK[cell] and enemyFigures, cell >= A8)
                        } else {
                            if (!(board checkBit (cell - BOARD_SIZE)) && !onlyCaptures)
                                addMove(moves, cell, type, BLACK_PAWNS_MOVE[cell] and figures[ALL][DEFAULT].inv())
                            addMove(moves, cell, type, BLACK_PAWNS_ATTACK[cell] and enemyFigures, cell <= H1)
                        }
                    KING -> addMove(moves, cell, type, if (onlyCaptures) KING_ATTACK[cell] and enemyFigures else KING_ATTACK[cell] and ourFiguresReverse)
                    KNIGHT -> addMove(moves, cell, type, if (onlyCaptures) KNIGHT_ATTACK[cell] and enemyFigures else KNIGHT_ATTACK[cell] and ourFiguresReverse)
                    BISHOP -> {
                        val attack = BISHOP_ATTACKS45[cell][((figures[ALL][ROTATED45] ushr SHIFT45[cell]) and 255).toInt()] or BISHOP_ATTACKS_MINUS45[cell][((figures[ALL][ROTATED_MINUS45] ushr SHIFT_MINUS45[cell]) and 255).toInt()]
                        addMove(moves, cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse)
                    }
                    ROOK -> {
                        val attack = ROOK_ATTACKS[cell][((figures[ALL][DEFAULT] ushr SHIFT[cell]) and 255).toInt()] or ROOK_ATTACKS90[cell][((figures[ALL][ROTATED90] ushr SHIFT90[cell]) and 255).toInt()]
                        addMove(moves, cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse)
                    }
                    QUEEN -> {
                        val attack = BISHOP_ATTACKS45[cell][((figures[ALL][ROTATED45] ushr SHIFT45[cell]) and 255).toInt()] or BISHOP_ATTACKS_MINUS45[cell][((figures[ALL][ROTATED_MINUS45] ushr SHIFT_MINUS45[cell]) and 255).toInt()] or ROOK_ATTACKS[cell][((figures[ALL][DEFAULT] ushr SHIFT[cell]) and 255).toInt()] or ROOK_ATTACKS90[cell][((figures[ALL][ROTATED90] ushr SHIFT90[cell]) and 255).toInt()]
                        addMove(moves, cell, type, if (onlyCaptures) attack and enemyFigures else attack and ourFiguresReverse)
                    }
                }
            }
        }
        return moves.toTypedArray()
    }

    override fun makeMove(move: Move, color: Int) {
        figures[color][move.type] = figures[color][move.type] zeroBit move.from
        figures[color][move.type] = figures[color][move.type] setBit move.to
        figures[ALL][DEFAULT] = figures[ALL][DEFAULT] setBit move.to
        figures[ALL][ROTATED90] = figures[ALL][ROTATED90] setBit ROTATE_90[move.to]
        figures[ALL][ROTATED45] = figures[ALL][ROTATED45] setBit ROTATE_45[move.to]
        figures[ALL][ROTATED_MINUS45] = figures[ALL][ROTATED_MINUS45] setBit ROTATE_MINUS45[move.to]
        var enemyColor = 1 - color
       // for ()
    }

    override fun unmakeMove(move: Move) {
    }

    override fun result(): Int {
        return 1
    }

}

