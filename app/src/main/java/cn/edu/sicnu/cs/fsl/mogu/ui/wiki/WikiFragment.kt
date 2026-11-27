package cn.edu.sicnu.cs.fsl.mogu.ui.wiki

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import cn.edu.sicnu.cs.fsl.mogu.databinding.FragmentWikiBinding
import kotlinx.coroutines.launch

class WikiFragment : Fragment() {
    private var _binding: FragmentWikiBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: WikiViewModel by viewModels()
    private val adapter = MushroomAdapter { mushroom ->
        startActivity(
            Intent(requireContext(), MushroomDetailActivity::class.java)
                .putExtra(MushroomDetailActivity.EXTRA_MUSHROOM_ID, mushroom.mushroomId),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentWikiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvMushrooms.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvMushrooms.adapter = adapter

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setKeyword(text?.toString().orEmpty())
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.saveSearch()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.chipAll.setOnClickListener { viewModel.setEdibleType(0) }
        binding.chipEdible.setOnClickListener { viewModel.setEdibleType(1) }
        binding.chipToxic.setOnClickListener { viewModel.setEdibleType(2) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.mushrooms.collect { items ->
                    adapter.submitList(items)
                    binding.tvEmpty.isVisible = items.isEmpty()
                }
            }
        }
    }

    private fun hideKeyboard() {
        val manager = requireContext().getSystemService(InputMethodManager::class.java)
        manager.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        binding.rvMushrooms.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
