package com.jefferson.application.br.task

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.database.AlbumDatabase
import com.jefferson.application.br.util.StringUtils
import java.io.File
import java.lang.Exception

class VideoDurationUpdaterTask(
    val context: Context,
    val adapter: MultiSelectRecyclerViewAdapter) : JTask(){
    override fun workingThread() {
        val database = AlbumDatabase.getInstance(context)
        for(item in adapter.items) {
            if (isCancelled) break
            val duration = getVideoDuration(File(item.path!!), database)
            val time = StringUtils.getFormattedVideoDuration(duration.toString())
            sendUpdate(item.path!!, time)
        }
    }

    override fun onUpdated(data: Array<out Any>?) {
        val path = data?.get(0)
        val time = data?.get(1)
        adapter.updateVideoDuration(path as String, time as String)
    }
    override fun onStarted() {}

    override fun onFinished() {}

    override fun onException(e: Exception?) {
       e?.printStackTrace()
    }

    private fun getVideoDuration(file: File, database: AlbumDatabase): Int {
        var duration = database.getDuration(file.name)
        if (duration == -1 || duration == 0) {
            duration = try {
                val uri = Uri.parse(file.absolutePath)
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(context, uri)
                val durationStr =
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                durationStr!!.toInt()
            } catch (e: Exception) {
                -1
            }
            if (duration != -1)
                database.updateVideoDuration(file.name, duration)
        }
        return duration
    }
}