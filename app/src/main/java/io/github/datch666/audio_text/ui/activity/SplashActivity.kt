package io.github.datch666.audio_text.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import io.github.datch666.audio_text.R
import io.github.datch666.audio_text.databinding.ActivitySplashBinding

@Suppress("DEPRECATION")
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        val animation = AnimationUtils.loadAnimation(this, R.anim.splash_image_open)
        val animation2 = AnimationUtils.loadAnimation(this, R.anim.splash_name_open)
        val annotation3 = AnimationUtils.loadAnimation(this, R.anim.splash_copyright_open)
        val annotation4 = AnimationUtils.loadAnimation(this, R.anim.splash_close)
        binding.imageView.startAnimation(animation)
        binding.textView.startAnimation(animation2)
        binding.copyright.startAnimation(annotation3)
        annotation4.fillAfter = true
        annotation4.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                @Suppress("DEPRECATION")
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {

            }
        })
        Handler(mainLooper).postDelayed({
            binding.main.startAnimation(annotation4)
        }, 5000)
    }

    private fun initView() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or        // 隐藏状态栏
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or   // 隐藏导航栏
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or // 布局延伸到状态栏区域
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE        // 保持布局稳定
        )
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // 适配刘海屏
        val params = window.attributes
        params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        window.attributes = params

        // 强制布局扩展到状态栏和导航栏区域
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}