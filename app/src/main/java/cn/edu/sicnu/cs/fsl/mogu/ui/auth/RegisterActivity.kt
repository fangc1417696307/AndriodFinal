package cn.edu.sicnu.cs.fsl.mogu.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { register() }
        binding.tvGoLogin.setOnClickListener {
            if (intent.getBooleanExtra(EXTRA_FROM_LOGIN, false)) {
                setResult(RESULT_CANCELED)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }
    }

    private fun register() {
        val username = binding.etUsername.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirm.text?.toString().orEmpty()

        if (password != confirm) {
            Toast.makeText(this, R.string.register_error_confirm, Toast.LENGTH_SHORT).show()
            return
        }
        binding.progress.visibility = android.view.View.VISIBLE
        binding.btnRegister.isEnabled = false
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                ServiceLocator.authRepository.register(username, password)
            }
            binding.progress.visibility = android.view.View.GONE
            binding.btnRegister.isEnabled = true
            result.onSuccess {
                Toast.makeText(this@RegisterActivity, R.string.register_success, Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }.onFailure {
                Toast.makeText(
                    this@RegisterActivity,
                    it.message ?: getString(R.string.register_error_username),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    companion object {
        const val EXTRA_FROM_LOGIN = "from_login"
    }
}
