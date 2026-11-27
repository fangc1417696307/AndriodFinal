package cn.edu.sicnu.cs.fsl.mogu.ui.records

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.FragmentRecordsBinding
import cn.edu.sicnu.cs.fsl.mogu.ui.auth.LoginActivity
import kotlinx.coroutines.launch

class RecordsFragment : Fragment() {
    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: RecordsViewModel by viewModels()
    private val adapter = RecordAdapter(
        onClick = { record ->
            startActivity(
                Intent(requireContext(), RecordDetailActivity::class.java)
                    .putExtra(RecordDetailActivity.EXTRA_RECORD_ID, record.recordId),
            )
        },
        onFavorite = { record -> viewModel.toggleFavorite(record.recordId) },
    )

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // SessionManager 的 Flow 会自动刷新列表和游客态。
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvRecords.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRecords.adapter = adapter
        binding.btnGuestRecordsLogin.setOnClickListener {
            loginLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ServiceLocator.session.isLoggedInFlow.collect { loggedIn ->
                    binding.guestPrompt.isVisible = !loggedIn
                    binding.rvRecords.isVisible = loggedIn
                    if (!loggedIn) {
                        adapter.submitList(emptyList())
                        binding.tvEmpty.isVisible = false
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.records.collect { items ->
                    adapter.submitList(items)
                    binding.tvEmpty.isVisible =
                        ServiceLocator.session.isLoggedIn && items.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvRecords.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
