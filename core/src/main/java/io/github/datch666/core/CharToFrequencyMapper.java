package io.github.datch666.core;

import java.util.HashMap;
import java.util.Map;

public class CharToFrequencyMapper {
    private static final Map<Character, Integer> charToMIDImap = new HashMap<>();

    // 初始化字符到 MIDI 值的映射表
    static {
        for (char c = 0; c < 65535; c++) {
            charToMIDImap.put(c, 60 + (c % 12)); // 按12个半音循环映射
        }
    }

    // 将 MIDI 值转换为频率
    private static double midiToFrequency(int midi) {
        return 440 * Math.pow(2, (midi - 69) / 12.0);
    }

    /**
     * 获取字符对应的频率
     * 此方法通过查找字符到MIDI音高的映射来获取字符对应的频率如果字符没有对应的MIDI音高，
     * 则使用A4（MIDI音高69）作为默认值此方法不解释具体映射关系和频率计算的实现细节，
     * 而是关注于如何使用这些映射和函数来达到目的
     *
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