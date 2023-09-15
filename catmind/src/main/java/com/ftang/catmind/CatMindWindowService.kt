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
import android.os.Build
import android.os.IBinder
import android.util.Log
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

    private lateinit var catCrossWindowLayoutParams: LayoutParams
    private lateinit var catCrossWindow: View
    private var crossVisible = false

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
        initCatMindCrossWindow()
        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(catMindMessageReceiver, IntentFilter(ACTION_LISTEN_TO_CAT_MIND))
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initCatMindFloatWindow() {
        //初始化猫猫头布局
        catFloatWindow = LayoutInflater.from(this).inflate(R.layout.cat_mind_float_layout, null)
        //初始化CatFloatLayoutParam
        catFloatWindowLayoutParams = CatMindLayoutParamsFactory.createLayoutParams(
            CatMindLayoutParamsFactory.FLOAT_TYPE
        )
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
        catBottomWindowLayoutParams = CatMindLayoutParamsFactory.createLayoutParams(
            CatMindLayoutParamsFactory.BOTTOM_TYPE
        )
        catBottomWindow.findViewById<View>(R.id.cat_mind_bottom_dismiss).apply {
            setOnClickListener {
                bottomVisible = !bottomVisible
                windowManager.removeView(catBottomWindow)
            }
        }
        catBottomWindow.findViewById<TextView>(R.id.show_cross).apply {
            setOnClickListener {
                bottomVisible = !bottomVisible
                windowManager.removeView(catBottomWindow)
                crossVisible = !crossVisible
                windowManager.addView(catCrossWindow, catCrossWindowLayoutParams)
            }
        }
    }
    private fun initCatMindCrossWindow() {
        catCrossWindow = LayoutInflater.from(this).inflate(R.layout.cat_mind_cross_layout, null)
        catCrossWindowLayoutParams = CatMindLayoutParamsFactory.createLayoutParams(
            CatMindLayoutParamsFactory.CROSS_TYPE
        )
        catCrossWindow.setOnTouchListener(object : View.OnTouchListener {
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
                        catCrossWindowLayoutParams.apply {
                            x += movedX
                            y += movedY
                        }
                        windowManager.updateViewLayout(view, catCrossWindowLayoutParams)
                    }
//
                    MotionEvent.ACTION_UP -> {
                        //这里要去渲染view的边界，获取布局信息并显示
                        val location = IntArray(2)
                        catCrossWindow.getLocationOnScreen(location)
                        val size = catCrossWindow.measuredHeight / 2
                        val centerX = location[0] + size
                        val centerY = location[1] + size
                        findViewByPoint(centerX, centerY)
                    }
                }
                return false
            }
        })
        //设置双击监听：用于关闭聚焦圈
        catCrossWindow.setOnClickListener(object : View.OnClickListener {
            private var lastClickTime:Long = 0L
            private val CLICK_INTERVAL:Long = 300
            override fun onClick(view: View?) {
                val now = System.currentTimeMillis()
                if (now - lastClickTime < CLICK_INTERVAL) {
                    //触发双击事件
                    if (crossVisible) {
                        windowManager.removeView(catCrossWindow)
                    }
                    crossVisible = !crossVisible
                }
                lastClickTime = now
            }
        })
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

    private fun findViewByPoint(x: Int, y: Int): View? {
        val activity = CatMind.activityReference?.get() ?: return null
        val decorView = activity.window.decorView
        val decorLocation = IntArray(2);
        decorView.getLocationOnScreen(decorLocation)
        val decorX = x - decorLocation[0]
        val decorY = y - decorLocation[1]
        val foundView = findTargetView(decorView, decorX, decorY)
        if (foundView == null) {
            Log.d(TAG, "foundView is null")
        } else {
            Log.d(TAG, "foundView name: ${foundView.accessibilityClassName}")
        }
        return null
    }

    /**
     * 根据绝对坐标查找view中的元素
     */
    private fun findTargetView(view: View, x: Int, y: Int): View? {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val childView = view.getChildAt(i)
                val location = IntArray(2)
                childView.getLocationOnScreen(location)
                val childX = x - location[0]
                val childY = y - location[1]
                if (childX >= 0 && childY >= 0 && childX <= childView.width && childY <= childView.height) {
                    // 在子 View 中查找
                    val targetView = findTargetView(childView, x, y)
                    if (targetView != null) {
                        return targetView
                    } else {
                        return childView
                    }
                }
            }
        } else {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            val viewX = x - location[0]
            val viewY = y - location[1]
            if (viewX >= 0 && viewY >= 0 && viewX <= view.width && viewY <= view.height) {
                // 找到了对应坐标的 View
                return view
            }
        }
        return null
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
        if (crossVisible) {
            windowManager.removeView(catCrossWindow)
        }
        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(catMindMessageReceiver)
    }
}