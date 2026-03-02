package com.gitfast.app.logging

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import timber.log.Timber
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FileLoggingTreeTest {

    private lateinit var tree: FileLoggingTree
    private lateinit var logDir: File
    private lateinit var logFile: File
    private lateinit var oldFile: File

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        tree = FileLoggingTree(context)
        logDir = File(context.filesDir, "logs")
        logFile = File(logDir, "gitfast.log")
        oldFile = File(logDir, "gitfast.log.old")

        Timber.uprootAll()
        Timber.plant(tree)
    }

    @After
    fun teardown() {
        Timber.uprootAll()
        logDir.deleteRecursively()
    }

    @Test
    fun `WARN log is written to file`() {
        Timber.tag("TestTag").w("Something went wrong")

        assertTrue(logFile.exists())
        val content = logFile.readText()
        assertTrue(content.contains("W/TestTag: Something went wrong"))
    }

    @Test
    fun `ERROR log with throwable includes stack trace`() {
        val exception = RuntimeException("test error")
        Timber.tag("TestTag").e(exception, "Crash happened")

        val content = logFile.readText()
        assertTrue(content.contains("E/TestTag: Crash happened"))
        assertTrue(content.contains("java.lang.RuntimeException: test error"))
    }

    @Test
    fun `DEBUG log is not written to file`() {
        Timber.tag("TestTag").d("Debug message")

        if (logFile.exists()) {
            assertFalse(logFile.readText().contains("Debug message"))
        }
    }

    @Test
    fun `file rotates when exceeding max size`() {
        val bigMessage = "X".repeat(10_000)
        repeat(250) {
            Timber.tag("Tag").w(bigMessage)
        }

        assertTrue(oldFile.exists())
        assertTrue(logFile.length() < 2 * 1024 * 1024)
    }

    @Test
    fun `cleanup removes old files`() {
        logDir.mkdirs()
        val staleFile = File(logDir, "stale.log")
        staleFile.writeText("old data")
        staleFile.setLastModified(System.currentTimeMillis() - 8 * 24 * 60 * 60 * 1000L)

        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        FileLoggingTree(context)

        assertFalse(staleFile.exists())
    }

    @Test
    fun `log entry includes timestamp`() {
        Timber.tag("TestTag").w("timestamped message")

        val content = logFile.readText()
        // Timestamp format: yyyy-MM-dd HH:mm:ss.SSS
        assertTrue(content.contains(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}")))
    }
}
