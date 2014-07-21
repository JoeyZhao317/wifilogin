package com.shixun.wifisdk.plugin;

import com.shixun.wifisdk.core.AccountInfo;
import com.shixun.wifisdk.core.OnLoginListener;
import com.shixun.wifisdk.core.WifiEntry;

import hugo.weaving.DebugLog;

public class ShixunAccountPlugin extends AccountPluginBase {
    public final static String ACCOUNT = "Shixun_account";

    @Override
    public String getWifiType(OnLoginListener listener) {
        return ACCOUNT;
    }

    @Override
    public void login(final OnLoginListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void logout() {
        // TODO Auto-generated method stub

    }

    @Override
    public AccountInfo getAccountInfo(WifiEntry entry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isLogined() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override @DebugLog
    public boolean checkWifiType() {
        return false;
    }
}