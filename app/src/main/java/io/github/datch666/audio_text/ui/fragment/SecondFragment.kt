package io.github.datch666.audio_text.ui.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.github.datch666.audio_text.R
import io.github.datch666.audio_text.databinding.FragmentSecondBinding
import io.github.datch666.audio_text.ui.activity.MainActivity
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.mainActivity
import io.github.datch666.core.MusicToText
import io.github.datch666.core.Sample

class SecondFragment(): Fragment() {
    private lateinit var binding: FragmentSecondBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecondBinding.inflate(inflater, container, false)
        mainActivity.initView()
        return binding.root
    }

    private fun Activity.initView() {
        binding.buttonSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/wav"
            startActivityForResult(Intent.createChooser(intent, "Select Audio"), 2)
        }

        binding.buttonDecrypt.setOnClickListener {
            if (MainActivity.deFileName != null) {
                val text = MusicToText(Sample.STANDARD).decode(MainActivity.deFileName)
                binding.editTextDecryptedContent.setText(text)
            } else {
                Toast.makeText(this,
                    getString(R.string.please_select_a_file_first), Toast.LENGTH_SHORT).show()
            }
        }
    }
}