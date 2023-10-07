package com.ftang.catmind.plugin.parser.view

import android.widget.FrameLayout
import com.ftang.catmind.extension.Output

open class FrameLayoutPropertiesParser(view: FrameLayout) : ViewGroupPropertiesParser<FrameLayout>(view) {

    override fun parse(@Output props: MutableMap<String, Any?>) {
        super.parse(props)

        if (view.measureAllChildren) {
            props["measureAllChildren"] = view.measureAllChildren
        }
    }
}