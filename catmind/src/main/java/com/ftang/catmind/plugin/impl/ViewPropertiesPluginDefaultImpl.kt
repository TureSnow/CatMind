package com.ftang.catmind.plugin.impl

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ftang.catmind.plugin.parser.PropertiesParser
import com.ftang.catmind.plugin.ViewPropertiesPlugin
import com.ftang.catmind.plugin.parser.FrameLayoutPropertiesParser
import com.ftang.catmind.plugin.parser.ImageViewPropertiesParser
import com.ftang.catmind.plugin.parser.LinearLayoutPropertiesParser
import com.ftang.catmind.plugin.parser.RecyclerViewPropertiesParser
import com.ftang.catmind.plugin.parser.RelativeLayoutPropertiesParser
import com.ftang.catmind.plugin.parser.TextViewPropertiesParser
import com.ftang.catmind.plugin.parser.ViewGroupPropertiesParser
import com.ftang.catmind.plugin.parser.ViewPropertiesParser

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