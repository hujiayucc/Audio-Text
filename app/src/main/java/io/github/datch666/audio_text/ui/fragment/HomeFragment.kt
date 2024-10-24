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
                onStatusChange(Status.ERROR, errorMessage)
            }

            override fun onStatus(status: Status) {
                onStatusChange(status)
            }
        })
    }

    private fun onStatusChange(status: Status, error: String = "") {
        when (status.value) {
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