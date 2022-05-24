package de.lemke.nakbuch.domain.utils

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import de.lemke.nakbuch.domain.model.BuchMode

class MessageService : WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {
        val messageIntent = Intent()
        messageIntent.action = Intent.ACTION_SEND
        when (messageEvent.path) {
            "/privateTextGesangbuch" -> {
                //Broadcast the received Data Layer messages locally//
                messageIntent.putExtra(BuchMode.Gesangbuch.toString(), messageEvent.data)
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
            }
            "/privateTextChorbuch" -> {
                messageIntent.putExtra(BuchMode.Chorbuch.toString(), messageEvent.data)
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
            }
            "/privateTextJugendliederbuch" -> {
                messageIntent.putExtra(BuchMode.Jugendliederbuch.toString(), messageEvent.data)
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
            }
            else -> super.onMessageReceived(messageEvent)
        }
    }
}