package com.cleanbar.hider.service

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.cleanbar.hider.R
import com.cleanbar.hider.shizuku.ImmersiveController
import com.cleanbar.hider.shizuku.ShizukuManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@RequiresApi(Build.VERSION_CODES.N)
class StatusBarTileService : TileService() {

    private val serviceJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val isProcessing = AtomicBoolean(false)

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        // Guard: prevent double-tap race condition
        if (isProcessing.getAndSet(true)) return

        if (!ShizukuManager.hasPermission()) {
            val tile = qsTile ?: run { isProcessing.set(false); return }
            tile.state = Tile.STATE_UNAVAILABLE
            setTileSubtitle(tile, "Shizuku Required")
            tile.updateTile()
            isProcessing.set(false)
            return
        }

        val tile = qsTile ?: run { isProcessing.set(false); return }
        val isCurrentlyActive = (tile.state == Tile.STATE_ACTIVE)

        scope.launch {
            try {
                if (isCurrentlyActive) {
                    ImmersiveController.resetToDefault()
                    tile.state = Tile.STATE_INACTIVE
                    tile.label = "CleanBar"
                    setTileSubtitle(tile, "Visible")
                } else {
                    ImmersiveController.hideStatusBarGlobally()
                    tile.state = Tile.STATE_ACTIVE
                    tile.label = "CleanBar"
                    setTileSubtitle(tile, "Hidden")
                }
                tile.icon = Icon.createWithResource(this@StatusBarTileService, R.drawable.ic_status_bar_tile)
                tile.updateTile()
            } catch (e: Throwable) {
                e.printStackTrace()
            } finally {
                isProcessing.set(false)
            }
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        if (!ShizukuManager.hasPermission()) {
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "CleanBar"
            setTileSubtitle(tile, "Setup Needed")
            tile.icon = Icon.createWithResource(this, R.drawable.ic_status_bar_tile)
            tile.updateTile()
            return
        }

        scope.launch {
            try {
                val isHidden = ImmersiveController.isCurrentlyHidden()
                tile.state = if (isHidden) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
                tile.label = "CleanBar"
                setTileSubtitle(tile, if (isHidden) "Hidden" else "Visible")
                tile.icon = Icon.createWithResource(this@StatusBarTileService, R.drawable.ic_status_bar_tile)
                tile.updateTile()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun setTileSubtitle(tile: Tile, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                tile.subtitle = text
            } catch (ignored: Throwable) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
