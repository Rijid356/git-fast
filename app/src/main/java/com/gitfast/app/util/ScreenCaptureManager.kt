package com.gitfast.app.util

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.widget.Toast
import com.gitfast.app.data.local.ScreenshotDao
import com.gitfast.app.data.local.entity.ScreenshotEntity
import com.gitfast.app.data.sync.FirestoreSync
import com.gitfast.app.service.WorkoutStateManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class ScreenCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenshotDao: ScreenshotDao,
    private val workoutStateManager: WorkoutStateManager,
    private val firestoreSync: FirestoreSync,
) {

    suspend fun captureScreen(activity: Activity): Bitmap? {
        val window = activity.window ?: return null
        val view = window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        return suspendCoroutine { continuation ->
            PixelCopy.request(
                window,
                bitmap,
                { result ->
                    if (result == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        bitmap.recycle()
                        continuation.resume(null)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }
    }

    fun saveBitmapToGallery(bitmap: Bitmap, filename: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/git-fast")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val gitFastDir = java.io.File(dir, "git-fast")
                if (!gitFastDir.exists()) gitFastDir.mkdirs()
                @Suppress("DEPRECATION")
                put(MediaStore.Images.Media.DATA, java.io.File(gitFastDir, filename).absolutePath)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return null

        resolver.openOutputStream(uri)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
        }

        return uri
    }

    suspend fun captureAndSave(activity: Activity, screenRoute: String?) {
        val bitmap = captureScreen(activity) ?: run {
            Toast.makeText(activity, "Screenshot failed", Toast.LENGTH_SHORT).show()
            return
        }

        val timestamp = System.currentTimeMillis()
        val filename = generateFilename(timestamp)
        val uri = saveBitmapToGallery(bitmap, filename)
        bitmap.recycle()

        if (uri == null) {
            Toast.makeText(activity, "Failed to save screenshot", Toast.LENGTH_SHORT).show()
            return
        }

        val workoutState = workoutStateManager.workoutState.value
        val entity = ScreenshotEntity(
            timestamp = timestamp,
            filename = filename,
            galleryUri = uri.toString(),
            workoutId = if (workoutState.isActive) workoutState.workoutId else null,
            activityType = if (workoutState.isActive) workoutState.activityType.name else null,
            screenRoute = screenRoute,
        )
        screenshotDao.insert(entity)

        // Fire-and-forget upload to Firebase Storage
        CoroutineScope(Dispatchers.IO).launch {
            firestoreSync.pushScreenshot(entity)
        }

        @Suppress("DEPRECATION")
        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
        vibrator?.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))

        Toast.makeText(activity, "Saved to Pictures/git-fast/", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

        fun generateFilename(timestamp: Long): String {
            return "git-fast_${dateFormat.format(Date(timestamp))}.png"
        }
    }
}
