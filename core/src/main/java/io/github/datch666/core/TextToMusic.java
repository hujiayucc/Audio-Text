package io.github.datch666.core;

import android.annotation.SuppressLint;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.ReturnCode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TextToMusic {
    private static final String TAG = "TextToMusic";

    /**
     * 创建包含文件列表的文本文件
     * 该方法用于将一组文件路径写入到一个指定的文本文件中
     * 每个文件路径将在文本文件中以"file '路径'"的格式单独占一行
     *
     * @param files 文件路径数组，表示需要列出的文件集合
     * @param listFilePath 字符串，表示将要创建的文件列表的路径
     * @throws IOException 如果写入文件时发生I/O错误
     */
    private void createFileList(String[] files, String listFilePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(listFilePath));
        for (String file : files) {
            writer.write("file '" + file + "'\n");
        }
        writer.close();
    }

    /**
     * 合并后删除临时文件
     *
     * @param outputDir  输出目录路径，用于查找临时文件
     * @param outputPath 最终合并文件的路径，用于保留该文件不被删除
     */
    private void deleteTempFiles(String outputDir, String outputPath) {
        // 创建一个File对象代表输出目录
        File outputDirFile = new File(outputDir);
        // 检查输出目录是否存在
        if (outputDirFile.exists()) {
            // 遍历输出目录中的所有文件
            for (File file : outputDirFile.listFiles()) {
                // 如果当前文件名不在最终输出路径中，则删除该文件
                if (!outputPath.contains(file.getName()))
                    file.delete();
            }
        }
    }

    /**
     * 将多个 WAV 文件拼接成一个文件
     *
     * @param wavFiles     需要拼接的 WAV 文件路径数组
     * @param outputDir    输出文件列表的目录
     * @param outputPath   拼接完成的文件输出路径
     * @param callback     拼接操作完成后的回调
     *
     * 此方法首先创建一个包含所有 WAV 文件路径的文本文件，然后使用 FFmpeg 根据这个文件列表
     * 将所有 WAV 文件拼接成一个文件此方法使用异步方式执行 FFmpeg 命令，并在操作完成后通过回调
     * 通知调用者操作结果
     */
    private void concatenateWavFiles(
            String[] wavFiles,
            String outputDir,
            String outputPath,
            Callback callback
    ) {
        try {
            String listFilePath = String.format("%s/wav_list.txt", outputDir);
            createFileList(wavFiles, listFilePath);

            // 使用 FFmpeg 拼接文件
            String ffmpegCommand = String.format(
                    "-f concat -safe 0 -i %s -c copy %s",
                    listFilePath,
                    outputPath
            );

            Log.i(TAG, "输出：" + ffmpegCommand);

            FFmpegKit.executeAsync(ffmpegCommand, session -> {
                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    Log.i(TAG, "Audio files concatenated successfully!");
                    callback.onSuccess(outputPath);
                } else {
                    callback.onError("Failed to concatenate audio files:\n" + session.getFailStackTrace());
                }
                callback.onProgress(Progress.CLEAR);
                deleteTempFiles(outputDir, outputPath);
                callback.onProgress(Progress.FINISHED);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据输入的文本，为每个字符生成一个wav音频文件
     * 此方法遍历输入的文本，为每个字符生成一个特定频率的音频文件
     * 频率由字符到频率的映射决定，每个字符的音频保存在指定的输出目录中
     *
     * @param text 输入的文本，将为其中的每个字符生成音频
     * @param outputDir 输出目录，生成的音频文件将保存在此目录中
     */
    private void generateWavForEachChar(String text, String outputDir) {
        int index = 0;
        for (char c : text.toCharArray()) {
            double frequency = CharToFrequencyMapper.getFrequency(c);
            String outputPath = String.format("%s/char_%d.wav", outputDir, index);

            // 构建 FFmpeg 命令，每个字符生成一个独立的 wav 文件
            String ffmpegCommand = String.format(
                    "-f lavfi -i aevalsrc='sin(2*PI*t*%.2f)':d=0.5 -acodec pcm_s16le -ar 44100 %s",
                    frequency,
                    outputPath
            );
            FFmpegKit.execute(ffmpegCommand);
            index++;
        }
    }

    /**
     * 根据给定的文本生成每个字符对应的音频文件，并将这些音频文件合并成一个音频文件
     *
     * @param text 输入的文本，将为每个字符生成音频
     * @param outputDir 输出目录的路径，用于保存生成的音频文件
     * @param callback 回调接口，用于通知音频生成和合并完成
     */
    public void generateAndConcatenate(String text, String outputDir, Callback callback) {
        callback.onStart();
        File outputDirFile = new File(outputDir);
        // 清空缓存
        if (outputDirFile.exists()) {
            for (File file : outputDirFile.listFiles()) {
                file.delete();
            }
        }
        String finalOutput = String.format("%s/%s.wav", outputDir, System.currentTimeMillis());
        callback.onProgress(Progress.GENERATING);
        generateWavForEachChar(text, outputDir);
        String[] wavFiles = new String[text.length()];
        for (int i = 0; i < text.length(); i++) {
            wavFiles[i] = String.format("%s/char_%d.wav", outputDir, i);
        }
        callback.onProgress(Progress.CONCATENATING);
        concatenateWavFiles(wavFiles, outputDir, finalOutput, callback);
    }
}