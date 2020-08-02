package passes.cfg

import guru.nidi.graphviz.attribute.*
import guru.nidi.graphviz.attribute.Attributes.attrs
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.MutableNode
import ir.IRStatement
import ir.declarations.IRStatementContainer
import java.io.File

data class BasicBlock(
    val id: String,
    val statements: ArrayList<IRStatement>,
    val successors: ArrayList<BasicBlock>,
    val predecessors: ArrayList<BasicBlock>,
    val node: MutableNode
)

class CFG{
    val startNode = BasicBlock("start", arrayListOf(), arrayListOf(), arrayListOf(), mutNode("start"))
    val endNode = BasicBlock("end", arrayListOf(), arrayListOf(), arrayListOf(), mutNode("end"))
    var mostRecentBlock = startNode

    private val graph = Factory.mutGraph("cfg")

    init{
        startNode.successors.add(endNode)
        endNode.predecessors.add(endNode)

        graph.graphAttrs().add(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM))
        graph.add(mutNode("start").apply {
            this.attrs().add(Shape.BOX)
            addLink(endNode.node)
        })
        graph.add(mutNode("end").apply {
            this.attrs().add(Shape.BOX)
            addLink(startNode.node)
        })
    }

    fun addBasicBlock(name: String, statements: ArrayList<IRStatement>){
        val block = BasicBlock(name, statements, arrayListOf(), arrayListOf(mostRecentBlock), mutNode(name))
        mostRecentBlock.successors.add(block)
        graph.add(block.node.apply {
            attrs().add(Color.WHITE, Shape.BOX)
            addLink(mostRecentBlock.id)
            statements.forEach {
                attrs().add(Label.of(it.toString()))
            }
        })
        mostRecentBlock = block
    }

    fun toGraph() =
        Graphviz
            .fromGraph(graph)
            .width(1080)
            .height(720)
            .render(Format.PNG)
            .toFile(
                File("cfg/cfg.png")
            )
}