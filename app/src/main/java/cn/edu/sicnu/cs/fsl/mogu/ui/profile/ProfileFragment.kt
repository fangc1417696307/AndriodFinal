package cn.edu.sicnu.cs.fsl.mogu.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.DialogChangePasswordBinding
import cn.edu.sicnu.cs.fsl.mogu.databinding.DialogEditProfileBinding
import cn.edu.sicnu.cs.fsl.mogu.databinding.FragmentProfileBinding
import cn.edu.sicnu.cs.fsl.mogu.ui.auth.LoginActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.auth.RegisterActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.feedback.FeedbackActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.settings.SettingsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
            refreshUi()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGuestLogin.setOnClickListener {
            authLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
        }
        binding.btnGuestRegister.setOnClickListener {
            authLauncher.launch(Intent(requireContext(), RegisterActivity::class.java))
        }
        binding.btnGuestSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.btnEditProfile.setOnClickListener { showEditDialog() }
        binding.btnChangePassword.setOnClickListener { showPasswordDialog() }
        binding.btnFeedback.setOnClickListener {
            startActivity(Intent(requireContext(), FeedbackActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
        binding.btnAbout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_about)
                .setMessage(R.string.profile_about_text)
                .setPositiveButton(R.string.common_confirm, null)
                .show()
        }
        binding.btnLogout.setOnClickListener { confirmLogout() }
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        refreshUi()
    }

    private fun refreshUi() {
        val loggedIn = ServiceLocator.session.isLoggedIn
        binding.guestCard.isVisible = !loggedIn
        binding.contentGroup.isVisible = loggedIn
        if (loggedIn) loadUser()
    }

    private fun loadUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = ServiceLocator.authRepository.currentUser()
            if (user == null) {
                // 本地用户数据异常时退化为游客态，不强制跳登录页。
                ServiceLocator.session.clear()
                refreshUi()
                return@launch
            }
            binding.tvName.text = user.realName
            binding.tvUsername.text = "@${user.username}"
            binding.tvPhone.text = user.phone?.takeIf { it.isNotBlank() } ?: "未填写手机号"
        }
    }

    private fun showEditDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(layoutInflater)
        viewLifecycleOwner.lifecycleScope.launch {
            val user = ServiceLocator.authRepository.currentUser() ?: return@launch
            dialogBinding.etRealName.setText(user.realName)
            dialogBinding.etPhone.setText(user.phone.orEmpty())
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.profile_edit)
                .setView(dialogBinding.root)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save) { _, _ ->
                    val name = dialogBinding.etRealName.text?.toString().orEmpty()
                    val phone = dialogBinding.etPhone.text?.toString()
                    if (name.isBlank()) {
                        Toast.makeText(requireContext(), "姓名不能为空", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            ServiceLocator.authRepository.updateProfile(name, phone, user.avatar)
                        }
                        Toast.makeText(requireContext(), R.string.profile_save_success, Toast.LENGTH_SHORT).show()
                        loadUser()
                    }
                }
                .show()
        }
    }

    private fun showPasswordDialog() {
        val dialogBinding = DialogChangePasswordBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profile_change_password)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_save) { _, _ ->
                val old = dialogBinding.etOldPassword.text?.toString().orEmpty()
                val new = dialogBinding.etNewPassword.text?.toString().orEmpty()
                val confirm = dialogBinding.etConfirmPassword.text?.toString().orEmpty()
                if (new != confirm) {
                    Toast.makeText(requireContext(), R.string.profile_password_mismatch, Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    val result = withContext(Dispatchers.IO) {
                        ServiceLocator.authRepository.changePassword(old, new)
                    }
                    result.onSuccess {
                        Toast.makeText(requireContext(), R.string.profile_password_success, Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(
                            requireContext(),
                            it.message ?: getString(R.string.profile_password_error),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
            .show()
    }

    private fun confirmLogout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout_title)
            .setMessage(R.string.logout_message)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_logout) { _, _ ->
                ServiceLocator.authRepository.logout()
                refreshUi()
                Toast.makeText(requireContext(), R.string.logout_success, Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
