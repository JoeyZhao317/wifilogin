package com.shixun.wifisdk.service;

import com.shixun.wifisdk.core.OnAccountDetectListener;
import com.shixun.wifisdk.core.OnWifiScanListener;
import com.shixun.wifisdk.events.DetectAccountEvent;
import com.shixun.wifisdk.plugin.AccountManager;
import com.shixun.wifisdk.plugin.AccountPluginBase;

import static com.shixun.wifisdk.util.Constants.EVENTBUS;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.List;

public class WifiService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private OnAccountDetectListener mDetectListener;

    public class LocalBinder extends Binder {
        public WifiService getService() {
            return WifiService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EVENTBUS.register(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void setOnWifiScanListener(OnWifiScanListener listener, long interval) {
    }

    public void startScanWifi() {
    }

    public void onEventBackgroundThread(DetectAccountEvent event) {
        AccountManager manager = AccountManager.getInstance();
        List<AccountPluginBase> accounts = manager.getAllAccounts();
        for (AccountPluginBase account : accounts) {
            if (account.checkWifiType()) {
                EVENTBUS.post(account);
                return;
            }
        }
    }

    public void onEventMainThread(AccountPluginBase account) {
        if (mDetectListener != null) {
            mDetectListener.OnAccountDetectFinish(account);
        }
    }

    public void detectAccount(OnAccountDetectListener listener) {
        mDetectListener = listener;
        EVENTBUS.post(new DetectAccountEvent());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EVENTBUS.unregister(this);
    }
}
