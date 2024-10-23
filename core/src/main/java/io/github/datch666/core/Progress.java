package io.github.datch666.core;

public enum Progress {
    /** 清除状态 */
    CLEAR(0),
    /** 生成中 */
    GENERATING(1),
    /** 合并中 */
    CONCATENATING(2),
    /** 完成 */
    FINISHED(3),
    /** 错误 */
    ERROR(4);

    private final int value;

    Progress(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
