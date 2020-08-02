package passes

class TemporaryNameCreator{
    private var counter = 0
    val name get() = "${counter++}"
}