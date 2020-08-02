package passes.cfg

import ir.declarations.ssa.BasicBlock

class DominanceFrontier{
    private val dom = hashMapOf<BasicBlock, BasicBlock>()
    private val dominanceFrontier = hashMapOf<BasicBlock, BasicBlock>()

    init{
        computerDominators()
//        buildTree()
//        calculateDominanceFrontiers()
    }

    private fun computerDominators(){
//        dom.put()
    }
}
