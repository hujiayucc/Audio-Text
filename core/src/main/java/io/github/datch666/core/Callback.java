package io.github.datch666.core;

public interface Callback {
    void onSuccess(String path);
    void onError(String errorMessage);
}
