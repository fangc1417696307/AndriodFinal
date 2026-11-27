package cn.edu.sicnu.cs.fsl.mogu.ui.feedback

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.ActivityFeedbackBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedbackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedbackBinding
    private val userId: Long
        get() = ServiceLocator.session.userId
    private val adapter = FeedbackAdapter()
    private var recordId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ServiceLocator.session.isLoggedIn) {
            Toast.makeText(this, R.string.feedback_login_required, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvBack.setOnClickListener { finish() }

        recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L).takeIf { it > 0 }
        binding.rvFeedback.layoutManager = LinearLayoutManager(this)
        binding.rvFeedback.adapter = adapter

        binding.btnSubmit.setOnClickListener { submit() }
        recordId?.let { id ->
            lifecycleScope.launch {
                val record = ServiceLocator.recordRepository.getRecord(id, userId)
                if (record != null) {
                    binding.tvRecordTip.isVisible = true
                    binding.tvRecordTip.text = getString(
                        R.string.feedback_record_tip,
                        record.predictedName,
                    )
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ServiceLocator.feedbackRepository.observeMine(userId).collect { items ->
                    adapter.submitList(items)
                    binding.tvEmpty.isVisible = items.isEmpty()
                }
            }
        }
    }

    private fun submit() {
        val content = binding.etContent.text?.toString().orEmpty()
        binding.btnSubmit.isEnabled = false
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                ServiceLocator.feedbackRepository.submit(userId, content, recordId)
            }
            binding.btnSubmit.isEnabled = true
            result.onSuccess {
                binding.etContent.text?.clear()
                Toast.makeText(this@FeedbackActivity, R.string.feedback_success, Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(
                    this@FeedbackActivity,
                    it.message ?: "提交失败",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    companion object {
        const val EXTRA_RECORD_ID = "record_id"
    }
}
