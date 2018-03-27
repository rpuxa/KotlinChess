package chess.abstracts

interface AbstractEvaluate {
    fun evaluate(position: AbstractPosition): Int
}