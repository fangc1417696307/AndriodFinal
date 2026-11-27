package cn.edu.sicnu.cs.fsl.mogu.util

import android.widget.TextView
import androidx.core.content.ContextCompat
import cn.edu.sicnu.cs.fsl.mogu.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 全局标签、颜色和格式化工具，避免各模块各写一套。
 */
object Labels {
    fun edibleLabel(type: Int?): String = when (type) {
        1 -> "可食用"
        2 -> "有毒"
        else -> "待确认"
    }

    fun edibleBackground(type: Int?): Int = when (type) {
        1 -> R.drawable.bg_tag_green
        2 -> R.drawable.bg_tag_red
        else -> R.drawable.bg_tag_amber
    }

    fun edibleTextColor(type: Int?): Int = when (type) {
        1 -> R.color.mogu_green_dark
        2 -> R.color.mogu_red
        else -> R.color.mogu_amber
    }

    fun applyEdibleTag(view: TextView, type: Int?) {
        view.setText(edibleLabel(type))
        view.setBackgroundResource(edibleBackground(type))
        view.setTextColor(ContextCompat.getColor(view.context, edibleTextColor(type)))
    }

    fun reviewLabel(status: Int?): String = when (status) {
        1 -> "审核通过"
        2 -> "审核驳回"
        else -> "待审核"
    }

    fun reviewBackground(status: Int?): Int = when (status) {
        1 -> R.drawable.bg_tag_green
        2 -> R.drawable.bg_tag_red
        else -> R.drawable.bg_tag_amber
    }

    fun applyReviewTag(view: TextView, status: Int?) {
        view.setText(reviewLabel(status))
        view.setBackgroundResource(reviewBackground(status))
        view.setTextColor(
            ContextCompat.getColor(
                view.context,
                when (status) {
                    1 -> R.color.mogu_green_dark
                    2 -> R.color.mogu_red
                    else -> R.color.mogu_amber
                },
            ),
        )
    }

    fun feedbackStatusLabel(status: Int?): String = if (status == 1) "已处理" else "待处理"

    fun feedbackStatusBackground(status: Int?): Int =
        if (status == 1) R.drawable.bg_tag_green else R.drawable.bg_tag_amber

    fun confidencePercent(confidence: Double): Int = (confidence * 100).toInt()

    fun macroLabel(code: String): String = when (code) {
        "SW" -> "西南地区"
        "SC" -> "华南地区"
        "CC" -> "华中地区"
        "EC" -> "华东地区"
        "NC" -> "华北地区"
        "NE" -> "东北地区"
        "NW" -> "西北地区"
        else -> "全国"
    }

    fun formatTime(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date(millis))
}
