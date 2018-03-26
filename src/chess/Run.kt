package chess

import chess.implementation.BitBoard

fun main(args: Array<String>) {
    movesGen()
    var a = BitBoard.start().getMoves(1, false)
    println()
}

