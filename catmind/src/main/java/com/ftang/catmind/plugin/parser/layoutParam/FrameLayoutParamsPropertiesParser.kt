package com.ftang.catmind.plugin.parser.layoutParam

import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY
import com.ftang.catmind.extension.*

/**
 * @author YvesCheung
 * 2021/1/4
 */
open class FrameLayoutParamsPropertiesParser<P : FrameLayout.LayoutParams>(lp: P) :
    MarginLayoutParamsPropertiesParser<P>(lp) {

    override fun parse(@Output props: MutableMap<String, Any?>) {
        super.parse(props)

        if (lp.gravity != UNSPECIFIED_GRAVITY) {
            props["layout_gravity"] = gravityToString(lp.gravity)
        }
    }
}