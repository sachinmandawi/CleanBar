package com.cleanbar.hider.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cleanbar.hider.service.KeepAliveService
import com.cleanbar.hider.shizuku.ImmersiveController
import com.cleanbar.hider.shizuku.ShizukuManager
import com.cleanbar.hider.util.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == "android.intent.action.QUICKBOOT_POWERON") {
            val isPersistent = PreferencesHelper.isPersistentModeEnabled(context)
            val isTargetHidden = PreferencesHelper.isTargetHidden(context)

            if (isPersistent && isTargetHidden) {
                if (ShizukuManager.hasPermission()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            ImmersiveController.hideStatusBarGlobally()
                        } catch (e: Throwable) {
                            e.printStackTrace()
                        }
                    }
                }
                KeepAliveService.start(context)
            }
        }
    }
}
