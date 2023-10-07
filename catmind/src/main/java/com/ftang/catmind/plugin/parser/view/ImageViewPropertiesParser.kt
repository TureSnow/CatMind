package com.ftang.catmind.plugin.parser.view

import android.os.Build
import android.widget.ImageView
import com.ftang.catmind.extension.Output
import com.ftang.catmind.extension.colorToString
import com.ftang.catmind.extension.drawableToString

open class ImageViewPropertiesParser(view: ImageView) : ViewPropertiesParser<ImageView>(view)  {
    override fun parse(@Output props: MutableMap<String, Any?>) {
        super.parse(props)

        props["scaleType"] = view.scaleType

        if (view.drawable != null) {
            props["src"] = drawableToString(view.drawable)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

            if (view.imageAlpha != 255) {
                props["imageAlpha"] = view.imageAlpha
            }

            if (view.cropToPadding) {
                props["cropToPadding"] = view.cropToPadding
            }

            if (view.adjustViewBounds) {
                props["adjustViewBounds"] = view.adjustViewBounds
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val tint = view.imageTintList
            if (tint != null) {
                props["imageTint"] = colorToString(tint)
            }

            if (view.imageTintMode != null) {
                props["imageTintMode"] = view.imageTintMode
            }
        }

        if (view.baseline > 0) {
            props["baseline"] = view.baseline
        }

        if (view.baselineAlignBottom) {
            props["baselineAlignBottom"] = view.baselineAlignBottom
        }
    }
}