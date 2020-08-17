package bc

sealed class Constant<T>(open val value: T){
    data class ConstInt(override val value: Int): Constant<Int>(value)
}

data class Store(
    val constants: ArrayList<Constant<*>> = ArrayList(),
    val names: ArrayList<String> = ArrayList()
){
    operator fun plus(name: String) =
        names.contains(name) || names.add(name)

    operator fun plus(constant: Constant<*>) =
        constants.contains(constant) || constants.add(constant)
}