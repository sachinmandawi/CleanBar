package com.cleanbar.hider.shizuku

import android.content.Context
import com.cleanbar.hider.util.PreferencesHelper

object ImmersiveController {

    /**
     * Completely hides all status bar elements in 1 click (system icons, clock, notification icons, policy).
     */
    suspend fun hideStatusBar(context: Context): Result<String> {
        if (!ShizukuManager.hasPermission()) {
            return Result.failure(IllegalStateException("Shizuku permission not granted"))
        }

        // Step 1: Exit any active demo mode
        ShizukuManager.executeCommand("am broadcast -a com.android.systemui.demo -e command exit")

        // Step 2: Full Hide - Disable all system icons, clock, and notification icons
        ShizukuManager.executeCommand("cmd statusbar send-disable-flag system-icons clock notification-icons")
        ShizukuManager.executeCommand("settings put global policy_control immersive.status=*")
        
        val allSlots = "battery,clock,time,wifi,mobile,phone_signal,data_connection,vpn,airplane,hotspot,alarm_clock,volume,headset,wireless_headset,bluetooth_handsfree_battery,bluetooth,location,gps,zen,cast,nfc,micphone,call_record,privacy_mode,sync_failing,sync_active,camera,mute,managed_profile"
        ShizukuManager.executeCommand("settings put secure icon_blacklist \"$allSlots\"")
        ShizukuManager.executeCommand("settings put system icon_blacklist \"$allSlots\"")
        ShizukuManager.executeCommand("settings put global icon_blacklist \"$allSlots\"")

        return Result.success("All status bar elements hidden")
    }

    /**
     * 100% Complete Factory Restore: Restores all status bar icons, clock, policies,
     * and deletes blacklist keys so device returns to completely normal state.
     */
    suspend fun resetToDefault(context: Context? = null): Result<String> {
        if (!ShizukuManager.hasPermission()) {
            return Result.failure(IllegalStateException("Shizuku permission not granted"))
        }

        // 1. Cleanly delete policy_control first so WindowManager exits immersive mode
        ShizukuManager.executeCommand("settings put global policy_control \"\"")
        ShizukuManager.executeCommand("settings delete global policy_control")

        // 2. Trigger ContentObservers with empty string first, then delete across all tables
        ShizukuManager.executeCommand("settings put secure icon_blacklist \"\"")
        ShizukuManager.executeCommand("settings put system icon_blacklist \"\"")
        ShizukuManager.executeCommand("settings put global icon_blacklist \"\"")
        ShizukuManager.executeCommand("settings delete secure icon_blacklist")
        ShizukuManager.executeCommand("settings delete system icon_blacklist")
        ShizukuManager.executeCommand("settings delete global icon_blacklist")

        // 3. Clear all disable flags in StatusBarManager (both flag 1 and flag 2)
        ShizukuManager.executeCommand("cmd statusbar send-disable-flag none")
        ShizukuManager.executeCommand("cmd statusbar send-disable-flag-2 none")

        // 4. Exit demo mode
        ShizukuManager.executeCommand("am broadcast -a com.android.systemui.demo -e command exit")

        return Result.success("Status bar restored to default")
    }

    suspend fun isCurrentlyHidden(context: Context): Boolean {
        if (!ShizukuManager.hasPermission()) {
            return PreferencesHelper.isTargetHidden(context)
        }
        return try {
            val res = ShizukuManager.executeCommand("settings get global policy_control")
            val policy = res.getOrDefault("").trim()
            val hasPolicy = policy.isNotEmpty() && policy != "null" && policy.contains("immersive.status")

            val blacklistRes = ShizukuManager.executeCommand("settings get secure icon_blacklist")
            val blacklist = blacklistRes.getOrDefault("").trim()
            // Only consider hidden if it contains core icons that CleanBar hides (clock & battery)
            val hasBlacklist = blacklist.isNotEmpty() && blacklist != "null" &&
                    blacklist.contains("clock") && blacklist.contains("battery")

            if (PreferencesHelper.isTargetHidden(context)) {
                true
            } else {
                hasPolicy || hasBlacklist
            }
        } catch (e: Throwable) {
            PreferencesHelper.isTargetHidden(context)
        }
    }
}
