package io.github.datch666.audio_text.ui.activity

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import io.github.datch666.audio_text.R
import io.github.datch666.audio_text.databinding.ActivityMainBinding
import io.github.datch666.audio_text.databinding.FileListBinding
import io.github.datch666.audio_text.ui.adapter.FileItemAdapter
import io.github.datch666.audio_text.ui.adapter.ViewPagerAdapter
import io.github.datch666.audio_text.ui.fragment.HomeFragment
import io.github.datch666.audio_text.ui.fragment.SecondFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
        viewPager2.adapter = ViewPagerAdapter(
            this, listOf(
                HomeFragment(),
                SecondFragment()
            )
        )

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val fileName = uri.path?.split("/")?.last().toString()
                val filePath =
                    uri.path?.substringBeforeLast("/")?.replace(
                        "/external_files",
                        Environment.getExternalStorageDirectory().absolutePath
                    )
                val file = File(filePath, fileName)
                deFileName = file.absolutePath
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0, 1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_granted, permissionNames[requestCode]),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_denied, permissionNames[requestCode]),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.encode, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_share -> {
                if (fileName == null) {
                    Toast.makeText(
                        this,
                        getString(R.string.please_generate_an_audio_file_first), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    shareAudio()
                }
                true
            }

            R.id.menu_save -> {
                if (fileName == null) {
                    Toast.makeText(
                        this,
                        getString(R.string.please_generate_an_audio_file_first), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    saveAudio()
                }
                return true
            }

            R.id.menu_play -> {
                playAudio()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun shareAudio() {
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            File(fileName.toString())
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "audio/wav"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(intent, getString(R.string.app_name)))
    }

    fun saveAudio() {
        if (ActivityCompat.checkSelfPermission(
                this,
                READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(arrayOf(READ_EXTERNAL_STORAGE), 0)
            return
        } else if (ActivityCompat.checkSelfPermission(
                this,
                WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermission(arrayOf(WRITE_EXTERNAL_STORAGE), 1)
            return
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()
                .not()
        ) {
            requestPermission(arrayOf(MANAGE_EXTERNAL_STORAGE))
            return
        }
        val file = File(fileName.toString())
        var saveFile = File(Environment.getExternalStorageDirectory(), "Download/${file.name}")
        if (saveFile.exists()) saveFile = File(
            Environment.getExternalStorageDirectory(), "Download/${
                file.name.replace(
                    ".wav",
                    "_${System.currentTimeMillis()}.wav"
                )
            }"
        )
        try {
            lifecycleScope.launch(Dispatchers.IO) {
                if (!saveFile.exists()) saveFile.createNewFile()

                val inputStream = FileInputStream(file)
                val fos = FileOutputStream(saveFile)

                val buffer = ByteArray(4096) // 4KB 缓冲区
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    fos.write(buffer, 0, bytesRead)
                }

                inputStream.close()
                fos.close()
            }

            Toast.makeText(
                this,
                getString(R.string.save_success, "${saveFile.absolutePath}"),
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.error))
                .setMessage(e.message)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun playAudio() {
        val listViewBinding = FileListBinding.inflate(layoutInflater)
        val fileList = File(filesDir, "audio").listFiles()
        listViewBinding.listView.adapter = FileItemAdapter(this, fileList!!)
        if (fileList.size != 0) {
            listViewBinding.textView.text = getString(R.string.help)
            listViewBinding.close.visibility = View.VISIBLE
        }
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(listViewBinding.root)
            .setCancelable(fileList.size == 0)
            .show()
        listViewBinding.close.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun requestPermission(permissions: Array<String>, requestCode: Int = 2) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.tips))
            .setMessage(
                getString(
                    R.string.this_feature_requires_permission,
                    permissionNames[requestCode]
                )
            )
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                if (permissions[0] == MANAGE_EXTERNAL_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse("package:io.github.datch666.audio_text")
                    startActivity(intent)
                    return@setPositiveButton
                }
                requestPermissions(permissions, requestCode)
            }.setNeutralButton(getString(R.string.cancel)) { _, _ ->
                exitProcess(403)
            }.show()
    }

    companion object {
        val permissionNames = arrayOf(
            "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE",
            "MANAGE_EXTERNAL_STORAGE"
        )
        lateinit var mainActivity: MainActivity
        var fileName: String? = null
        var deFileName: String? = null
    }
}