package com.ftang.catmind.plugin.properties.impl

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.ftang.catmind.plugin.parser.layoutParam.AppBarLayoutParamsPropertiesParser
import com.ftang.catmind.plugin.parser.layoutParam.ConstraintLayoutParamsPropertiesParser
import com.ftang.catmind.plugin.parser.layoutParam.CoordinatorLayoutParamsPropertiesParser
import com.ftang.catmind.plugin.parser.layoutParam.FrameLayoutParamsPropertiesParser
import com.ftang.catmind.plugin.parser.layoutParam.LayoutParamsPropertiesParser
import com.ftang.catmind.plugin.parser.layoutParam.LinearLayoutParamsPropertiesParser
import com.ftang.catmind.plugin.parser.layoutParam.RelativeLayoutParamsPropertiesParser
import com.ftang.catmind.plugin.parser.view.PropertiesParser
import com.ftang.catmind.plugin.properties.LayoutParamsPropertiesPlugin
import com.google.android.material.appbar.AppBarLayout

class LayoutParamsPropertiesPluginImpl : LayoutParamsPropertiesPlugin {
    override fun tryCreate(view: View, lp: ViewGroup.LayoutParams): PropertiesParser? {
        return when (lp) {
            is ConstraintLayout.LayoutParams ->
                ConstraintLayoutParamsPropertiesParser(view, lp)
            is LinearLayout.LayoutParams ->
                LinearLayoutParamsPropertiesParser(lp)
            is FrameLayout.LayoutParams ->
                FrameLayoutParamsPropertiesParser(lp)
            is RelativeLayout.LayoutParams ->
                RelativeLayoutParamsPropertiesParser(view, lp)
            is CoordinatorLayout.LayoutParams ->
                CoordinatorLayoutParamsPropertiesParser(view, lp)
            is AppBarLayout.LayoutParams ->
                AppBarLayoutParamsPropertiesParser(view, lp)
            else -> LayoutParamsPropertiesParser(lp)
        }
    }
}