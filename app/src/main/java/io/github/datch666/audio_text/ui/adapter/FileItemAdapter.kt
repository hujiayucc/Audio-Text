package io.github.datch666.audio_text.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.datch666.audio_text.R
import io.github.datch666.audio_text.databinding.DialogMusicProgressBinding
import io.github.datch666.audio_text.databinding.FileItemBinding
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.fileName
import io.github.datch666.audio_text.ui.activity.MainActivity.Companion.mainActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@SuppressLint("DefaultLocale")
class FileItemAdapter(
    val context: Context,
    var fileList: Array<File>
) : BaseAdapter() {

    // 日期格式化
    private val dateFormat = SimpleDateFormat("dd-MM-yy HH:mm", Locale.getDefault())
    // 控制进度条更新的标志
    private var isUpdatingSeekBar = true

    override fun getCount(): Int = fileList.size

    override fun getItem(position: Int): Any = fileList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: FileItemBinding
        var view = convertView  // 声明为 var 以便可能重新赋值

        if (view == null) {
            // 创建新的 binding 实例
            binding = FileItemBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root  // 重新赋值 view
            view.tag = binding
        } else {
            // 复用已有的视图
            binding = view.tag as FileItemBinding
        }

        // 获取当前文件对象
        val file = fileList[position]

        // 设置文件名
        binding.fileName.text = file.name

        // 格式化并设置文件信息（创建时间和大小）
        val lastModified = Date(file.lastModified())
        val formattedDate = dateFormat.format(lastModified)
        val fileSize = formatFileSize(file.length())
        binding.fileInfo.text = "$formattedDate  $fileSize"

        binding.root.setOnClickListener {
            showPlayDialog(file)
        }

        binding.root.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View?): Boolean {
                itemOnLongClick(view, file)
                return true
            }
        })
        return view
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            sizeInBytes < kb -> "$sizeInBytes B"  // 小于 1KB，用 B 显示
            sizeInBytes < mb -> "${sizeInBytes / kb} KB"  // 小于 1MB，用 KB 显示
            sizeInBytes < gb -> "${sizeInBytes / mb} MB"  // 小于 1GB，用 MB 显示
            else -> "${sizeInBytes / gb} GB"  // 1GB 及以上，用 GB 显示
        }
    }

    private fun itemOnLongClick(view: View, file: File) {
        val popupMenu = PopupMenu(context, view)
        val menuInflater = popupMenu.menuInflater
        menuInflater.inflate(R.menu.file_item_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_delete -> {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.tips)
                        .setMessage(context.getString(R.string.sure_delete_audio, file.name))
                        .setPositiveButton(R.string.ok) { _, _ ->
                            file.delete()
                            fileList = fileList.filter { it != file }.toTypedArray()
                            notifyDataSetChanged()
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                    return@setOnMenuItemClickListener true
                }

                R.id.action_share -> {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        File(file.absolutePath)
                    )
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "audio/wav"
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.app_name)))
                    return@setOnMenuItemClickListener true
                }

                R.id.action_save -> {
                    fileName = file.absolutePath
                    mainActivity.saveAudio()
                    return@setOnMenuItemClickListener true
                }
                else -> return@setOnMenuItemClickListener false
            }
        }
        popupMenu.show()
    }

    @SuppressLint("SetTextI18n")
    private fun showPlayDialog(file: File) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(file.absolutePath)
        mediaPlayer.prepare()

        val binding = DialogMusicProgressBinding.inflate(LayoutInflater.from(context))

        // 初始化 UI
        binding.fileName.text = file.name
        binding.seekBar.max = mediaPlayer.duration
        binding.time.text = formatTime(0) + " / " + formatTime(mediaPlayer.duration)

        // 播放音频
        mediaPlayer.start()
        isUpdatingSeekBar = true  // 启动进度条更新
        updateSeekBar(mediaPlayer, binding.seekBar, binding.time)

        // 创建 MaterialAlertDialogBuilder 对话框
        val dialog = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.close) { dialog, _ ->
                mediaPlayer.stop()
                mediaPlayer.release()
                isUpdatingSeekBar = false  // 停止进度条更新
                dialog.dismiss()
            }
            .setNeutralButton(R.string.pause, null)  // 中性按钮的回调为空
            .create()

        // 显示对话框
        dialog.show()

        // 设置中性按钮点击事件
        val pauseButton = dialog.getButton(DialogInterface.BUTTON_NEUTRAL)
        pauseButton?.setOnClickListener {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                pauseButton.text = context.getString(R.string.play)
            } else {
                mediaPlayer.start()
                pauseButton.text = context.getString(R.string.pause)
                isUpdatingSeekBar = true  // 恢复进度条更新
                updateSeekBar(mediaPlayer, binding.seekBar, binding.time)
            }
        }

        // 播放完成的监听器
        mediaPlayer.setOnCompletionListener {
            pauseButton?.text = context.getString(R.string.play)  // 播放完成后更新按钮文字为“播放”
            isUpdatingSeekBar = false  // 停止进度条更新
        }

        // SeekBar 拖动事件监听
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)  // 跳转到指定位置
                    binding.time.text = formatTime(progress) + " / " + formatTime(mediaPlayer.duration)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (mediaPlayer.isPlaying) mediaPlayer.pause()  // 拖动时暂停播放
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer.start()  // 停止拖动后继续播放
                pauseButton?.text = context.getString(R.string.pause)
                isUpdatingSeekBar = true  // 恢复进度条更新
                updateSeekBar(mediaPlayer, binding.seekBar, binding.time)
            }
        })
    }

    // 更新进度条和时间显示
    @SuppressLint("SetTextI18n")
    private fun updateSeekBar(
        mediaPlayer: MediaPlayer,
        seekBar: SeekBar,
        timeTextView: TextView
    ) {
        if (!isUpdatingSeekBar || !mediaPlayer.isPlaying) return  // 防止非法状态调用

        try {
            seekBar.progress = mediaPlayer.currentPosition
            timeTextView.text = formatTime(mediaPlayer.currentPosition) + " / " + formatTime(mediaPlayer.duration)

            // 每秒更新一次进度条
            Handler(context.mainLooper).postDelayed({
                updateSeekBar(mediaPlayer, seekBar, timeTextView)
            }, 1000)
        } catch (e: IllegalStateException) {
            e.printStackTrace()  // 打印异常日志，确保不会崩溃
        }
    }

    // 将时间格式化为 MM:SS
    private fun formatTime(milliseconds: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds.toLong()) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
