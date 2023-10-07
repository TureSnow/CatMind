package com.ftang.catmind.ui.panel

import android.view.View
import androidx.annotation.MainThread

@MainThread
interface CatMindPopupPanelContainer {
    fun show(anchorView: View)
    fun dismiss()
}