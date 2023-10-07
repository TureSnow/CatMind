package com.ftang.catmind.plugin.parser.view

import androidx.recyclerview.widget.RecyclerView
import com.ftang.catmind.extension.Output
import com.ftang.catmind.extension.canonicalName
import com.ftang.catmind.extension.simpleName

open class RecyclerViewPropertiesParser(view: RecyclerView) :
    ViewPropertiesParser<RecyclerView>(view) {

    override fun parse(@Output props: MutableMap<String, Any?>) {
        super.parse(props)

        val lm = view.layoutManager
        if (lm != null) {
            props["layoutManager"] = lm.simpleName
        }

        val adapter = view.adapter
        if (adapter != null) {
            props["adapter"] = adapter.canonicalName
        }

        if (view.itemDecorationCount > 0) {
            props["itemDecoration"] =
                (0 until view.itemDecorationCount).joinToString { index ->
                    view.getItemDecorationAt(index).canonicalName
                }
        }
    }
}