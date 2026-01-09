package com.talsk.amadz.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class ProxyController(
    context: Context,
    lifecycle: Lifecycle
) : DefaultLifecycleObserver{
    private val powerManager =
        context.getSystemService(PowerManager::class.java)
    private val sensorManager =
        context.getSystemService(SensorManager::class.java)

    private val wakeLock =
        powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            "Call::Proximity"
        )
    private val sensor =
        sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val near = event.values[0] < sensor!!.maximumRange
            if (near && !wakeLock.isHeld) wakeLock.acquire()
            if (!near && wakeLock.isHeld) wakeLock.release()
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    init {
        lifecycle.addObserver(this)
    }


    override fun onStart(owner: LifecycleOwner) {
        sensor?.let {
            sensorManager.registerListener(
                listener, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        sensorManager.unregisterListener(listener)
        if (wakeLock.isHeld) wakeLock.release()
    }
}