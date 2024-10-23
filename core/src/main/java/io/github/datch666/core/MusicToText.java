package io.github.datch666.core;

import java.util.List;

public class MusicToText {

    private static final String TAG = "MusicToText";

    private final Sample SAMPLE_RATE;

    public MusicToText(Sample sample) {
        SAMPLE_RATE = sample;
    }

    public String decode(String fileName) {
        AudioAnalyzer analyzer = new AudioAnalyzer();
        List<Double> frequencies = analyzer.getFrequenciesFromFile(fileName, SAMPLE_RATE);
        return FrequencyToCharMapper.frequenciesToText(frequencies);
    }
}
