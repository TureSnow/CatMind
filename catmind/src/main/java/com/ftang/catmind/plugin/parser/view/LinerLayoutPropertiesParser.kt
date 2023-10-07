package com.ftang.catmind.plugin.parser.view

import android.os.Build
import android.view.Gravity
import android.widget.LinearLayout
import com.ftang.catmind.extension.Output
import com.ftang.catmind.extension.gravityToString

open class LinearLayoutPropertiesParser(view: LinearLayout) : ViewGroupPropertiesParser<LinearLayout>(view) {

    override fun parse(@Output props: MutableMap<String, Any?>) {
        super.parse(props)

        props["orientation"] =
            if (view.orientation == LinearLayout.HORIZONTAL) "HORIZONTAL" else "VERTICAL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (view.gravity != Gravity.START or Gravity.TOP) {
                props["gravity"] = gravityToString(view.gravity)
            }
        }

        if (view.weightSum > 0f) {
            props["weightSum"] = view.weightSum
        }

        if (view.isBaselineAligned) {
            props["isBaselineAligned"] = view.isBaselineAligned
        }
    }
}