package com.ftang.catmind

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.ftang.catmind.extension.getClassNameWithExtension
import com.ftang.catmind.extension.registerPartialActivityLifecycleCallbacks
import com.ftang.catmind.service.CatMindWindowService
import java.lang.ref.WeakReference

object CatMind {
    private const val TAG : String = "CatMind"
    private lateinit var application: Application
    private const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469
    var activityReference: WeakReference<Activity>? = null
    var targetViewReference: WeakReference<View>? =null
    //悬浮窗在父布局中的实际偏移量，可以直接应用在LayoutParams中
    var floatX = 0
    var floatY = 0

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
                    launchService(application, catMindWindowService)
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
                fun onAppToBackground() {
                    application.stopService(catMindWindowService)
                    clearReference()
                }
            }
        )
    }

    private fun bindCatMindOnActivityAndFragment(application: Application) {
        Log.d(TAG, "catMind bindCatMindOnActivityAndFragment")
        application.registerPartialActivityLifecycleCallbacks(
            onActivityCreated = { activity ->
                if (!hasOverLayPermission(application))
                    showPermissionExplanationDialog(application, activity)
            },
            onActivityResumed = { activity ->
                Log.d(TAG, "activity resumed")
                activityReference = WeakReference(activity)
                listenForResumedActivitiesAndFragments(activity)
            },
            onActivityDestroyed = { activity ->
                if (activityReference?.get() != null && activityReference?.get()!! == activity) {
                    activityReference = null
                }
            }
        )
    }

    private fun showPermissionExplanationDialog(application: Application, activity: Activity) {
        val dialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
            .setTitle("CatMind")
            .setMessage("需要授权权限：允许显示在应用上层")
            .setPositiveButton("授予权限") { _, _ ->
                requestPermissions(application, activity)
            }
            .setNegativeButton("取消") { _, _ ->
                Toast.makeText(application, "授权失败", Toast.LENGTH_SHORT).show()
            }.create()
        dialog.show()
    }
    
    private fun launchService(
        application: Application,
        catMindWindowService: Intent
    ) {
        val hasOverLayPermission = hasOverLayPermission(application)
        if (hasOverLayPermission) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ContextCompat.startForegroundService(application, catMindWindowService)
            else
                application.startService(catMindWindowService)
        } else {
            Log.d(TAG, "launchService fail, no permission")
        }
    }


    private fun hasOverLayPermission(application: Application) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(application)
        } else {
            true
        }

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

    private fun sendActivityAndFragmentDetails2CatMind(
        activityClass: Class<out Any>,
        fragmentClass: Class<out Any>?) {
        CatMindWindowService.sendActivityAndFragment(
            application,
            activityClass.getClassNameWithExtension(),
            fragmentClass?.getClassNameWithExtension()
        )
    }

    private fun notifyCatMindFragmentDestroyed(
        activityClass: Class<out Any>,
        fragmentClass: Class<out Any>?) {
        CatMindWindowService.notifyFragmentDestroyed(
            application,
            activityClass.getClassNameWithExtension(),
            fragmentClass?.getClassNameWithExtension()
        )
    }

    fun updateTargetView(view: View?) {
        view?.let {
            targetViewReference = WeakReference(view)
            CatMindWindowService.notifyTargetViewChanged(application)
        }
    }

    private fun clearReference() {
        activityReference = null
        targetViewReference = null
    }
}