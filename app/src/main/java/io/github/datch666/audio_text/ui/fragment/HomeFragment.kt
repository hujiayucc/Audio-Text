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
import android.os.Handler
import android.os.Looper
import android.provider.Settings
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
import io.github.datch666.audio_text.ui.activity.MainActivity
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.mainActivity
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.permissionNames
import io.github.datch666.core.Callback
import io.github.datch666.core.MusicToText
import io.github.datch666.core.Progress
import io.github.datch666.core.Sample
import io.github.datch666.core.TextToMusic
import io.github.datch666.core.TextUtils
import java.io.File
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
            if (binding.editView.text?.isBlank() == true) {
                Toast.makeText(this, getString(R.string.please_input_your_text), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onGenerate()
        }
    }

    private fun Activity.onGenerate() {
        if (File("${filesDir.path}/audio").exists().not()) {
            File("${filesDir.path}/audio").mkdir()
        }
        val music = TextToMusic("${filesDir.path}/audio", Sample.STANDARD)
        music.start(TextUtils.stringToUnicode(binding.editView.text.toString()), object : Callback {
            override fun onStart() {
                dialog.show()
                dialog.setCancelable(false)
            }

            override fun onSuccess(fileName: String) {
                MainActivity.fileName = fileName
                dialog.setCancelable(true)
            }

            override fun onError(errorMessage: String) {
                onStatusChange(Progress.ERROR, errorMessage)
            }

            override fun onProgress(progress: Progress) {
                onStatusChange(progress)
            }
        })
    }

    private fun onStatusChange(progress: Progress, error: String = "") {
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

            4 -> mainActivity.runOnUiThread {
                dialog.dismiss()
                MaterialAlertDialogBuilder(mainActivity)
                    .setTitle(R.string.error)
                    .setCancelable(false)
                    .setMessage(error)
                    .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                        dialog.dismiss()
                    }.show()
            }
        }
    }
}