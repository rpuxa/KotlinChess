package chess.abstracts

interface AbstractSearch {
    fun search(position: AbstractPosition, colorMove: Int, depth: Int): Int
}