package ir.symbol

interface IRSymbol{
    val owner: IRSymbol
    val isBound: Boolean
}