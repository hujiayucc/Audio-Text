package io.github.datch666.audio_text.ui.fragment

import android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context.CLIPBOARD_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.datch666.audio_text.R
import io.github.datch666.audio_text.databinding.FragmentHomeBinding
import io.github.datch666.audio_text.databinding.ProgressEncodeBinding
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.mainActivity
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.permissionNames
import io.github.datch666.core.Callback
import io.github.datch666.core.Progress
import io.github.datch666.core.TextToMusic
import kotlin.system.exitProcess

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val mediaPlayer = MediaPlayer()
    private val dialog = MaterialAlertDialogBuilder(mainActivity).create()
    private lateinit var dialogBinding: ProgressEncodeBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        dialogBinding = ProgressEncodeBinding.inflate(inflater)
        dialog.setView(dialogBinding.root)
        mainActivity.initFragment()
        return binding.root
    }

    private fun Activity.initFragment() {
        binding.button.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission(arrayOf(READ_EXTERNAL_STORAGE), 0)
                return@setOnClickListener
            } else if (ActivityCompat.checkSelfPermission(
                    this,
                    WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission(arrayOf(WRITE_EXTERNAL_STORAGE), 1)
                return@setOnClickListener
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()
                    .not()
            ) {
                requestPermission(arrayOf(MANAGE_EXTERNAL_STORAGE))
                return@setOnClickListener
            } else if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                return@setOnClickListener
            }
            onGenerate()
        }
    }

    private fun Activity.onGenerate() {
        val music = TextToMusic()
        Thread {
            music.generateAndConcatenate(
                binding.editView.text.toString(),
                cacheDir.path,
                object : Callback {
                    override fun onStart() {
                        dialog.setCancelable(false)
                        mainActivity.runOnUiThread {
                            dialog.show()
                        }
                    }

                    override fun onSuccess(path: String) {
                        playAudio(path)
                    }

                    override fun onError(errorMessage: String) {
                        Log.e("TAG", "onError: $errorMessage")
                    }

                    override fun onProgress(progress: Progress) {
                        onStatusChange(progress)
                    }
                })
        }.start()
    }

    private fun onStatusChange(progress: Progress) {
        when (progress.value) {
            1 -> mainActivity.runOnUiThread {
                dialogBinding.message.text = getString(R.string.generate_audio)
            }

            2 -> mainActivity.runOnUiThread {
                dialogBinding.message.text = getString(R.string.concatenate_audio)
            }

            3 -> mainActivity.runOnUiThread {
                dialogBinding.message.text = getString(R.string.finish)
                dialog.setCancelable(true)
            }
        }
    }

    // 播放音频文件
    private fun playAudio(path: String) {
        if (path.isBlank()) {
            Toast.makeText(
                mainActivity,
                getString(R.string.please_input_the_path_of_the_audio_file), Toast.LENGTH_SHORT
            ).show()
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
            e.printStackTrace()
            var error: String = ""
            for (i in e.stackTrace.indices) {
                error += e.stackTrace[i].toString() + "\n"
            }
            MaterialAlertDialogBuilder(mainActivity)
                .setTitle(getString(R.string.error))
                .setMessage(error)
                .setCancelable(false)
                .setNeutralButton(getString(R.string.copy)) { _, _ ->
                    val clipboardManager =
                        mainActivity.getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboardManager.setPrimaryClip(
                        android.content.ClipData.newPlainText(
                            "error",
                            error
                        )
                    )
                }.setNegativeButton(getString(R.string.close)) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun Activity.requestPermission(permissions: Array<String>, requestCode: Int = 2) {
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
                    intent.setData(Uri.parse("package:io.github.datch666.audio_text"))
                    startActivity(intent)
                    return@setPositiveButton
                }
                requestPermissions(permissions, requestCode)
            }.setNeutralButton(getString(R.string.cancel)) { _, _ ->
                exitProcess(403)
            }.show()
    }
}