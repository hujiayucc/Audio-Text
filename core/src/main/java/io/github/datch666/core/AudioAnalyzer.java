package io.github.datch666.core;

import android.util.Log;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class AudioAnalyzer {

    public List<Double> getFrequenciesFromFile(String filePath, Sample sampleRate) {
        List<Double> frequencies = new ArrayList<>();
        FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
        int numSamples = (int) (sampleRate.getValue() * 0.5);
        int paddedSize = nextPowerOfTwo(numSamples);
        byte[] buffer = new byte[numSamples * 2];

        try (FileInputStream fis = new FileInputStream(filePath)) {
            fis.skip(44); // Skip the WAV file header

            while (fis.read(buffer) != -1) {
                ShortBuffer shortBuffer = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
                double[] audioData = new double[paddedSize];
                for (int i = 0; i < numSamples; i++) {
                    audioData[i] = shortBuffer.get(i);
                }

                Complex[] complexTransformed = fft.transform(audioData, TransformType.FORWARD);
                double dominantFrequency = findDominantFrequency(complexTransformed, sampleRate.getValue());
                frequencies.add(dominantFrequency);
                System.out.println("Dominant frequency: " + dominantFrequency);
            }
        } catch (IOException e) {
            Log.e("AudioAnalyzer", "Error reading file: " + e.getMessage(), e);
        }

        return frequencies;
    }

    private double findDominantFrequency(Complex[] complexData, int sampleRate) {
        double maxMagnitude = Double.MIN_VALUE;
        double indexMaxMagnitude = -1.0;

        for (int i = 0; i < complexData.length / 2; i++) {
            double magnitude = complexData[i].abs();
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude;
                indexMaxMagnitude = i;
            }
        }

        return indexMaxMagnitude * sampleRate / complexData.length;
    }

    private int nextPowerOfTwo(int number) {
        int power = 1;
        while (power < number) {
            power *= 2;
        }
        return power;
    }
}