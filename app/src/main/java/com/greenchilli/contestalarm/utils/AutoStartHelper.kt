package com.greenchilli.contestalarm.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

object AutoStartHelper {

    fun requestAutoStartPermission(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        try {
            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            val componentName = when {
                manufacturer.contains("xiaomi") -> ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                manufacturer.contains("oppo") -> ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
                manufacturer.contains("vivo") -> ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
                manufacturer.contains("letv") -> ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
                manufacturer.contains("honor") -> ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
                manufacturer.contains("huawei") -> ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
                manufacturer.contains("oneplus") -> ComponentName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                )
                manufacturer.contains("asus") -> ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.autostart.AutoStartActivity"
                )
                manufacturer.contains("samsung") -> ComponentName(
                    "com.samsung.android.sm_cn",
                    "com.samsung.android.sm.ui.ram.AutoRunActivity"
                )
                manufacturer.contains("tecno") || manufacturer.contains("infinix") || manufacturer.contains("itel") -> ComponentName(
                    "com.transsion.phonemanager",
                    "com.itel.autobootmanager.activity.AutoBootMgrActivity"
                )
                else -> null
            }

            if (componentName != null) {
                intent.component = componentName
                val list = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                if (list.isNotEmpty()) {
                    android.widget.Toast.makeText(
                        context,
                        "Please allow 'Auto Start' or 'Background Execution' for Contest Alarm to ring on time.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    context.startActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
