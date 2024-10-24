package io.github.datch666.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本转换为音乐
 */
public class TextToMusic {
    private final String TAG = "TextToMusic";
    /**
     * 采样率
     */
    private final int SAMPLE;
    /**
     * 输出路径
     */
    private final String OUTPUT_PATH;
    /**
     * 输出文件名
     */
    private final String OUTPUT_FILE_NAME;
    /**
     * 持续时间（秒）
     */
    private final double DURATION = 0.5;

    /**
     * 标准采样率
     * @param outputPath 输出路径
     */
    public TextToMusic(String outputPath, String fileName) {
        OUTPUT_PATH = outputPath;
        SAMPLE = Sample.STANDARD.getValue();
        OUTPUT_FILE_NAME = fileName.replace("/", "_")
                .replace("\\", "_");
    }

    /**
     * 自定义采样率
     *
     * @param outputPath 输出路径
     * @param sample {@link Sample} 采样率
     */
    public TextToMusic(String outputPath, String fileName, Sample sample) {
        OUTPUT_PATH = outputPath;
        SAMPLE = sample.getValue();
        OUTPUT_FILE_NAME = fileName.replace("/", "_")
                .replace("\\", "_");
    }

    private double[] getFrequencys(String text) {
        double[] frequencies = new double[text.length()];
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            double frequency = CharToFrequencyMapper.getFrequency(c);
            frequencies[i] = frequency;
        }
        return frequencies;
    }

    // 生成单个音频文件
    private boolean generateTone(double frequency, String fileName, Callback callback) {
        callback.onStatus(Status.GENERATING);
        int numSamples = (int) (DURATION * SAMPLE);
        double[] samples = new double[numSamples];
        byte[] generatedSound = new byte[2 * numSamples];

        // 生成正弦波数据
        for (int i = 0; i < numSamples; ++i) {
            samples[i] = Math.sin(2 * Math.PI * i / (SAMPLE / frequency));
        }

        // 将正弦波数据转换为16位PCM格式
        int idx = 0;
        for (final double dVal : samples) {
            // 将样本值转换为16位PCM格式
            final short val = (short) ((dVal * 32767));
            generatedSound[idx++] = (byte) (val & 0x00ff);
            generatedSound[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        // 创建WAV文件并写入头部信息和数据
        File file = new File(OUTPUT_PATH, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            writeWavHeader(fos);
            fos.write(generatedSound);
        } catch (IOException e) {
            callback.onError(e.getMessage());
            return false;
        }
        return true;
    }

    private void writeWavHeader(FileOutputStream fos) throws IOException {
        byte[] header = new byte[44];

        long totalDataLen = SAMPLE * 2L + 36;
        long byteRate = SAMPLE * 2L;

        // RIFF/WAVE header
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = 1;  // channels = 1
        header[23] = 0;
        header[24] = (byte) (SAMPLE & 0xff);
        header[25] = (byte) ((SAMPLE >> 8) & 0xff);
        header[26] = (byte) ((SAMPLE >> 16) & 0xff);
        header[27] = (byte) ((SAMPLE >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = 2;  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (SAMPLE * 2 & 0xff);
        header[41] = (byte) ((SAMPLE * 2 >> 8) & 0xff);
        header[42] = (byte) ((SAMPLE * 2 >> 16) & 0xff);
        header[43] = (byte) ((SAMPLE * 2 >> 24) & 0xff);

        fos.write(header, 0, 44);
    }

    private void writeWavHeader(FileOutputStream fos, int totalAudioLen, int samples, int channels) throws IOException {
        byte[] header = new byte[44];

        long totalDataLen = totalAudioLen + 36;
        long byteRate = (long) samples * channels * 2;

        // RIFF/WAVE header
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;  // channels
        header[23] = 0;
        header[24] = (byte) (samples & 0xff);
        header[25] = (byte) ((samples >> 8) & 0xff);
        header[26] = (byte) ((samples >> 16) & 0xff);
        header[27] = (byte) ((samples >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 2);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        fos.write(header, 0, 44);
    }

    // 合并多个WAV文件
    private boolean mergeWavFiles(String[] inputFiles, Callback callback) {
        int totalDataSize = 0;
        int sampleRate = 0;
        int channels = 0;

        // 创建输出WAV文件
        try (FileOutputStream fos = new FileOutputStream(OUTPUT_PATH + "/" + OUTPUT_FILE_NAME + ".wav")) {
            callback.onStatus(Status.CONCATENATING);
            // 第一遍：计算总的数据大小、采样率和通道数
            for (int i = 0; i < inputFiles.length; i++) {
                String inputFile = inputFiles[i];
                callback.onProgress(inputFiles.length, i + 1);
                File file = new File(OUTPUT_PATH, inputFile);

                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] header = new byte[44];
                    fis.read(header);  // 读取WAV头部

                    // 获取采样率和通道数
                    if (i == 0) {  // 只在第一个文件中获取采样率和通道数
                        sampleRate = ByteBuffer.wrap(header, 24, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        channels = ByteBuffer.wrap(header, 22, 2).order(ByteOrder.LITTLE_ENDIAN).getShort();
                    }

                    // 计算总的数据大小（排除每个文件的44字节头部）
                    totalDataSize += (file.length() - 44);
                } catch (IOException e) {
                    callback.onError(e.getMessage());
                    return false;
                }
            }

            // 写入WAV头部信息
            callback.onStatus(Status.CONCATENATING);
            writeWavHeader(fos, totalDataSize, sampleRate, channels);

            // 第二遍：写入音频数据
            for (int i = 0; i < inputFiles.length; i++) {
                callback.onProgress(inputFiles.length, i + 1);
                String inputFile = inputFiles[i];
                File file = new File(OUTPUT_PATH, inputFile);

                try (FileInputStream fis = new FileInputStream(file)) {
                    // 跳过WAV头部
                    fis.skip(44);

                    byte[] buffer = new byte[4096];  // 定义一个4KB缓冲区
                    int bytesRead;
                    // 分块读取数据并写入输出文件
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    callback.onError(e.getMessage());
                    return false;
                }
            }
        } catch (IOException e) {
            callback.onError(e.getMessage());
            return false;
        }
        return true;
    }

    private void clearTempFiles(String[] inputFiles, Callback callback) {
        callback.onStatus(Status.CLEAR);
        for (int i = 0; i < inputFiles.length; i++) {
            String inputFile = inputFiles[i];
            File file = new File(OUTPUT_PATH, inputFile);
            file.delete();
        }
    }

    public void start(String text, Callback callback) {
        callback.onStart();
        double[] frequencies = getFrequencys(text);
        String[] fileNames = new String[frequencies.length];
        for (int i = 0; i < frequencies.length; i++) {
            fileNames[i] = "tone_" + i + ".wav";
            callback.onProgress(frequencies.length, i+1);
            if (!generateTone(frequencies[i], fileNames[i], callback)) return;
        }
        if (!mergeWavFiles(fileNames, callback)) return;
        callback.onStatus(Status.CLEAR);
        clearTempFiles(fileNames, callback);
        callback.onSuccess(OUTPUT_PATH + "/" + OUTPUT_FILE_NAME + ".wav");
        callback.onStatus(Status.FINISHED);
    }
}