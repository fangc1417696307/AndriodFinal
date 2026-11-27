package cn.edu.sicnu.cs.fsl.mogu.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.DialogChangePasswordBinding
import cn.edu.sicnu.cs.fsl.mogu.databinding.DialogEditProfileBinding
import cn.edu.sicnu.cs.fsl.mogu.databinding.FragmentProfileBinding
import cn.edu.sicnu.cs.fsl.mogu.ui.auth.LoginActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.feedback.FeedbackActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.settings.SettingsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = requireNotNull(_binding)

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
    }

    override fun onResume() {
        super.onResume()
        loadUser()
    }

    private fun loadUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = ServiceLocator.authRepository.currentUser()
            if (user == null) {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
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
                startActivity(
                    Intent(requireContext(), LoginActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                )
                requireActivity().finish()
            }
            .show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
