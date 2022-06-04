package de.lemke.nakbuch.domain.hymnUseCases

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.data.HymnsRepository
import de.lemke.nakbuch.domain.model.*
import de.lemke.nakbuch.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import javax.inject.Inject

class SetPrivateTextsUseCase @Inject constructor(
    private val hymnsRepository: HymnsRepository,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(result: List<Uri>?): String {
        return withContext(Dispatchers.Default) {
            when {
                result == null -> return@withContext "Fehler: Keine passende Datei erkannt"
                result.isEmpty() -> return@withContext "Kein Inhalt ausgewählt"
                else -> {
                    val ok = StringBuilder()
                    for (uri in result) {
                        var fileName: String? = null
                        if (uri.scheme == "content") {
                            context.contentResolver.query(uri, null, null, null, null)
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
                        if (fileName?.matches("""hymnsGesangbuch.*\.txt""".toRegex()) == true) {
                            if (setPrivateTexts(uri, BuchMode.Gesangbuch)) {
                                if (ok.isNotEmpty()) ok.append(", ")
                                ok.append(" ${BuchMode.Gesangbuch}")
                                sendToWear(uri, "/privateTextGesangbuch")
                            }
                        } else if (fileName?.matches("""hymnsChorbuch.*\.txt""".toRegex()) == true) {
                            if (setPrivateTexts(uri, BuchMode.Chorbuch)) {
                                if (ok.isNotEmpty()) ok.append(", ")
                                ok.append(" ${BuchMode.Chorbuch}")
                                sendToWear(uri, "/privateTextChorbuch")
                            }
                        }else if (fileName?.matches("""hymnsJugendliederbuch.*\.txt""".toRegex()) == true) {
                            if (setPrivateTexts(uri, BuchMode.Jugendliederbuch)) {
                                if (ok.isNotEmpty()) ok.append(", ")
                                ok.append(" ${BuchMode.Jugendliederbuch}")
                                sendToWear(uri, "/privateTextJugendliederbuch")
                            }
                        }
                    }
                    return@withContext if (ok.toString().isEmpty())
                        "Fehler: Keine passende Datei erkannt"
                    else {
                        MainActivity.colorSettingChanged = true
                        "$ok aktualisiert"
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST", "BlockingMethodInNonBlockingContext")
    suspend fun setPrivateTexts(uri: Uri, buchMode: BuchMode): Boolean {
        val fis: InputStream?
        val ois: ObjectInputStream
        val result: ArrayList<HashMap<String, String>>
        try {
            fis = context.contentResolver.openInputStream(uri)
            ois = ObjectInputStream(fis)
            result = ois.readObject() as ArrayList<HashMap<String, String>>
            ois.close()
            fis!!.close()
        } catch (e: Exception) {
            Log.e("setPrivateHymnText", e.toString() + "\n" + e.message.toString())
            return false
        }
        hymnsRepository.addHymns(result.map {
            Hymn(
                HymnId.create(it["hymnNr"]!!.toInt(),buchMode)!!,
                Rubric(RubricId.create(it["hymnRubricIndex"]!!.toInt(), buchMode)!!),
                it["hymnNrAndTitle"]!!,
                it["hymnTitle"]!!,
                it["hymnText"]!!.replace("</p><p>", "\n\n").replace("<br>", ""),
                it["hymnCopyright"]!!.replace("<br>", ""),
                it["hymnText"]!!.contains("urheberrechtlich geschützt...", ignoreCase = true)
            )
        })
        return true
    }


    @Suppress("UNCHECKED_CAST")
    private fun sendToWear(uri: Uri, path: String) {
        val fis: InputStream?
        val ois: ObjectInputStream
        val result: ArrayList<HashMap<String, String>>
        try {
            fis = context.contentResolver.openInputStream(uri)
            ois = ObjectInputStream(fis)
            result = ois.readObject() as ArrayList<HashMap<String, String>>
            ByteArrayOutputStream().use { bos ->
                val out = ObjectOutputStream(bos)
                out.writeObject(result)
                out.flush()
                SendThread(path, bos.toByteArray()).start()
            }
            ois.close()
            fis?.close()
        } catch (e: Exception) {
            Log.e("Send to Wear", e.toString())
        }
    }

    internal inner class SendThread(private var path: String, private var message: ByteArray) : Thread() {
        override fun run() { //Retrieve the connected devices, known as nodes
            val wearableList = Wearable.getNodeClient(context).connectedNodes
            try {
                val nodes = Tasks.await(wearableList)
                for (node in nodes) {
                    val sendMessageTask = Wearable.getMessageClient(context).sendMessage(node.id, path, message)
                    Tasks.await(sendMessageTask)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}