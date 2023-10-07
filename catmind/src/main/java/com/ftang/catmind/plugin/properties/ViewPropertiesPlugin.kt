package com.ftang.catmind.plugin.properties

import android.view.View
import com.ftang.catmind.plugin.CatMindPlugin
import com.ftang.catmind.plugin.parser.view.PropertiesParser
import com.ftang.catmind.plugin.properties.impl.ViewPropertiesPluginDefaultImpl

interface ViewPropertiesPlugin : CatMindPlugin {

    fun tryCreate(view: View): PropertiesParser?

    companion object {

        private val parserFactory by lazy {
            ViewPropertiesPluginDefaultImpl()
        }

        fun of(view: View): PropertiesParser? {
            return parserFactory.tryCreate(view)
        }
    }
}