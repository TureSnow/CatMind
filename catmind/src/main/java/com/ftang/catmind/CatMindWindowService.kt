package com.ftang.catmind

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class CatMindWindowService : Service() {
    private val TAG: String = "CatMind"
    private lateinit var windowManager: WindowManager

    private lateinit var catFloatWindowLayoutParams: LayoutParams
    private lateinit var catFloatWindow: View

    private lateinit var catBottomWindowLayoutParams: LayoutParams
    private lateinit var catBottomWindow: View
    private var bottomVisible = false

    companion object {
        // 使用静态变量存储activity和fragment的意义在于这个service总是落后于MainActivity启动，
        // 如果在Service中存储，那么MainActivity的消息总是会丢失
        private var activityClassName = ""
        private var fragmentClassName = ""

        // 为了在bottomWindow显示的过程中实时更新Activity和Fragment，
        // 需要采用一种通信方式通知CatMindWindowService
        // 由CatMindWindowService更新消息
        // 这里采用了广播的方式
        private val ACTION_LISTEN_TO_CAT_MIND =
            "com.ftang.catmind.ACTION_LISTEN_TO_CATMIND"

        fun sendActivityAndFragment(
            context: Context,
            activityName: String?,
            fragmentName: String?,
        ) {
            activityClassName = activityName?:""
            fragmentClassName = fragmentName?:""
            Log.d("CatMind", "activity name:$activityClassName, fragment name:$fragmentClassName")
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
    }

    private val catMindMessageReceiver:BroadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            if (bottomVisible) {
                catBottomWindow.findViewById<TextView>(R.id.activity_name).text = activityClassName
                catBottomWindow.findViewById<TextView>(R.id.fragment_name).text = fragmentClassName
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
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(catMindMessageReceiver, IntentFilter(ACTION_LISTEN_TO_CAT_MIND))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initCatMindFloatWindow() {
        //初始化猫猫头布局
        catFloatWindow = LayoutInflater.from(this).inflate(R.layout.cat_float_window, null)
        //初始化CatFloatLayoutParam
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
            if (CatMind.floatX == 0 && CatMind.floatY == 0) {
                // 获取屏幕的宽度和高度
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val screenWidth = displayMetrics.widthPixels
                val screenHeight= displayMetrics.heightPixels
                x = screenWidth / 2 - width / 2
                y = screenHeight / 2 - height / 2
                CatMind.floatX = x
                CatMind.floatY = y
            } else {
                x = CatMind.floatX
                y = CatMind.floatY
            }
        }
        //添加猫猫头悬浮窗
        windowManager.addView(catFloatWindow, catFloatWindowLayoutParams)
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
                    //触发双击事件
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
                bottomVisible = !bottomVisible
                windowManager.removeView(catBottomWindow)
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
        windowManager.removeView(catFloatWindow)
        if (bottomVisible) {
            windowManager.removeView(catBottomWindow)
        }
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(catMindMessageReceiver)
    }
}