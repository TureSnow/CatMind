package com.ftang.catmind.service

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ftang.catmind.CatMind
import com.ftang.catmind.Factory.CatMindLayoutParamsFactory
import com.ftang.catmind.R
import com.ftang.catmind.ui.CatMindMask
import java.util.concurrent.atomic.AtomicBoolean


class CatMindWindowService : Service() {
    private val TAG: String = "CatMind"
    private lateinit var windowManager: WindowManager

    private val catFloatWindowLayoutParams: LayoutParams by lazy {
        CatMindLayoutParamsFactory.create(
            CatMindLayoutParamsFactory.FLOAT_TYPE
        )
    }
    private lateinit var catFloatWindow: View
    private var floatVisible = false

    private val catBottomWindowLayoutParams: LayoutParams by lazy {
        CatMindLayoutParamsFactory.create(
            CatMindLayoutParamsFactory.BOTTOM_TYPE
        )
    }
    private lateinit var catBottomWindow: View
    private var bottomVisible = false

    private val catMaskWindowLayoutParams: LayoutParams by lazy {
        CatMindLayoutParamsFactory.create(
            CatMindLayoutParamsFactory.MASK_TYPE
        )
    }
    private var mask:CatMindMask? = null
    private var maskAdded = AtomicBoolean(false)

