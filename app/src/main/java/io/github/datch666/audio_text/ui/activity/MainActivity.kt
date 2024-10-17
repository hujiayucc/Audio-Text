package io.github.datch666.audio_text.ui.activity

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.github.datch666.audio_text.R
import io.github.datch666.audio_text.databinding.ActivityMainBinding
import io.github.datch666.audio_text.ui.adapter.ViewPagerAdapter
import io.github.datch666.audio_text.ui.fragment.HomeFragment
import io.github.datch666.audio_text.ui.fragment.SecondFragment
import kotlin.system.exitProcess

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        viewPager2 = binding.viewPager
        setContentView(binding.root)
        initView()
        setSupportActionBar(binding.toolbar)
        initViewPager()
    }

    private fun initView() {
        // 设置系统 UI 标志，允许布局延伸到状态栏区域
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  // 布局延伸到状态栏
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE   // 保持布局稳定
        )

        // 设置状态栏颜色为透明，这样布局背景能够延伸到状态栏后面
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // 适配刘海屏，允许内容延伸到刘海区域
        val params = window.attributes
        params.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = params
    }

    private fun initViewPager() {
        viewPager2.isUserInputEnabled = false
        viewPager2.adapter = ViewPagerAdapter(this, listOf(
            HomeFragment(),
            SecondFragment()
        ))

        TabLayoutMediator(binding.tabLayout, viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_home)
                1 -> getString(R.string.tab_second)
                else -> ""
            }
        }.attach()
    }

    private var exitTime: Long = 0
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - exitTime > 2000) {
                Toast.makeText(this, getString(R.string.exit_tip), Toast.LENGTH_SHORT).show()
                exitTime = System.currentTimeMillis()
            } else {
                finish()
                exitProcess(0)
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0,1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this,
                        getString(R.string.permission_granted, permissionNames[requestCode]), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,
                        getString(R.string.permission_denied, permissionNames[requestCode]), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        val permissionNames = arrayOf(
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "MANAGE_EXTERNAL_STORAGE"
        )
        lateinit var mainActivity: MainActivity
    }
}