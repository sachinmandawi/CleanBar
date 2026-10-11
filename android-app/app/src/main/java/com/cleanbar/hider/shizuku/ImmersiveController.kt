package com.cleanbar.hider.shizuku

object ImmersiveController {

    suspend fun hideStatusBarGlobally(): Result<String> {
        // Step 1: Exit any active demo mode
        ShizukuManager.executeCommand("am broadcast -a com.android.systemui.demo -e command exit")

        // Step 2: Disable all status bar icons, clock and notifications
        val iconResult = ShizukuManager.executeCommand(
            "cmd statusbar send-disable-flag system-icons clock notification-icons"
        )
        if (iconResult.isFailure) {
            // Non-fatal — continue even if this partially fails
        }

        // Step 3: Force immersive status bar policy (this is the critical step)
        val policyResult = ShizukuManager.executeCommand(
            "settings put global policy_control immersive.status=*"
        )
        return if (policyResult.isSuccess) {
            Result.success("Status bar hidden")
        } else {
            policyResult
        }
    }

    suspend fun resetToDefault(): Result<String> {
        // Step 1: Re-enable all status bar icons
        ShizukuManager.executeCommand("cmd statusbar send-disable-flag none")

        // Step 2: Remove immersive policy cleanly (delete key + null fallback)
        ShizukuManager.executeCommand("settings delete global policy_control")
        val policyResult = ShizukuManager.executeCommand(
            "settings put global policy_control null"
        )
        if (policyResult.isFailure) {
            // Non-fatal — continue
        }

        // Step 3: Exit demo mode to ensure full restoration
        val demoResult = ShizukuManager.executeCommand(
            "am broadcast -a com.android.systemui.demo -e command exit"
        )

        return if (policyResult.isSuccess || demoResult.isSuccess) {
            Result.success("Status bar restored")
        } else {
            policyResult
        }
    }

    suspend fun isCurrentlyHidden(): Boolean {
        if (!ShizukuManager.hasPermission()) return false
        return try {
            val res = ShizukuManager.executeCommand("settings get global policy_control")
            val policy = res.getOrDefault("")
            // "null" string means no policy set — treat as visible
            val hasPolicy = policy.isNotEmpty() &&
                policy != "null" &&
                (policy.contains("immersive.status") || policy.contains("immersive.full"))

            if (!hasPolicy) return false

            // On aggressive OEM skins (Vivo OriginOS, Xiaomi HyperOS), verify dumpsys statusbar
            // If the lockscreen reset mDisabled1 to 0x0, the status bar is actually visible
            val dumpRes = ShizukuManager.executeCommand("dumpsys statusbar")
            val dump = dumpRes.getOrDefault("")
            if (dump.isNotEmpty()) {
                val match = Regex("""mDisabled1=0x([0-9a-fA-F]+)""").find(dump)
                if (match != null) {
                    val hex = match.groupValues[1]
                    val flagVal = hex.toLongOrNull(16) ?: 0L
                    if (flagVal == 0L) {
                        return false
                    }
                }
            }

            true
        } catch (e: Throwable) {
            false
        }
    }
}
