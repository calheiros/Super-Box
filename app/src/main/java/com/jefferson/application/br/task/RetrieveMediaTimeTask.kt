package com.jefferson.application.br.task

import android.app.Activity
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.database.PathsDatabase
import com.jefferson.application.br.model.MediaModel
import com.jefferson.application.br.util.Storage
import com.jefferson.application.br.util.StringUtils
import java.io.File

class RetrieveMediaTimeTask(
    private val list: ArrayList<MediaModel>?,
    private val adapter: MultiSelectRecyclerViewAdapter?,
    private val activity: Activity
) : Thread() {
    var isWorking = true
        private set
    private var cancelled = false
    override fun run() {
        try {
            val database = PathsDatabase.getInstance(
                activity,
                Storage.getDefaultStoragePath(activity)
            )
            for (model in list!!) {
                if (!isWorking) break
                try {
                    val file = File(model.path!!)
                    var duration = database.getDuration(file.name)
                    if (duration == -1 || duration == 0) {
                        duration = try {
                            val uri = Uri.parse(model.path)
                            val mmr = MediaMetadataRetriever()
                            mmr.setDataSource(activity, uri)
                            val durationStr =
                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            durationStr!!.toInt()
                        } catch (e: Exception) {
                            -2
                        }
                        database.updateMediaDuration(file.name, duration)
                    }
                    val time =
                        StringUtils.getFormattedVideoDuration(duration.toString())
                    activity.runOnUiThread { adapter?.updateVideoDuration(model.path!!, time) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        } finally {
            isWorking = false
        }
    }

    fun isCancelled(): Boolean {
        return cancelled
    }

    fun stopWork() {
        cancelled = true
        isWorking = false
    }
}
