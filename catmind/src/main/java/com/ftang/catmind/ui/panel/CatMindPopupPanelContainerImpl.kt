package com.ftang.catmind.ui.panel

import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ftang.catmind.R
import com.ftang.catmind.extension.IntDef
import com.ftang.catmind.extension.dpToPx
import com.ftang.catmind.plugin.panel.CatMindDefaultChildPanelPlugin
import com.ftang.catmind.ui.panel.child.CatMindChildPanel
//import com.ftang.catmind.ui.panel.child.CatMindHierarchyChildPanel
import com.ftang.catmind.ui.panel.child.CatMindPropertiesChildPanel
import kotlinx.android.synthetic.main.cat_mind_popup_container_layout.view.*

class CatMindPopupPanelContainerImpl(val parent: ViewGroup) : CatMindPopupPanelContainer {

    private var popupPanel: CatMindPopupPanel? = null
    override fun show(anchorView: View) {
        dismiss()
        val childrenPanel: List<CatMindChildPanel> =  listOf(
            CatMindPropertiesChildPanel(CatMindDefaultChildPanelPlugin.PROPERTIES_PRIORITY)
        )
        if (childrenPanel.isNotEmpty()) {
            popupPanel = CatMindPopupPanel(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.cat_mind_popup_container_layout, parent),
                childrenPanel
            ).apply {
                val anchorLocation = IntArray(2).apply {
                    anchorView.getLocationOnScreen(this)
                }
                val panelHeight = parent.context.resources
                    .getDimension(R.dimen.cat_mind_container_height)
                if (anchorLocation[1] > panelHeight &&
                    anchorLocation[1] + anchorView.height > parent.measuredHeight - panelHeight
                ) {
                    showAt(Gravity.TOP)
                } else {
                    showAt(Gravity.BOTTOM)
                }
            }
        }
    }

    override fun dismiss() {
        popupPanel?.dismiss()
        popupPanel = null
    }

    private class CatMindPopupPanel(
        val inspectorMask: View,
        children: List<CatMindChildPanel>
    ) {
        private val popupPanel = inspectorMask.popup_panel
        private val viewPager = popupPanel.popup_panel_viewpager

        private val adapter = PanelAdapter(children)
        fun dismiss() {
            if (popupPanel.parent === inspectorMask) {
                adapter.createdPanel.keys.forEach {
                    it.onDestroyView()
                }
                (inspectorMask as ViewGroup).removeView(popupPanel)
            }
        }

        fun showAt(@IntDef(Gravity.TOP, Gravity.BOTTOM) gravity: Int) {
            initView()

            val lp = popupPanel.layoutParams
            if (lp is FrameLayout.LayoutParams) {
                lp.gravity = gravity
            } else if (lp is LinearLayout.LayoutParams) {
                lp.gravity = gravity
            }
            popupPanel.layoutParams = lp
        }

        private fun initView() {
            viewPager.offscreenPageLimit = 3
            viewPager.adapter = adapter
            viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    lastSelectedPanelPosition = position
                }
            })
            viewPager.setCurrentItem(lastSelectedPanelPosition, false)
        }
    }

    private class PanelAdapter(val children: List<CatMindChildPanel>) : PagerAdapter() {

        val createdPanel = mutableMapOf<CatMindChildPanel, View>()

        private var currentPrimary: CatMindChildPanel? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val panel = children[position]
            return createdPanel.getOrPut(panel) {
                val child =
                    try {
                        panel.onCreateView(container.context)
                    } catch (e: Throwable) {
                        TextView(container.context).apply {
                            textSize = 16f
                            setTextColor(
                                ContextCompat.getColor(
                                    container.context,
                                    R.color.cat_mind_error_color
                                )
                            )
                            setPadding(8.dpToPx, 8.dpToPx, 8.dpToPx, 0)
                            isSingleLine = false
                            movementMethod = ScrollingMovementMethod()
                            text = Log.getStackTraceString(e)
                        }
                    }
                container.addView(child)
                child
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            val panel = children[position]
            val view = createdPanel.remove(panel)
            if (view != null) {
                panel.onDestroyView()
                container.removeView(view)
            }
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            val nextPrimary = children[position]
            currentPrimary = nextPrimary
        }

        override fun getPageTitle(position: Int) = children[position].title

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view === obj
        }

        override fun getCount(): Int = children.size
    }

    companion object {

        private var lastSelectedPanelPosition = 0
    }
}