    companion object {
        // 使用静态变量存储activity和fragment的意义在于这个service总是落后于MainActivity启动，
        // 如果在Service中存储，那么MainActivity的消息总是会丢失
        private var activityClassName = ""
        private var fragmentClassName = ""

        // 为了在bottomWindow显示的过程中实时更新Activity和Fragment，
        // 需要采用一种通信方式通知CatMindWindowService
        // 由CatMindWindowService更新消息
        // 这里采用了广播的方式
        val ACTION_LISTEN_TO_CAT_MIND =
            "com.ftang.catmind.ACTION_LISTEN_TO_CATMIND"

        val BROAD_CAST_TYPE_DEFAULT = 0
        val BROAD_CAST_TYPE_LONG_PRESS = 1
        val BROAD_CAST_TYPE_TARGET_VIEW_CHANGE = 2

        fun sendActivityAndFragment(
            context: Context,
            activityName: String?,
            fragmentName: String?,
        ) {
            activityClassName = activityName?:""
            fragmentClassName = fragmentName?:""
            val intent = Intent(ACTION_LISTEN_TO_CAT_MIND)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        fun notifyFragmentDestroyed(
            context: Context,
            activityName: String?,
            fragmentName: String?
        ) {
            if (activityName == activityClassName && fragmentName == fragmentClassName ) {
                fragmentClassName = ""
            }
            val intent = Intent(ACTION_LISTEN_TO_CAT_MIND)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        fun notifyTargetViewChanged(context: Context) {
            val intent = Intent(ACTION_LISTEN_TO_CAT_MIND).apply {
                putExtra("type", BROAD_CAST_TYPE_TARGET_VIEW_CHANGE)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    private val catMindMessageReceiver:BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val type = intent.getIntExtra("type", BROAD_CAST_TYPE_DEFAULT)
            when(type) {
                BROAD_CAST_TYPE_LONG_PRESS -> {
                    dismissMask()
                    showFloat()
                }
                BROAD_CAST_TYPE_TARGET_VIEW_CHANGE -> {
                    mask?.updateTargetViews(CatMind.targetViewReference?.get())
                }
                BROAD_CAST_TYPE_DEFAULT -> {
                    if (bottomVisible) {
                        catBottomWindow.findViewById<TextView>(R.id.activity_name).text = activityClassName
                        catBottomWindow.findViewById<TextView>(R.id.fragment_name).text = fragmentClassName
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        //初始化WindowManager对象
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        initCatMindFloatWindow()
        initCatMindBottomWindow()
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(catMindMessageReceiver, IntentFilter(ACTION_LISTEN_TO_CAT_MIND))
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initCatMindFloatWindow() {
        //初始化猫猫头布局
        catFloatWindow = LayoutInflater.from(this).inflate(R.layout.cat_mind_float_layout, null)
        catFloatWindow.clipToOutline = true
        //添加猫猫头悬浮窗
        showFloat()
        //设置猫猫头的监听对象:用于移动浮动窗口
        catFloatWindow.setOnTouchListener(object : View.OnTouchListener {
            //悬浮窗相对于屏幕左上角的坐标，不可以直接当作LayoutParams的x、y的值，需要进行转换
            private var mFloatRawX = 0
            private var mFloatRawY = 0
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                when (motionEvent?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mFloatRawX = motionEvent.rawX.toInt()
                        mFloatRawY = motionEvent.rawY.toInt()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val nowX = motionEvent.rawX.toInt()
                        val nowY = motionEvent.rawY.toInt()
                        val movedX = nowX - mFloatRawX
                        val movedY = nowY - mFloatRawY
                        mFloatRawX = nowX
                        mFloatRawY = nowY
                        catFloatWindowLayoutParams.apply {
                            x += movedX
                            y += movedY
                            CatMind.floatX = x
                            CatMind.floatY = y
                        }
                        windowManager.updateViewLayout(view, catFloatWindowLayoutParams)
                    }
                }
                return false
            }
        })
        //设置双击监听：用于展示bottom窗口
        catFloatWindow.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime:Long = 0L
            private val CLICK_INTERVAL:Long = 300
            override fun onClick(view: View?) {
                val now = System.currentTimeMillis()
                if (now - lastClickTime < CLICK_INTERVAL) {
                    if (bottomVisible) {
                        windowManager.removeView(catBottomWindow)
                    } else {
                        refreshCatBottomWindow()
                        windowManager.addView(catBottomWindow, catBottomWindowLayoutParams)
                    }
                    bottomVisible = !bottomVisible
                }
                lastClickTime = now
            }
        })
    }

    private fun initCatMindBottomWindow() {
        catBottomWindow = LayoutInflater.from(this).inflate(R.layout.cat_mind_bottom_layout, null)
        catBottomWindow.setOnTouchListener(object : View.OnTouchListener {
            //悬浮窗相对于屏幕左上角的坐标，不可以直接当作LayoutParams的x、y的值，需要进行转换
            private var onBottom = true
            private var mBottomRawY = 0
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                when (motionEvent?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mBottomRawY = motionEvent.rawY.toInt()
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val nowY = motionEvent.rawY.toInt()
                        onBottom = nowY - mBottomRawY >= 0
                        mBottomRawY = nowY
                    }

                    MotionEvent.ACTION_UP -> {
                        catBottomWindowLayoutParams.gravity = if (onBottom) {
                            Gravity.BOTTOM
                        } else {
                            Gravity.TOP
                        }
                        windowManager.updateViewLayout(view, catBottomWindowLayoutParams)
                    }
                }
                return false
            }
        })
        catBottomWindow.findViewById<View>(R.id.cat_mind_bottom_dismiss).apply {
            setOnClickListener {
                dismissBottom()
            }
        }
        catBottomWindow.findViewById<TextView>(R.id.click_intercept).apply {
            setOnClickListener{
                if (!maskAdded.get()){
                    CatMind.activityReference?.get()?.let { showMask(it) }
                }
            }
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

    private fun showMask(activity: Activity) {
        if (maskAdded.compareAndSet(false, true)) {
            windowManager.addView(
                CatMindMask(activity).apply {
                    mask = this
                },
                catMaskWindowLayoutParams
            )
            dismissFloat()
            dismissBottom()
        }
    }

    private fun dismissMask() {
        if (maskAdded.compareAndSet(true, false)) {
            mask?.let {
                windowManager.removeView(mask)
                mask = null
            }
        }
    }

    private fun dismissBottom() {
        if (bottomVisible) {
            windowManager.removeView(catBottomWindow)
            bottomVisible = !bottomVisible
        }
    }

    private fun showFloat() {
        if (!floatVisible) {
            windowManager.addView(catFloatWindow, catFloatWindowLayoutParams)
            floatVisible = !floatVisible
        }
    }

    private fun dismissFloat() {
        if (floatVisible) {
            windowManager.removeView(catFloatWindow)
            floatVisible = !floatVisible
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("cat_mind_service", "CatMind")
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            ""
        }
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(101, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissFloat()
        dismissBottom()
        dismissMask()
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(catMindMessageReceiver)
    }
}