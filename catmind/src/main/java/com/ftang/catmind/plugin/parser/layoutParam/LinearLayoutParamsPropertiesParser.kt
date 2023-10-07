package com.ftang.catmind.plugin.parser.layoutParam

import android.widget.LinearLayout
import com.ftang.catmind.extension.*

/**
 * @author YvesCheung
 * 2021/1/4
 */
open class LinearLayoutParamsPropertiesParser<P : LinearLayout.LayoutParams>(lp: P) :
    MarginLayoutParamsPropertiesParser<P>(lp) {

    override fun parse(@Output props: MutableMap<String, Any?>) {
        super.parse(props)

        if (lp.weight != 0f) {
            props["layout_weight"] = lp.weight
        }

        if (lp.gravity != -1) {
            props["layout_gravity"] = gravityToString(lp.gravity)
        }
    }
}