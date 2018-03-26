package chess.abstractclasses

interface AbstractEvaluate {
    fun evaluate(position: AbstractPosition): Int
}