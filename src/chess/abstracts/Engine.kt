package chess.abstracts

import chess.ROTATE_45
import chess.ROTATE_90
import chess.ROTATE_MINUS45
import chess.constants.*
import chess.implementation.BitBoard
import chess.implementation.SortMoves
import chess.movesGen
import chess.utils.setBit

class Engine(val position: AbstractPosition,
             private val search: AbstractSearch,
             var turn: Int) {
    init {
        movesGen()
    }

    fun getAIMove() = search.search(position, turn, 6)

    fun makeMove(from: Int, to: Int) {
        position.makeMove(from.toByte(), to.toByte())
        turn = 1 - turn
    }

    fun makeMove(move: Int) {
        position.makeMove(move, turn)
        turn = 1 - turn
    }

    companion object {
        fun fromFENtoBitBoard(fen: String): BitBoard {
            val bitBoard = BitBoard.empty(SortMoves())
            var cellRotated = 0
            for (c in fen.toCharArray()) {
                if (c == ' ')
                    break
                if (c != '/') {
                    val color = if (c in 'a'..'z') 1 else 0
                    val figure = when (c.toLowerCase()) {
                        'r' -> ROOK
                        'n' -> KNIGHT
                        'b' -> BISHOP
                        'q' -> QUEEN
                        'k' -> KING
                        'p' -> PAWN
                        else -> '0' - c
                    }
                    if (figure < 0) {
                        cellRotated -= figure
                        continue
                    }
                    val allColor = ALL_WHITES + color
                    val cell = cellRotated xor 0x38

                    bitBoard.figures[ALL][DEFAULT] = bitBoard.figures[ALL][DEFAULT] setBit cell
                    bitBoard.figures[ALL][ROTATED90] = bitBoard.figures[ALL][ROTATED90] setBit ROTATE_90[cell]
                    bitBoard.figures[ALL][ROTATED45] = bitBoard.figures[ALL][ROTATED45] setBit ROTATE_45[cell]
                    bitBoard.figures[ALL][ROTATED_MINUS45] = bitBoard.figures[ALL][ROTATED_MINUS45] setBit ROTATE_MINUS45[cell]
                    bitBoard.figures[ALL][allColor] = bitBoard.figures[ALL][allColor] setBit cell

                    bitBoard.figures[color][figure] = bitBoard.figures[color][figure] setBit cell
                    cellRotated++
                }
            }
            return bitBoard
        }
    }

}