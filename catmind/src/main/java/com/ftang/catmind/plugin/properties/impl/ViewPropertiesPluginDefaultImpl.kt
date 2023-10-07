package com.ftang.catmind.plugin.properties.impl

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ftang.catmind.plugin.parser.view.PropertiesParser
import com.ftang.catmind.plugin.properties.ViewPropertiesPlugin
import com.ftang.catmind.plugin.parser.view.FrameLayoutPropertiesParser
import com.ftang.catmind.plugin.parser.view.ImageViewPropertiesParser
import com.ftang.catmind.plugin.parser.view.LinearLayoutPropertiesParser
import com.ftang.catmind.plugin.parser.view.RecyclerViewPropertiesParser
import com.ftang.catmind.plugin.parser.view.RelativeLayoutPropertiesParser
import com.ftang.catmind.plugin.parser.view.TextViewPropertiesParser
import com.ftang.catmind.plugin.parser.view.ViewGroupPropertiesParser
import com.ftang.catmind.plugin.parser.view.ViewPropertiesParser

class ViewPropertiesPluginDefaultImpl : ViewPropertiesPlugin {
    override fun tryCreate(view: View): PropertiesParser? {
        return when (view) {
            is ImageView -> ImageViewPropertiesParser(view)
            is TextView -> TextViewPropertiesParser(view)
            is RecyclerView -> RecyclerViewPropertiesParser(view)
            is LinearLayout -> LinearLayoutPropertiesParser(view)
            is RelativeLayout -> RelativeLayoutPropertiesParser(view)
            is FrameLayout -> FrameLayoutPropertiesParser(view)
            is ViewGroup -> ViewGroupPropertiesParser(view)
            else -> ViewPropertiesParser(view)
        }
    }

}