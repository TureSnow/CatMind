package com.ftang.catmind.plugin.properties

import android.view.View
import android.view.ViewGroup
import com.ftang.catmind.plugin.CatMindPlugin
import com.ftang.catmind.plugin.parser.layoutParam.LayoutParamsPropertiesParser
import com.ftang.catmind.plugin.parser.view.PropertiesParser
import com.ftang.catmind.plugin.properties.impl.LayoutParamsPropertiesPluginImpl

interface LayoutParamsPropertiesPlugin : CatMindPlugin {
    fun tryCreate(view: View, lp: ViewGroup.LayoutParams): PropertiesParser?

    companion object {

        private val parserFactory by lazy {
            LayoutParamsPropertiesPluginImpl()
        }

        fun of(view: View, lp: ViewGroup.LayoutParams): PropertiesParser? {

            return parserFactory.tryCreate(view, lp)
        }
    }
}