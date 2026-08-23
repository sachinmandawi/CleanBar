package com.cleanbar.hider.shizuku

object ImmersiveController {

    suspend fun hideStatusBarGlobally(): Result<String> {
        // Disable demo mode first
        ShizukuManager.executeCommand("am broadcast -a com.android.systemui.demo -e command exit")
        
        // Hide all status bar icons, clock and battery via StatusBarManager
        ShizukuManager.executeCommand("cmd statusbar send-disable-flag system-icons clock notification-icons")
        
        // Force immersive status bar policy
        return ShizukuManager.executeCommand("settings put global policy_control immersive.status=*")
    }

    suspend fun resetToDefault(): Result<String> {
        // Restore all status bar icons, clock and battery
        ShizukuManager.executeCommand("cmd statusbar send-disable-flag none")
        
        // Remove immersive policy
        ShizukuManager.executeCommand("settings put global policy_control null")
        
        // Ensure demo mode is off
        return ShizukuManager.executeCommand("am broadcast -a com.android.systemui.demo -e command exit")
    }

    suspend fun isCurrentlyHidden(): Boolean {
        if (!ShizukuManager.hasPermission()) return false
        return try {
            val res = ShizukuManager.executeCommand("settings get global policy_control")
            val policy = res.getOrDefault("")
            policy.contains("immersive.status") || policy.contains("immersive.full")
        } catch (e: Throwable) {
            false
        }
    }
}
