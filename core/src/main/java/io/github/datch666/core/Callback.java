package io.github.datch666.core;

public interface Callback {
    /** Called when the task is started. */
    void onStart();
    /** Called when the task is completed successfully. */
    void onSuccess(String path);
    /** Called when an error occurs during the task. */
    void onError(String errorMessage);
    /** Called to report the progress of the task. */
    void onProgress(Progress progress);
}
