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

    private void createFileList(String[] files, String listFilePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(listFilePath));
        for (String file : files) {
            writer.write("file '" + file + "'\n");
        }
        writer.close();
    }

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
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
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

    @SuppressLint("DefaultLocale")
    public void generateAndConcatenate(String text, String outputDir, Callback callback) {
        File outputDirFile = new File(outputDir);
        // 清空缓存
        if (outputDirFile.exists()) {
            for (File file : outputDirFile.listFiles()) {
                file.delete();
            }
        }
        String finalOutput = String.format("%s/%s.wav", outputDir, System.currentTimeMillis());
        generateWavForEachChar(text, outputDir);
        String[] wavFiles = new String[text.length()];
        for (int i = 0; i < text.length(); i++) {
            wavFiles[i] = String.format("%s/char_%d.wav", outputDir, i);
        }
        concatenateWavFiles(wavFiles, outputDir, finalOutput, callback);
    }
}