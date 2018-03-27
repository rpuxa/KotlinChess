package chess

data class Move(var from: Int, var to: Int, var type: Int, var killed: Int, var promotion: Int) {

    override fun equals(other: Any?): Boolean {
        if (other !is Move)
            return false
        return other.from == from && other.to == to
    }
}