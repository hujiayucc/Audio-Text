package io.github.datch666.core;

import static io.github.datch666.core.CharToFrequencyMapper.charToMIDImap;

import java.util.List;
import java.util.Map;

public class FrequencyToCharMapper {
    private static int frequencyToMidi(double frequency) {
        double midi = 69 + 12 * Math.log(frequency / 440.0) / Math.log(2);
        return (int) Math.round(midi);
    }

    private static char midiToChar(int value) {
        for (Map.Entry<Character, Integer> entry : charToMIDImap.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return '?'; // 如果找不到，返回 ?
    }


    private static char frequencyToChar(double frequency) {
        int midi = frequencyToMidi(frequency);
        return midiToChar(midi);
    }

    public static String frequenciesToText(List<Double> frequencies) {
        StringBuilder unicodeText = new StringBuilder();
        for (double frequency : frequencies) {
            unicodeText.append(frequencyToChar(frequency));
        }
        return TextUtils.unicodeToString(unicodeText.toString());
    }
}
