package cn.edu.sicnu.cs.fsl.mogu.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvBack.setOnClickListener { finish() }

        val settings = ServiceLocator.settings
        binding.etApiKey.setText(settings.apiKey)
        binding.switchOnline.isChecked = settings.useOnline
        updateSwitchText()

        binding.switchOnline.setOnCheckedChangeListener { _, _ -> updateSwitchText() }
        binding.btnSave.setOnClickListener {
            settings.apiKey = binding.etApiKey.text?.toString().orEmpty()
            settings.useOnline = binding.switchOnline.isChecked
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
            if (settings.useOnline && !settings.hasApiKey) {
                Toast.makeText(this, R.string.settings_key_missing, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateSwitchText() {
        binding.switchOnline.text = getString(
            if (binding.switchOnline.isChecked) R.string.settings_mode_online
            else R.string.settings_mode_offline,
        )
    }
}
