package com.gitfast.app.logging

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Timber tree that writes WARN and ERROR logs to a file on disk.
 *
 * - Log file: `<filesDir>/logs/gitfast.log`
 * - Max size: 2 MB, rotates to `gitfast.log.old`
 * - Auto-cleanup: deletes files older than 7 days
 * - Max disk usage: ~4 MB (current + old)
 * - All exceptions swallowed — logging never crashes the app.
 */
class FileLoggingTree(context: Context) : Timber.Tree() {

    private val logDir = File(context.filesDir, "logs")
    private val logFile = File(logDir, "gitfast.log")
    private val oldFile = File(logDir, "gitfast.log.old")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val lock = Any()

    companion object {
        private const val MAX_SIZE_BYTES = 2 * 1024 * 1024L // 2 MB
        private const val MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    init {
        try {
            logDir.mkdirs()
            cleanup()
        } catch (_: Exception) {
            // Never crash due to logging setup
        }
    }

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        return priority >= Log.WARN
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            val level = when (priority) {
                Log.WARN -> "W"
                Log.ERROR -> "E"
                Log.ASSERT -> "A"
                else -> "?"
            }
            val timestamp = dateFormat.format(Date())
            val line = buildString {
                append("$timestamp $level/${tag ?: "---"}: $message")
                if (t != null) {
                    append("\n")
                    append(t.stackTraceToString())
                }
                append("\n")
            }

            synchronized(lock) {
                rotateIfNeeded()
                FileWriter(logFile, true).use { it.write(line) }
            }
        } catch (_: Exception) {
            // Never crash due to logging
        }
    }

    private fun rotateIfNeeded() {
        try {
            if (logFile.exists() && logFile.length() >= MAX_SIZE_BYTES) {
                oldFile.delete()
                logFile.renameTo(oldFile)
            }
        } catch (_: Exception) {
            // Swallow
        }
    }

    private fun cleanup() {
        try {
            val now = System.currentTimeMillis()
            logDir.listFiles()?.forEach { file ->
                if (now - file.lastModified() > MAX_AGE_MS) {
                    file.delete()
                }
            }
        } catch (_: Exception) {
            // Swallow
        }
    }
}
