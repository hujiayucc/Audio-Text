package io.github.datch666.core;

import java.util.HashMap;
import java.util.Map;

public class CharToFrequencyMapper {
    public static final Map<Character, Integer> charToMIDImap = new HashMap<>();

    static {
        for (char c = 32; c <= 126; c++) {
            charToMIDImap.put(c, 36 + (c - 32));
        }
    }

    // 将 MIDI 值转换为频率
    public static double midiToFrequency(int midi) {
        return 440 * Math.pow(2, (midi - 69) / 12.0);
    }

    /**
     * 获取字符对应的频率
     * @param c 要查询频率的字符
     * @return 字符对应的频率
     */
    public static double getFrequency(char c) {
        // 将字符转换为对应的MIDI音高，如果转换失败，则使用A4（MIDI音高69）作为默认值
        //noinspection DataFlowIssue
        int midi = charToMIDImap.getOrDefault(c, 69); // 默认A4
        // 根据MIDI音高获取对应的频率
        return midiToFrequency(midi);
    }
}