package io.github.datch666.audio_text.ui.fragment

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.datch666.audio_text.R
import io.github.datch666.audio_text.databinding.FragmentHomeBinding
import io.github.datch666.audio_text.databinding.ProgressEncodeBinding
import io.github.datch666.audio_text.ui.activity.MainActivity
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.mainActivity
import io.github.datch666.core.Callback
import io.github.datch666.core.Sample
import io.github.datch666.core.Status
import io.github.datch666.core.TextToMusic
import io.github.datch666.core.TextUtils
import java.io.File

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val mediaPlayer = MediaPlayer()
    private val dialog = MaterialAlertDialogBuilder(mainActivity).create()
    private lateinit var dialogBinding: ProgressEncodeBinding
    private val handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0  // 用于记录任务开始的时间
    private var sample = Sample.STANDARD

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
                Toast.makeText(this, getString(R.string.please_input_your_text), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            Thread {
                onGenerate()
            }.start()
        }

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.standard.id -> sample = Sample.STANDARD
                binding.high.id -> sample = Sample.HIGH
                binding.veryHigh.id -> sample = Sample.VERY_HIGH
            }
        }
    }

    private fun Activity.onGenerate() {
        if (File("${filesDir.path}/audio").exists().not()) {
            File("${filesDir.path}/audio").mkdir()
        }
        val text = binding.editView.text.toString()
        val music = TextToMusic(
            "${filesDir.path}/audio",
            binding.editView.text.toString().substring(0, if (text.length > 20) 20 else text.length),
            sample
        )
        music.start(TextUtils.stringToUnicode(binding.editView.text.toString()), object : Callback {
            override fun onStart() {
                startTime = System.currentTimeMillis()
                runOnUiThread {
                    dialog.show()
                    dialog.setCancelable(false)
                }
            }

            override fun onSuccess(fileName: String) {
                MainActivity.fileName = fileName
                dialog.setCancelable(true)
            }

            override fun onError(errorMessage: String) {
                onStatusChange(Status.ERROR, errorMessage)
            }

            override fun onStatus(status: Status) {
                onStatusChange(status)
            }

            override fun onProgress(total: Int, progress: Int) {
                onProgressUpdate(total, progress)
            }
        })
    }

    private fun Activity.onProgressUpdate(total: Int, progress: Int) {
        runOnUiThread {
            dialogBinding.progressBar.max = total
            dialogBinding.progressBar.progress = progress
            val percentage = (progress.toDouble() / total * 100).toInt()
            val elapsedTime = System.currentTimeMillis() - startTime
            val remainingTime = if (progress > 0) {
                (elapsedTime / progress) * (total - progress)  // 用已用时间推算剩余时间
            } else {
                0L
            }
            val remainingMinutes = (remainingTime / 1000) / 60
            val remainingSeconds = (remainingTime / 1000) % 60
            val timeEstimate = String.format("%02d:%02d", remainingMinutes, remainingSeconds)
            dialogBinding.message.text = getString(
                R.string.progress_message,
                progress,
                total,
                percentage,
                timeEstimate
            )
        }
    }

    private fun Activity.onStatusChange(status: Status, error: String = "") {
        when (status.value) {
            Status.GENERATING.value -> runOnUiThread {
                dialogBinding.title.text = getString(R.string.generate_audio)
            }

            Status.CONCATENATING.value -> runOnUiThread {
                dialogBinding.title.text = getString(R.string.concatenate_audio)
            }

            Status.FINISHED.value -> runOnUiThread {
                dialogBinding.title.text = getString(R.string.finish)
                dialog.setCancelable(true)
            }

            Status.ERROR.value -> runOnUiThread {
                dialogBinding.title.text = getString(R.string.error)
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