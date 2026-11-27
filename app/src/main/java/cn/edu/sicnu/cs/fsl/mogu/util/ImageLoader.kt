package cn.edu.sicnu.cs.fsl.mogu.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import cn.edu.sicnu.cs.fsl.mogu.R
import java.io.File

object ImageLoader {
    /**
     * 支持三种路径：
     * - assets 内的 covers/xx.jpg
     * - 本地文件绝对路径
     * - http(s) URL（后续如需在线图片）
     */
    fun load(view: ImageView, path: String?) {
        val model: Any? = when {
            path.isNullOrBlank() -> null
            path.startsWith("covers/") -> "file:///android_asset/$path"
            path.startsWith("http://") || path.startsWith("https://") -> path
            else -> File(path)
        }
        Glide.with(view)
            .load(model)
            .placeholder(R.drawable.bg_image_placeholder)
            .error(R.drawable.bg_image_placeholder)
            .centerCrop()
            .into(view)
    }
}
