package com.example.noirdialer.telecom

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import androidx.core.app.NotificationCompat

class NoirInCallService : InCallService() {
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        ActiveCallHolder.call = call
        ActiveCallHolder.service = this
        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                if (state == Call.STATE_DISCONNECTED) {
                    ActiveCallHolder.call = null
                    getSystemService(NotificationManager::class.java).cancel(4102)
                } else postCallNotification(call, state == Call.STATE_RINGING)
            }
        })
        postCallNotification(call, call.state == Call.STATE_RINGING)
    }

    override fun onCallRemoved(call: Call) {
        if (ActiveCallHolder.call == call) ActiveCallHolder.call = null
        getSystemService(NotificationManager::class.java).cancel(4102)
        super.onCallRemoved(call)
    }

    private fun postCallNotification(call: Call, incoming: Boolean) {
        val channel = "noir_phone_calls"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(
            NotificationChannel(channel, "Phone calls", NotificationManager.IMPORTANCE_HIGH))
        val intent = Intent(this, CallActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(this, 4102, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val number = call.details.handle?.schemeSpecificPart ?: "Phone call"
        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setContentTitle(if (incoming) "Incoming call" else "Ongoing call")
            .setContentText(number).setCategory(Notification.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX).setOngoing(true)
            .setFullScreenIntent(pending, true).setContentIntent(pending).build()
        manager.notify(4102, notification)
    }
}

object ActiveCallHolder {
    @Volatile var call: Call? = null
    @Volatile var service: InCallService? = null
}
