package cn.edu.sicnu.cs.fsl.mogu.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private val registerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ServiceLocator.session.isLoggedIn) {
            setResult(RESULT_OK)
            finish()
            return
        }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { login() }
        binding.tvGoRegister.setOnClickListener {
            registerLauncher.launch(
                Intent(this, RegisterActivity::class.java)
                    .putExtra(RegisterActivity.EXTRA_FROM_LOGIN, true),
            )
        }
        binding.tvBrowse.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun login() {
        val username = binding.etUsername.text?.toString().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        if (username.isBlank() || password.isBlank()) {
            Toast.makeText(this, R.string.login_error_empty, Toast.LENGTH_SHORT).show()
            return
        }
        hideKeyboard()
        binding.progress.visibility = android.view.View.VISIBLE
        binding.btnLogin.isEnabled = false
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                ServiceLocator.authRepository.login(username, password)
            }
            binding.progress.visibility = android.view.View.GONE
            binding.btnLogin.isEnabled = true
            result.onSuccess {
                setResult(RESULT_OK)
                finish()
            }.onFailure {
                Toast.makeText(
                    this@LoginActivity,
                    it.message ?: getString(R.string.login_error_failed),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    private fun hideKeyboard() {
        val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}
