package com.ftang.catmind.plugin.panel

import com.ftang.catmind.ui.panel.child.CatMindChildPanel
import com.ftang.catmind.ui.panel.child.CatMindPropertiesChildPanel

class CatMindDefaultChildPanelPlugin : CatMindChildPanelPlugin {
    override fun createPanels(): Set<CatMindChildPanel> {
        return setOf(
            CatMindPropertiesChildPanel(PROPERTIES_PRIORITY)
//            CatMindHierarchyChildPanel(HIERARCHY_PRIORITY)
        )
    }

    companion object {

        const val PROPERTIES_PRIORITY = 100

        const val HIERARCHY_PRIORITY = 200

    }
}