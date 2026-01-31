package com.dailycheckin.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

object BatteryOptimizationHelper {
    
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }
    
    fun requestIgnoreBatteryOptimization(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
        }
    }
    
    fun openBatteryOptimizationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        context.startActivity(intent)
    }
    
    fun openAutoStartSettings(context: Context) {
        // 不同厂商的自启动设置页面
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        val intent = when {
            manufacturer.contains("xiaomi") -> {
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
            }
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                }
            }
            manufacturer.contains("oppo") -> {
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                }
            }
            manufacturer.contains("vivo") -> {
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                }
            }
            manufacturer.contains("samsung") -> {
                Intent().apply {
                    component = android.content.ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                    )
                }
            }
            else -> {
                Intent(Settings.ACTION_SETTINGS)
            }
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // 如果特定厂商的页面打不开，打开通用设置
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }
    
    fun getManufacturerTip(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") -> "小米用户需要开启\"自启动\"和\"后台弹出界面\"权限"
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> "华为/荣耀用户需要关闭\"电池优化\"并允许\"后台活动\""
            manufacturer.contains("oppo") -> "OPPO用户需要开启\"自启动\"和\"后台运行\"权限"
            manufacturer.contains("vivo") -> "vivo用户需要开启\"自启动\"和\"后台高耗电\"权限"
            manufacturer.contains("samsung") -> "三星用户需要关闭\"电池优化\"和\"让未使用的应用进入休眠\""
            else -> "请关闭电池优化并允许应用自启动"
        }
    }
}
