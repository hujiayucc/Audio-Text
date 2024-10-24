package io.github.datch666.core;

public interface Callback {
    /** 开始 */
    void onStart();
    /** 成功 */
    void onSuccess(String fileName);
    /** 发生错误 */
    void onError(String errorMessage);
    /** 状态 */
    void onStatus(Status status);
    /** 进度 */
    void onProgress(int total, int progress);
}
