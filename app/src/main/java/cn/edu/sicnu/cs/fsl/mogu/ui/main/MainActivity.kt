package cn.edu.sicnu.cs.fsl.mogu.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.ActivityMainBinding
import cn.edu.sicnu.cs.fsl.mogu.ui.auth.LoginActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.identify.IdentifyFragment
import cn.edu.sicnu.cs.fsl.mogu.ui.local.LocalFragment
import cn.edu.sicnu.cs.fsl.mogu.ui.profile.ProfileFragment
import cn.edu.sicnu.cs.fsl.mogu.ui.records.RecordsFragment
import cn.edu.sicnu.cs.fsl.mogu.ui.wiki.WikiFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentTag: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ServiceLocator.session.isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
        if (savedInstanceState == null) {
            binding.bottomNav.selectedItemId = R.id.nav_identify
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNav.selectedItemId != R.id.nav_identify) {
                    binding.bottomNav.selectedItemId = R.id.nav_identify
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun showTab(itemId: Int) {
        val (tag, factory) = when (itemId) {
            R.id.nav_wiki -> "wiki" to { WikiFragment() }
            R.id.nav_local -> "local" to { LocalFragment() }
            R.id.nav_records -> "records" to { RecordsFragment() }
            R.id.nav_profile -> "profile" to { ProfileFragment() }
            else -> "identify" to { IdentifyFragment() }
        }
        if (tag == currentTag) return
        currentTag = tag
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, factory(), tag)
        }
    }
}
