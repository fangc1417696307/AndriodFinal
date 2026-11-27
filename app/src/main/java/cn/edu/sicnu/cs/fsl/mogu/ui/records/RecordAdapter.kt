package cn.edu.sicnu.cs.fsl.mogu.ui.records

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.IdentifyRecordEntity
import cn.edu.sicnu.cs.fsl.mogu.databinding.ItemRecordBinding
import cn.edu.sicnu.cs.fsl.mogu.util.ImageLoader
import cn.edu.sicnu.cs.fsl.mogu.util.Labels

class RecordAdapter(
    private val onClick: (IdentifyRecordEntity) -> Unit,
    private val onFavorite: (IdentifyRecordEntity) -> Unit,
) : ListAdapter<IdentifyRecordEntity, RecordAdapter.VH>(Diff) {

    inner class VH(val binding: ItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IdentifyRecordEntity) {
            binding.tvName.text = item.predictedName
            binding.tvTime.text = Labels.formatTime(item.identifyTime)
            binding.tvConfidence.text = "置信度 ${Labels.confidencePercent(item.confidence)}%"
            binding.tvFavorite.text = if (item.favorite == 1) "★" else "☆"
            Labels.applyEdibleTag(binding.tvTag, item.edibleType)
            ImageLoader.load(binding.ivImage, item.imagePath)
            binding.root.setOnClickListener { onClick(item) }
            binding.tvFavorite.setOnClickListener { onFavorite(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val Diff = object : DiffUtil.ItemCallback<IdentifyRecordEntity>() {
            override fun areItemsTheSame(
                oldItem: IdentifyRecordEntity,
                newItem: IdentifyRecordEntity,
            ) = oldItem.recordId == newItem.recordId

            override fun areContentsTheSame(
                oldItem: IdentifyRecordEntity,
                newItem: IdentifyRecordEntity,
            ) = oldItem == newItem
        }
    }
}
