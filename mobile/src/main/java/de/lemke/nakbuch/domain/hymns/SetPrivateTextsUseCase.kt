package de.lemke.nakbuch.domain.hymns

import android.app.Activity
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import de.lemke.nakbuch.App
import de.lemke.nakbuch.data.hymnsRepo
import de.lemke.nakbuch.domain.model.BuchMode
import de.lemke.nakbuch.ui.MainActivity
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SetPrivateTextsUseCase {
    operator fun invoke(mActivity: Activity, result: List<Uri>?): String {
        when {
            result == null -> return "Fehler: Keine passende Datei erkannt"
            result.isEmpty() -> return "Kein Inhalt ausgewählt"
            else -> {
                Log.d("Ausgewählte Inhalte", result.toString())
                val ok = StringBuilder()
                for (uri in result) {
                    var fileName: String? = null
                    if (uri.scheme == "content") {
                        App.myRepository.contentResolver.query(uri, null, null, null, null)
                            .use { cursor ->
                                if (cursor != null && cursor.moveToFirst()) {
                                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                                }
                            }
                    }
                    if (fileName == null) {
                        fileName = uri.path
                        val cut = fileName!!.lastIndexOf('/')
                        if (cut != -1) fileName = fileName?.substring(cut + 1)
                    }
                    if (fileName == "hymnsGesangbuch.txt") {
                        if (hymnsRepo.setPrivateTexts(uri, BuchMode.Gesangbuch)) {
                            ok.append(" ${BuchMode.Gesangbuch}")
                            sendToWear(mActivity, uri, "/privateTextGesangbuch")
                        }
                    } else if (fileName == "hymnsChorbuch.txt") {
                        if (hymnsRepo.setPrivateTexts(uri, BuchMode.Chorbuch)) {
                            ok.append(" ${BuchMode.Chorbuch}")
                            sendToWear(mActivity, uri, "/privateTextChorbuch")
                        }
                    }
                }
                return if (ok.toString().isEmpty())
                    "Fehler: Keine passende Datei erkannt"
                else {
                    MainActivity.colorSettingChanged = true
                    "$ok aktualisiert"
                }
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun sendToWear(mActivity: Activity, uri: Uri, path: String) {
        val fis: InputStream?
        val ois: ObjectInputStream
        val result: ArrayList<HashMap<String, String>>
        try {
            fis = App.myRepository.contentResolver.openInputStream(uri)
            ois = ObjectInputStream(fis)
            result = ois.readObject() as ArrayList<HashMap<String, String>>
            ByteArrayOutputStream().use { bos ->
                val out = ObjectOutputStream(bos)
                out.writeObject(result)
                out.flush()
                SendThread(mActivity, path, bos.toByteArray()).start()
            }
            ois.close()
            fis?.close()
        } catch (e: Exception) {
            Log.e("Send to Wear", e.toString())
        }
    }

    internal inner class SendThread(private var mActivity: Activity, private var path: String, private var message: ByteArray) : Thread() {
        override fun run() { //Retrieve the connected devices, known as nodes
            val wearableList = Wearable.getNodeClient(mActivity.applicationContext).connectedNodes
            try {
                val nodes = Tasks.await(wearableList)
                for (node in nodes) {
                    val sendMessageTask = Wearable.getMessageClient(mActivity).sendMessage(node.id, path, message)
                    Tasks.await(sendMessageTask)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}