package io.github.datch666.core;

/** 音频采样率 */
public enum Sample {
    /** 标准 */
    STANDARD(44100),
    /** 高质量 */
    HIGH(48000),
    /** 超高 */
    VERY_HIGH(96000);
    private final int value;

    Sample(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
