package io.github.datch666.audio_text.ui.activity

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.datch666.audio_text.R
import io.github.datch666.audio_text.databinding.ActivityMainBinding
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        setSupportActionBar(binding.toolbar)
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
}