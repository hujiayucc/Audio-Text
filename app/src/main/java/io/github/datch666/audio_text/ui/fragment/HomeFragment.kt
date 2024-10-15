package io.github.datch666.audio_text.ui.fragment

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.datch666.audio_text.databinding.FragmentHomeBinding
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.mainActivity
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.permissionNames
import kotlin.system.exitProcess

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val mediaPlayer = MediaPlayer()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        mainActivity.initFragment()
        return binding.root
    }

    fun Activity.initFragment() {
        binding.button.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(arrayOf(READ_EXTERNAL_STORAGE), 0)
                return@setOnClickListener
            } else if (ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(arrayOf(WRITE_EXTERNAL_STORAGE), 1)
                return@setOnClickListener
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager().not()) {
                Toast.makeText(this, "${ActivityCompat.checkSelfPermission(this, MANAGE_EXTERNAL_STORAGE)}", Toast.LENGTH_SHORT).show()
                requestPermission(arrayOf(MANAGE_EXTERNAL_STORAGE), 2)
                return@setOnClickListener
            } else if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                return@setOnClickListener
            }
            val path = binding.editView.text.toString()
            playAudio(path)
        }
    }

    // 播放音频文件
    private fun playAudio(path: String) {
        if (path.isBlank()) {
            Toast.makeText(mainActivity, "Please input the path of the audio file.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            mediaPlayer.setDataSource(path)
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.reset()
            }
        } catch (e: Exception) {
            MaterialAlertDialogBuilder(mainActivity)
                .setTitle("Error")
                .setMessage(e.message)
                .show()
        }
    }

    private fun Activity.requestPermission(permissions: Array<String>, requestCode: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Tips")
            .setMessage("This feature requires ${permissionNames[requestCode]} permission.")
            .setPositiveButton("OK") { _, _ ->
                if (permissions[0] == MANAGE_EXTERNAL_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.setData(Uri.parse("package:io.github.datch666.audio_text"))
                    startActivity(intent)
                    return@setPositiveButton
                }
                requestPermissions(permissions, requestCode)
            }.setNeutralButton("Cancel") { _, _ ->
                exitProcess(403)
            }.show()
    }
}