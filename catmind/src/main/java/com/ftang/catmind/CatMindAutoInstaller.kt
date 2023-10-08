package com.ftang.catmind

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Process

class CatMindAutoInstaller : ContentProvider() {
    override fun onCreate(): Boolean {
        val ctx = requireNotNull(context)
        val processName = getCurrentProcessName(ctx)
        if (processName == null /*unknown?*/ || processName == ctx.packageName /*main process*/) {
            CatMind.initialize(ctx)
        }
        return true
    }

    /**
     * @return 当前进程名
     */
    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun getCurrentProcessName(context: Context): String? {

        fun getCurrentProcessNameByActivityThread(): String? {
            var processName: String? = null
            try {
                val declaredMethod = Class.forName(
                    "android.app.ActivityThread",
                    false,
                    Application::class.java.classLoader
                ).getDeclaredMethod("currentProcessName")
                declaredMethod.isAccessible = true
                processName = declaredMethod.invoke(null) as String
            } catch (e: Throwable) {
            }
            return processName
        }

        fun getCurrentProcessNameByActivityManager(context: Context): String? {
            val pid: Int = Process.myPid()
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (am != null) {
                val runningAppList = am.runningAppProcesses
                if (runningAppList != null) {
                    for (processInfo in runningAppList) {
                        if (processInfo.pid == pid) {
                            return processInfo.processName
                        }
                    }
                }
            }
            return null
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Application.getProcessName()
        } else {
            getCurrentProcessNameByActivityThread()
                ?: getCurrentProcessNameByActivityManager(context)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}