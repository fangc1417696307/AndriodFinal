package cn.edu.sicnu.cs.fsl.mogu.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

object ImageStorage {
    /** 拍照临时文件，放在 cache/camera 下，由 FileProvider 暴露给相机。 */
    fun createCameraFile(context: Context): File {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        return File(dir, "mogu_${System.currentTimeMillis()}.jpg")
    }

    /** 相册图片复制到私有目录，避免 content Uri 授权过期。 */
    fun copyToInternal(context: Context, uri: Uri): File {
        val dir = File(context.filesDir, "identify").apply { mkdirs() }
        val target = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法读取图片" }
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return target
    }

    fun uriForFile(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
