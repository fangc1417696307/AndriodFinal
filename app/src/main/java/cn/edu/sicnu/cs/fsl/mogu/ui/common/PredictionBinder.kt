package cn.edu.sicnu.cs.fsl.mogu.ui.common

import android.view.LayoutInflater
import android.widget.LinearLayout
import cn.edu.sicnu.cs.fsl.mogu.data.model.TopPrediction
import cn.edu.sicnu.cs.fsl.mogu.databinding.ItemTopPredictionBinding
import cn.edu.sicnu.cs.fsl.mogu.util.Labels
import com.google.gson.Gson

object PredictionBinder {
    private val gson = Gson()

    fun parse(json: String?): List<TopPrediction> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson(json, Array<TopPrediction>::class.java)?.toList().orEmpty()
        }.getOrDefault(emptyList())
    }

    fun render(
        container: LinearLayout,
        items: List<TopPrediction>,
        onClick: ((TopPrediction) -> Unit)? = null,
    ) {
        container.removeAllViews()
        items.forEachIndexed { index, item ->
            val binding = ItemTopPredictionBinding.inflate(
                LayoutInflater.from(container.context),
                container,
                false,
            )
            binding.tvRank.text = (index + 1).toString()
            binding.tvName.text = item.name
            Labels.applyEdibleTag(binding.tvTag, item.edibleType)
            binding.tvConfidence.text = "${Labels.confidencePercent(item.confidence)}%"
            if (onClick != null) {
                binding.root.setOnClickListener { onClick(item) }
            }
            container.addView(binding.root)
        }
    }
}
