package chess.abstracts

import chess.Move

interface AbstractSearch {
    fun search(position: AbstractPosition, colorMove: Int, depth: Int): Move
}