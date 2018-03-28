package chess

data class Move(var from: Int, var to: Int, var type: Int, var victim: Int, var promotion: Int) {

    var score = Integer.MIN_VALUE

    override fun equals(other: Any?): Boolean {
        if (other !is Move)
            return false
        return other.from == from && other.to == to
    }
}