package com.example.aonime

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object SubtitleProcessor {

    suspend fun processAndShiftSubtitle(
        context: Context,
        vttUrl: String,
        delaySeconds: Float
    ): String? = withContext(Dispatchers.IO) {
        try {
            // Note: In Aonime-mobile it uses BuildConfig.BASE_URL. We'll just use the URL directly since it's a proxy in Aonime-mobile
            // but for safety let's ensure it's absolute
            val actualUrl = if (vttUrl.startsWith("http")) vttUrl else vttUrl
            if (!actualUrl.startsWith("http")) return@withContext vttUrl // Not a remote URL

            val connection = URL(actualUrl).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val text = connection.getInputStream().bufferedReader().use { it.readText() }
            
            val shiftedText = if (delaySeconds == 0f) {
                text
            } else {
                shiftVtt(text, delaySeconds)
            }

            // Save to cache dir
            val file = File(context.cacheDir, "shifted_sub_${System.currentTimeMillis()}.vtt")
            FileOutputStream(file).use {
                it.write(shiftedText.toByteArray())
            }
            return@withContext file.toURI().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    private fun shiftVtt(vtt: String, delay: Float): String {
        val lines = vtt.split("\n")
        val builder = java.lang.StringBuilder()
        val regex = Regex("^([^\\s]+)\\s*-->\\s*([^\\s]+)(.*)$")

        for (line in lines) {
            val match = regex.find(line.trim())
            if (match != null) {
                val startStr = match.groupValues[1]
                val endStr = match.groupValues[2]
                val rest = match.groupValues[3]

                if (startStr.contains(":") && endStr.contains(":")) {
                    val startSec = Math.max(0f, parseTime(startStr) + delay)
                    val endSec = Math.max(0f, parseTime(endStr) + delay)
                    builder.append(timeStr(startSec)).append(" --> ").append(timeStr(endSec)).append(rest).append("\n")
                    continue
                }
            }
            builder.append(line).append("\n")
        }
        return builder.toString()
    }

    private fun parseTime(s: String): Float {
        val p = s.trim().split(":")
        var h = 0f
        var m = 0f
        var sec = 0f
        if (p.size == 3) {
            h = p[0].toFloat()
            m = p[1].toFloat()
            sec = p[2].toFloat()
        } else if (p.size == 2) {
            m = p[0].toFloat()
            sec = p[1].toFloat()
        } else {
            sec = p[0].toFloat()
        }
        return h * 3600f + m * 60f + sec
    }

    private fun timeStr(s: Float): String {
        var sec = s
        if (sec < 0) sec = 0f
        val h = Math.floor((sec / 3600).toDouble()).toInt()
        val m = Math.floor(((sec % 3600) / 60).toDouble()).toInt()
        val ss = Math.floor((sec % 60).toDouble()).toInt()
        val ms = Math.round((sec % 1) * 1000)
        
        return String.format("%02d:%02d:%02d.%03d", h, m, ss, ms)
    }
}
