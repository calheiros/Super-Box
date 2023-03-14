package com.jefferson.application.br.task

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.jefferson.application.br.adapter.MultiSelectRecyclerViewAdapter
import com.jefferson.application.br.database.PathsDatabase
import com.jefferson.application.br.util.StringUtils
import java.io.File
import java.lang.Exception

class VideoDurationUpdaterTask(
    val context: Context,
    val adapter: MultiSelectRecyclerViewAdapter) : JTask(){
    override fun workingThread() {
        val database = PathsDatabase.getInstance(context)
        for(item in adapter.items) {
            if (isCancelled) break
            val duration = getVideoDuration(File(item.path!!), database)
            val time = StringUtils.getFormattedVideoDuration(duration.toString())
            adapter.updateVideoDuration(item.path!!, time)
        }
    }

    override fun onStarted() {
        TODO("Not yet implemented")
    }

    override fun onFinished() {
        TODO("Not yet implemented")
    }

    override fun onException(e: Exception?) {
        TODO("Not yet implemented")
    }

    private fun getVideoDuration(file: File, database: PathsDatabase): Int {
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