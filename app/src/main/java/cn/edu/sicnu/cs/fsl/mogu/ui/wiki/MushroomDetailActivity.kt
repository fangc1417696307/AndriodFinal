package cn.edu.sicnu.cs.fsl.mogu.ui.wiki

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.ActivityMushroomDetailBinding
import cn.edu.sicnu.cs.fsl.mogu.util.ImageLoader
import cn.edu.sicnu.cs.fsl.mogu.util.Labels
import kotlinx.coroutines.launch

class MushroomDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMushroomDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMushroomDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvBack.setOnClickListener { finish() }

        val id = intent.getIntExtra(EXTRA_MUSHROOM_ID, 0)
        if (id <= 0) {
            finish()
            return
        }
        lifecycleScope.launch {
            val mushroom = ServiceLocator.mushroomRepository.getById(id)
            if (mushroom == null) {
                Toast.makeText(this@MushroomDetailActivity, "品种不存在", Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            binding.tvName.text = mushroom.name
            Labels.applyEdibleTag(binding.tvTag, mushroom.edibleType)
            binding.tvAlias.text = mushroom.alias?.takeIf { it.isNotBlank() }?.let { "别名：$it" } ?: ""
            binding.tvCategory.text = "分类：${mushroom.categoryName}"
            binding.tvSeason.text = "生长季节：${mushroom.growthSeason ?: "—"}"
            binding.tvMorphology.text = mushroom.morphology ?: "—"
            binding.tvDistribution.text = mushroom.distribution ?: "—"
            binding.tvToxicity.text = mushroom.toxicityDesc ?: "—"
            binding.tvAdvice.text = mushroom.edibleAdvice ?: "—"
            binding.tvContent.text = mushroom.content ?: "—"
            ImageLoader.load(binding.ivCover, mushroom.coverImage)
        }
    }

    companion object {
        const val EXTRA_MUSHROOM_ID = "mushroom_id"
    }
}
