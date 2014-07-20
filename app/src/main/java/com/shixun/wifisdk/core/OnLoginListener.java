package com.shixun.wifisdk.core;

public interface OnLoginListener {

    void onComplete();
    void onFailed(String msg, ErrorType error);
    void onSuccessful();
}
