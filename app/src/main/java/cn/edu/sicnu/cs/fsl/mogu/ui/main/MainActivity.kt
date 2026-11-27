package cn.edu.sicnu.cs.fsl.mogu.ui.main

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.databinding.ActivityMainBinding
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNav.setOnItemSelectedListener { item ->
            showTab(item.itemId)
            true
        }
        if (savedInstanceState == null) {
            // 游客先看图鉴；已登录用户直接进入识别。
            binding.bottomNav.selectedItemId = defaultTabId
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNav.selectedItemId != defaultTabId) {
                    binding.bottomNav.selectedItemId = defaultTabId
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private val defaultTabId: Int
        get() = if (ServiceLocator.session.isLoggedIn) R.id.nav_identify else R.id.nav_wiki

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
