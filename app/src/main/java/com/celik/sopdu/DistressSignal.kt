package com.celik.sopdu

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

internal suspend fun runDistressSignal(context: Context, active: Boolean) {
    if (!active) {
        setTorch(context, false)
        return
    }
    val tone = ToneGenerator(AudioManager.STREAM_ALARM, 100)
    try {
        while (true) {
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 450)
            setTorch(context, true)
            delay(450)
            setTorch(context, false)
            delay(300)
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 450)
            setTorch(context, true)
            delay(450)
            setTorch(context, false)
            delay(900)
        }
    } finally {
        tone.release()
        setTorch(context, false)
    }
}

internal fun setTorch(context: Context, enabled: Boolean) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
    try {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return
        manager.setTorchMode(cameraId, enabled)
    } catch (_: Throwable) {
    }
}
