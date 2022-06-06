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
        for (buchMode in BuchMode.values()) {
            if (messageEvent.path == "/$buchMode") {
                //Broadcast the received Data Layer messages locally//
                messageIntent.putExtra(buchMode.toString(), messageEvent.data)
                LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent)
                return
            }
        }
        super.onMessageReceived(messageEvent)
    }
}
