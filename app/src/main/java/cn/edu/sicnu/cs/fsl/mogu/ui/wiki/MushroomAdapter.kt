package cn.edu.sicnu.cs.fsl.mogu.ui.wiki

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cn.edu.sicnu.cs.fsl.mogu.data.local.entity.MushroomEntity
import cn.edu.sicnu.cs.fsl.mogu.databinding.ItemMushroomBinding
import cn.edu.sicnu.cs.fsl.mogu.util.ImageLoader
import cn.edu.sicnu.cs.fsl.mogu.util.Labels

class MushroomAdapter(
    private val onClick: (MushroomEntity) -> Unit,
) : ListAdapter<MushroomEntity, MushroomAdapter.VH>(Diff) {

    inner class VH(val binding: ItemMushroomBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MushroomEntity) {
            binding.tvName.text = item.name
            binding.tvAlias.text = item.alias?.takeIf { it.isNotBlank() } ?: "别名：—"
            Labels.applyEdibleTag(binding.tvTag, item.edibleType)
            ImageLoader.load(binding.ivCover, item.coverImage)
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemMushroomBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        val Diff = object : DiffUtil.ItemCallback<MushroomEntity>() {
            override fun areItemsTheSame(oldItem: MushroomEntity, newItem: MushroomEntity) =
                oldItem.mushroomId == newItem.mushroomId

            override fun areContentsTheSame(oldItem: MushroomEntity, newItem: MushroomEntity) =
                oldItem == newItem
        }
    }
}
