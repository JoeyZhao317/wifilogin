package com.shixun.wifisdk;

import com.shixun.wifisdk.core.OnAccountDetectListener;
import com.shixun.wifisdk.plugin.AccountPluginBase;
import com.shixun.wifisdk.service.WifiService;
import com.shixun.wifisdk.service.WifiService.LocalBinder;
import com.shixun.wifisdk.util.WifiLog;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.IBinder;

public class WifiSDKManager {
    private static final String TAG = WifiSDKManager.class.getSimpleName();
    private static WifiSDKManager mInstance;
    private static WifiService mWifiService;
    private Context mContext;
    private boolean mBound;

    public static void initlize(Context context) {
        if (mInstance == null) {
            mInstance = new WifiSDKManager(context);
        }
    }

    private WifiSDKManager(Context context) {
        mContext = context;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get
            // LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mWifiService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    public void detectWifiAccount() {
        if (mBound) {
            mWifiService.detectAccount(new OnAccountDetectListener() {
                @Override
                public void OnAccountDetectFinish(AccountPluginBase account) {

                }
            });
        }
    }

    public static WifiManager getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("Initilze first");
        }
        return getInstance();
    }
}
