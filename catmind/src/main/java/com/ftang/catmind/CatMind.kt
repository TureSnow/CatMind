package com.ftang.catmind

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner

object CatMind {
    private const val TAG : String = "CatMind"
    private lateinit var application: Application
    private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469

    fun initialize(application: Application) {
        Log.d(TAG,"catMind init")
        val catMindWindowServiceIntent = Intent(application, CatMindWindowService::class.java)
        bindCatMindServiceOnAppEvent(application, catMindWindowServiceIntent)
        CatMind.application = application
        bindCatMindOnActivityAndFragment(application)
        launchService(application, catMindWindowServiceIntent)
    }

    private fun bindCatMindServiceOnAppEvent(
        application: Application,
        catMindWindowService: Intent
    ) {
        Log.d(TAG,"catMind bindCatMindServiceOnAppEvent")
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_START)
                fun onAppToForeground() {
                    //todo:兼容foreground
                    launchService(application, catMindWindowService)
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                fun onAppToBackground() {
                    application.stopService(catMindWindowService)
                }
            }
        )
    }

    private fun bindCatMindOnActivityAndFragment(application: Application) {
        Log.d(TAG,"catMind bindCatMindOnActivityAndFragment")
        application.registerPartialActivityLifecycleCallbacks(
            onActivityCreated = { activity ->
                if (!hasOverLayPermission(application))
                    requestPermissions(application, activity)
            },
            onActivityResumed = { activity ->
                Log.d(TAG, "activity resumed")
                listenForResumedActivitiesAndFragments(activity)
            })
    }
    
    private fun launchService(
        application: Application,
        catMindWindowService: Intent
    ) {
        val hasOverLayPermission = hasOverLayPermission(application)
        if (hasOverLayPermission) {
            //todo:兼容Foreground
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                ContextCompat.startForegroundService(application, catMindWindowService)
//            else
                application.startService(catMindWindowService)
        } else {
            Log.d(TAG, "launchService fail, no permission")
        }
    }

    private fun stopService(
        application: Application
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //todo：兼容foreground
        }
    }


    private fun hasOverLayPermission(application: Application) = Settings.canDrawOverlays(application)

    private fun requestPermissions(
        application: Application,
        activity: Activity
    ) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + application.packageName)
        )
        activity.startActivityForResult(
            intent,
            ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE
        )
    }

    private fun listenForResumedActivitiesAndFragments(activity: Activity) {
        //监听Activity
        sendActivityAndFragmentDetails2CatMind(activity.javaClass, null)
        (activity as? FragmentActivity)?.supportFragmentManager?.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                    super.onFragmentResumed(fm, f)
                    sendActivityAndFragmentDetails2CatMind(activity.javaClass, f.javaClass)
                }

                override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                    //这里需要注意fragment的销毁时机，去删除fragment缓存
                    super.onFragmentViewDestroyed(fm, f)
                    notifyCatMindFragmentDestroyed(activity.javaClass, f.javaClass)
                }
            },
            true
        )
    }

    private fun sendActivityAndFragmentDetails2CatMind(activityClass: Class<out Any>, fragmentClass: Class<out Any>?) {
        Log.d(TAG,"send activity and fragment")
        CatMindWindowService.sendActivityAndFragment(
            activityClass.getClassNameWithExtension(),
            fragmentClass?.getClassNameWithExtension()
        )
    }

    private fun notifyCatMindFragmentDestroyed(activityClass: Class<out Any>, fragmentClass: Class<out Any>?) {
        Log.d(TAG, "Destroyed fragment:${fragmentClass?.getClassNameWithExtension()}")
        CatMindWindowService.notifyFragmentDestroyed(
            activityClass.getClassNameWithExtension(),
            fragmentClass?.getClassNameWithExtension()
        )
    }
}