package com.ftang.catmind.model

import android.view.View
import com.ftang.catmind.plugin.ViewPropertiesPlugin

class ViewProperties(
    view: View,
    private val actual: LinkedHashMap<String, Any?> = LinkedHashMap()
) : Map<String, Any?> by actual {

    init {
        ViewPropertiesPlugin.of(view)?.parse(actual)
        //todo:初始化layoutParam
//        val lp = view.layoutParams
//        if (lp != null) {
//            LayoutParamsPropertiesPlugin.of(view, lp)?.parse(actual)
//        }
    }
}