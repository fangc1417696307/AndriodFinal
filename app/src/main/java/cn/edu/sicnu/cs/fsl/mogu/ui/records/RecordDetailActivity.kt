package cn.edu.sicnu.cs.fsl.mogu.ui.records

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.ActivityRecordDetailBinding
import cn.edu.sicnu.cs.fsl.mogu.ui.feedback.FeedbackActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.wiki.MushroomDetailActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.common.PredictionBinder
import cn.edu.sicnu.cs.fsl.mogu.util.ImageLoader
import cn.edu.sicnu.cs.fsl.mogu.util.Labels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private val userId = ServiceLocator.session.userId
    private var recordId = 0L
    private var favorite = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvBack.setOnClickListener { finish() }

        recordId = intent.getLongExtra(EXTRA_RECORD_ID, 0L)
        if (recordId <= 0) {
            finish()
            return
        }

        binding.btnFavorite.setOnClickListener { toggleFavorite() }
        binding.btnFeedback.setOnClickListener {
            startActivity(
                Intent(this, FeedbackActivity::class.java)
                    .putExtra(FeedbackActivity.EXTRA_RECORD_ID, recordId),
            )
        }
        binding.btnDelete.setOnClickListener { confirmDelete() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            val record = ServiceLocator.recordRepository.getRecord(recordId, userId)
            if (record == null) {
                Toast.makeText(this@RecordDetailActivity, "记录不存在", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            favorite = record.favorite
            binding.tvName.text = record.predictedName
            Labels.applyEdibleTag(binding.tvTag, record.edibleType)
            binding.tvConfidence.text = "置信度：${Labels.confidencePercent(record.confidence)}%"
            binding.tvReview.text = "审核状态：${Labels.reviewLabel(record.reviewStatus)}"
            binding.tvTime.text = "识别时间：${Labels.formatTime(record.identifyTime)}"
            binding.tvSource.text = "识别来源：${if (record.source == "deepseek") "DeepSeek" else "本地模拟"}"
            binding.btnFavorite.text = if (favorite == 1) "★ 已收藏" else "☆ 收藏"
            ImageLoader.load(binding.ivImage, record.imagePath)

            val top3 = PredictionBinder.parse(record.top3Json)
            PredictionBinder.render(binding.containerTop3, top3) { prediction ->
                prediction.mushroomId?.let { id ->
                    startActivity(
                        Intent(this@RecordDetailActivity, MushroomDetailActivity::class.java)
                            .putExtra(MushroomDetailActivity.EXTRA_MUSHROOM_ID, id),
                    )
                }
            }
        }
    }

    private fun toggleFavorite() {
        lifecycleScope.launch {
            val nowFavorite = ServiceLocator.recordRepository.toggleFavorite(recordId, userId)
            favorite = if (nowFavorite) 1 else 0
            binding.btnFavorite.text = if (nowFavorite) "★ 已收藏" else "☆ 收藏"
        }
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.records_delete_title)
            .setMessage(R.string.records_delete_message)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_delete) { _, _ ->
                lifecycleScope.launch {
                    ServiceLocator.recordRepository.delete(recordId, userId)
                    Toast.makeText(
                        this@RecordDetailActivity,
                        R.string.records_delete_success,
                        Toast.LENGTH_SHORT,
                    ).show()
                    finish()
                }
            }
            .show()
    }

    companion object {
        const val EXTRA_RECORD_ID = "record_id"
    }
}
