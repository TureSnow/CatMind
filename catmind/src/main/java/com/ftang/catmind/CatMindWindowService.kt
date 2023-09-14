package com.ftang.catmind

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.TextView
import androidx.annotation.RequiresApi


class CatMindWindowService : Service() {
    private val TAG: String = "CatMind"
    private lateinit var windowManager: WindowManager

    private lateinit var catFloatWindowLayoutParams: LayoutParams
    private lateinit var catFloatWindow: View

    private lateinit var catBottomWindowLayoutParams: LayoutParams
    private lateinit var catBottomWindow: View
    private var bottomVisible = false

    companion object {
        private var activityClassName = ""
        private var fragmentClassName = ""

        fun sendActivityAndFragment(
            activityName: String?,
            fragmentName: String?,
        ) {
            activityClassName = activityName?:""
            fragmentClassName = fragmentName?:""
            Log.d("CatMind", "activity name:$activityClassName, fragment name:$fragmentClassName")
        }

        fun notifyFragmentDestroyed(
            activityName: String?,
            fragmentName: String?
        ) {
            if (activityName == activityClassName && fragmentName == fragmentClassName ) {
                fragmentClassName = ""
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "CatMindWindowService onCreated")
        //初始化WindowManager对象
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        initCatMindFloatWindow()
        initCatMindBottomWindow()
    }

    private fun initCatMindFloatWindow() {
        //初始化猫猫头布局
        catFloatWindow = LayoutInflater.from(this).inflate(R.layout.cat_float_window, null)
        //初始化CatLayoutParam
        catFloatWindowLayoutParams = LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            flags = LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
            //位置大小设置
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            // 获取屏幕的宽度和高度
            // 获取屏幕的宽度和高度
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            x = screenWidth - width
            y = 0
        }
        //添加猫猫头
        windowManager.addView(catFloatWindow, catFloatWindowLayoutParams)
        //设置猫猫头的监听对象:用于移动浮动窗口
        catFloatWindow.setOnTouchListener(
            CatMindFloatTouchListener(
                catFloatWindowLayoutParams,
                windowManager
            )
        )
        //设置点击监听：用于展示bottom窗口
        catFloatWindow.setOnClickListener {
            if (bottomVisible) {
                windowManager.removeView(catBottomWindow)
            } else {
                refreshCatBottomWindow()
                windowManager.addView(catBottomWindow, catBottomWindowLayoutParams)
            }
            bottomVisible = !bottomVisible
        }
    }

    private fun refreshCatBottomWindow() {
        catBottomWindow.findViewById<TextView>(R.id.activity_name).apply {
            text = activityClassName.ifEmpty {
                "no activity"
            }
        }
        catBottomWindow.findViewById<TextView>(R.id.fragment_name).apply {
            text = fragmentClassName.ifEmpty {
                "no fragment"
            }
        }
    }

    private fun initCatMindBottomWindow() {
        catBottomWindow = LayoutInflater.from(this).inflate(R.layout.cat_mind_bottom_layout, null)
        catBottomWindowLayoutParams = LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.RGBA_8888
            //位置大小设置
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.WRAP_CONTENT
            gravity = Gravity.BOTTOM
            flags = LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
            x = 0
            y = 0
            windowAnimations = android.R.style.Animation_Dialog
        }
        catBottomWindow.findViewById<View>(R.id.cat_mind_bottom_dismiss).apply {
            setOnClickListener {
                //todo：改为双击
                bottomVisible = !bottomVisible
                windowManager.removeView(catBottomWindow)
            }
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //todo：兼容foreground
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(catFloatWindow)
        if (bottomVisible) {
            windowManager.removeView(catBottomWindow)
        }
    }
}