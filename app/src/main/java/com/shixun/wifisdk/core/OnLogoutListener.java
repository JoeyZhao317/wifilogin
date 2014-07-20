package com.shixun.wifisdk.core;

public interface OnLogoutListener {
    void onComplete();
    void onFailed(String msg, ErrorType error);
    void onSuccessful();
}
