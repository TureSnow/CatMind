package com.ftang.catmind.model

import android.view.View
import com.ftang.catmind.plugin.properties.LayoutParamsPropertiesPlugin
import com.ftang.catmind.plugin.properties.ViewPropertiesPlugin

class ViewProperties(
    view: View,
    private val actual: LinkedHashMap<String, Any?> = LinkedHashMap()
) : Map<String, Any?> by actual {
    init {
        ViewPropertiesPlugin.of(view)?.parse(actual)
        val lp = view.layoutParams
        if (lp != null) {
            LayoutParamsPropertiesPlugin.of(view, lp)?.parse(actual)
        }
    }
}