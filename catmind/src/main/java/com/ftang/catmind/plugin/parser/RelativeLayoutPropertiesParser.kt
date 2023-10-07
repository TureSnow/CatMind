package com.ftang.catmind.plugin.parser

import android.os.Build
import android.view.Gravity
import android.widget.RelativeLayout
import com.ftang.catmind.extension.Output
import com.ftang.catmind.extension.gravityToString

open class RelativeLayoutPropertiesParser(view: RelativeLayout) : ViewGroupPropertiesParser<RelativeLayout>(view) {

    override fun parse(@Output props: MutableMap<String, Any?>) {
        super.parse(props)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (view.gravity != Gravity.START or Gravity.TOP) {
                props["gravity"] = gravityToString(view.gravity)
            }
        }
    }
}