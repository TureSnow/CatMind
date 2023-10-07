package com.ftang.catmind.plugin.panel

import com.ftang.catmind.plugin.CatMindPlugin
import com.ftang.catmind.ui.panel.child.CatMindChildPanel

interface CatMindChildPanelPlugin : CatMindPlugin {
    /**
     * To initialize your own [CatMindChildPanel].
     * Load them into the framework.
     */
    fun createPanels(): Set<CatMindChildPanel>
}