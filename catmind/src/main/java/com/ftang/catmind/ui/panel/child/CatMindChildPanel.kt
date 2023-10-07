package com.ftang.catmind.ui.panel.child

import android.content.Context
import android.view.View
import androidx.annotation.MainThread

@MainThread
interface CatMindChildPanel {

    val priority: Int

    val title: CharSequence
    fun onCreateView(context: Context): View
    fun onDestroyView()
}