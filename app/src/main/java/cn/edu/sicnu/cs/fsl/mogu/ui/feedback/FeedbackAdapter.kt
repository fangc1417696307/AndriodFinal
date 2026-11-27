package cn.edu.sicnu.cs.fsl.mogu.ui.feedback

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.FeedbackEntity
import cn.edu.sicnu.cs.fsl.mogu.databinding.ItemFeedbackBinding
import cn.edu.sicnu.cs.fsl.mogu.util.Labels

class FeedbackAdapter : ListAdapter<FeedbackEntity, FeedbackAdapter.VH>(Diff) {

    inner class VH(val binding: ItemFeedbackBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FeedbackEntity) {
            binding.tvContent.text = item.content
            binding.tvStatus.text = Labels.feedbackStatusLabel(item.status)
            binding.tvStatus.setBackgroundResource(Labels.feedbackStatusBackground(item.status))
            binding.tvStatus.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (item.status == 1) R.color.mogu_green_dark else R.color.mogu_amber,
                ),
            )
            binding.tvTime.text = Labels.formatTime(item.createdAt)
            if (item.handleRemark.isNullOrBlank()) {
                binding.tvHandle.visibility = View.GONE
            } else {
                binding.tvHandle.visibility = View.VISIBLE
                binding.tvHandle.text = "处理意见：${item.handleRemark}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemFeedbackBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val Diff = object : DiffUtil.ItemCallback<FeedbackEntity>() {
            override fun areItemsTheSame(oldItem: FeedbackEntity, newItem: FeedbackEntity) =
                oldItem.feedbackId == newItem.feedbackId

            override fun areContentsTheSame(oldItem: FeedbackEntity, newItem: FeedbackEntity) =
                oldItem == newItem
        }
    }
}
