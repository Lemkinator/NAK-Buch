package de.lemke.nakbuch.domain.utils

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class MessageService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val messageIntent = Intent()
        messageIntent.action = Intent.ACTION_SEND
        when (messageEvent.path) {
            "/privateTextGesangbuch" -> {
                messageIntent.putExtra(
                    "privateTextGesangbuch",
                    messageEvent.data
                ) //Broadcast the received Data Layer messages locally//
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
            }
            "/privateTextChorbuch" -> {
                messageIntent.putExtra(
                    "privateTextChorbuch",
                    messageEvent.data
                ) //Broadcast the received Data Layer messages locally//
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
            }
            else -> {
                super.onMessageReceived(messageEvent)
            }
        }
    }
}