package org.bonitasoft.connectors.model

import com.slack.api.model.block.LayoutBlock

/**
 *  This class wraps a List of LayoutBlock. <br>
 *  It allows to bypass the impossibility to have a typed list as connector input.
 */
class SlackConnectorBlocks(vararg blocks: LayoutBlock) {

    private val blocks : ArrayDeque<LayoutBlock> = ArrayDeque()

    init {
        this.blocks.addAll(blocks)
    }

    fun getBlocks() : MutableList<LayoutBlock> {
        return blocks
    }

    fun addBlockAtTheEnd(block : LayoutBlock) {
        blocks.addLast(block)
    }

    fun addBlockAtTheBeginning(block: LayoutBlock) {
        blocks.addFirst(block)
    }
}