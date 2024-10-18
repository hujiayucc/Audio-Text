package io.github.datch666.core;

import java.util.HashMap;
import java.util.Map;

public class CharToFrequencyMapper {
    private static final Map<Character, Integer> charToMIDImap = new HashMap<>();

    // 初始化字符到 MIDI 值的映射表
    static {
        // 英文字母映射 (示例：A -> MIDI 69)
        for (char c = 'A'; c <= 'Z'; c++) {
            charToMIDImap.put(c, 69 + (c - 'A'));
        }
        for (char c = 'a'; c <= 'z'; c++) {
            charToMIDImap.put(c, 69 + (c - 'a'));
        }

        // 数字映射 (示例：0 -> MIDI 48，对应C3)
        for (char c = '0'; c <= '9'; c++) {
            charToMIDImap.put(c, 48 + (c - '0'));
        }

        // 标点符号映射（使用适当的范围, 例如从 MIDI 36 开始）
        charToMIDImap.put('.', 36);  // 句号 -> C2
        charToMIDImap.put(',', 37);  // 逗号 -> C#2
        charToMIDImap.put('!', 38);  // 感叹号 -> D2
        charToMIDImap.put('?', 39);  // 问号 -> D#2
        charToMIDImap.put(';', 40);  // 分号 -> E2
        charToMIDImap.put(':', 41);  // 冒号 -> F2
        charToMIDImap.put('-', 42);  // 连字符 -> F#2
        charToMIDImap.put('_', 43);  // 下划线 -> G2
        charToMIDImap.put('(', 44);  // 左括号 -> G#2
        charToMIDImap.put(')', 45);  // 右括号 -> A2
        charToMIDImap.put('\'', 46); // 单引号 -> A#2
        charToMIDImap.put('"', 47);  // 双引号 -> B2

        // 中文字符映射示例（根据 Unicode 范围分段）
        for (char c = '\u4E00'; c <= '\u9FFF'; c++) { // 常用汉字区
            charToMIDImap.put(c, 60 + (c % 12)); // 按12个半音循环映射
        }
    }

    // 将 MIDI 值转换为频率
    private static double midiToFrequency(int midi) {
        return 440 * Math.pow(2, (midi - 69) / 12.0);
    }

    /**
     * 获取字符对应的频率
     *
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

    public static void main(String[] args) {
        // 测试示例
        String text = "Hello 123, 世界!";
        for (char c : text.toCharArray()) {
            double frequency = getFrequency(c);
            System.out.printf("字符: %c, 频率: %.2f Hz\n", c, frequency);
        }
    }
}