package cn.edu.sicnu.cs.fsl.mogu.ui.local

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.databinding.FragmentLocalBinding
import cn.edu.sicnu.cs.fsl.mogu.ui.wiki.MushroomAdapter
import cn.edu.sicnu.cs.fsl.mogu.ui.wiki.MushroomDetailActivity
import cn.edu.sicnu.cs.fsl.mogu.util.Labels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class LocalFragment : Fragment() {
    private var _binding: FragmentLocalBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: LocalViewModel by viewModels()
    private val edibleAdapter = MushroomAdapter { openDetail(it.mushroomId) }
    private val toxicAdapter = MushroomAdapter { openDetail(it.mushroomId) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLocalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvEdible.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEdible.adapter = edibleAdapter
        binding.rvToxic.layoutManager = LinearLayoutManager(requireContext())
        binding.rvToxic.adapter = toxicAdapter

        binding.btnRegion.setOnClickListener { showRegionPicker() }
        viewModel.load()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    val selected = state.selected
                    if (selected == null) {
                        binding.tvRegion.text = getString(R.string.local_no_region)
                        binding.btnRegion.text = getString(R.string.local_select_region)
                        binding.sectionEdible.isVisible = false
                        binding.sectionToxic.isVisible = false
                        binding.tvLocalEmpty.isVisible = false
                    } else {
                        binding.tvRegion.text = getString(
                            R.string.local_region_format,
                            selected.name,
                            Labels.macroLabel(selected.macro),
                        )
                        binding.btnRegion.text = getString(R.string.local_change_region)
                        binding.sectionEdible.isVisible = true
                        binding.sectionToxic.isVisible = true
                        edibleAdapter.submitList(state.edible)
                        toxicAdapter.submitList(state.toxic)
                        binding.tvLocalEmpty.isVisible =
                            state.edible.isEmpty() && state.toxic.isEmpty()
                    }
                }
            }
        }
    }

    private fun showRegionPicker() {
        val regions = viewModel.state.value.regions
        if (regions.isEmpty()) {
            Toast.makeText(requireContext(), "地区数据加载中，请稍后", Toast.LENGTH_SHORT).show()
            return
        }
        val names = regions.map { "${it.name}（${Labels.macroLabel(it.macro)}）" }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.local_select_region)
            .setItems(names) { _, which -> viewModel.select(regions[which]) }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun openDetail(id: Int) {
        startActivity(
            Intent(requireContext(), MushroomDetailActivity::class.java)
                .putExtra(MushroomDetailActivity.EXTRA_MUSHROOM_ID, id),
        )
    }

    override fun onDestroyView() {
        binding.rvEdible.adapter = null
        binding.rvToxic.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
