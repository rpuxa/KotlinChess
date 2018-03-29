package chess.abstracts

interface AbstractPosition {

    fun getSortMoves(color: Int, onlyCaptures: Boolean): Array<Int>

    fun getMoves(color: Int, onlyCaptures: Boolean): Array<Int>

    fun makeMove(from: Byte, to: Byte)

    fun makeMove(move: Int, color: Int)

    fun unmakeMove(move: Int, color: Int)

    fun result(): Int
}