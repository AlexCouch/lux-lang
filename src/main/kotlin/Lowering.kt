import ir.IRElement

interface LoweringPass<T, R> where R: IRElement{
    fun lower(data: T): R
}