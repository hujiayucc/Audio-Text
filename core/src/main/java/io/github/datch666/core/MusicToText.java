package io.github.datch666.core;

import java.util.List;

public class MusicToText {

    private static final String TAG = "MusicToText";

    public String decode(String fileName) {
        AudioAnalyzer analyzer = new AudioAnalyzer();
        List<Double> frequencies = null;
        String text = "Invalid file.";
        for (Sample SAMPLE_RATE : Sample.values()) {
            frequencies = analyzer.getFrequenciesFromFile(fileName, SAMPLE_RATE);
            String str1 = FrequencyToCharMapper.frequenciesToText(frequencies);
            if (!str1.isBlank()) {
                text = str1;
                break;
            }
        }
        return text;
    }
}
