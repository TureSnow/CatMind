package com.ftang.catmind.plugin

import android.view.View
import com.ftang.catmind.CatMind
import com.ftang.catmind.plugin.impl.ViewPropertiesPluginDefaultImpl
import com.ftang.catmind.plugin.parser.PropertiesParser

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