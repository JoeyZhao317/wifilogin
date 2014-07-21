package com.shixun.wifisdk;

import com.shixun.wifisdk.core.OnAccountDetectListener;
import com.shixun.wifisdk.plugin.AccountPluginBase;
import com.shixun.wifisdk.service.WifiService;
import com.shixun.wifisdk.service.WifiService.LocalBinder;
import com.shixun.wifisdk.util.WifiLog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import hugo.weaving.DebugLog;

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
        Intent intent = new Intent(mContext, WifiService.class);
        if(mContext.bindService(intent,mConnection,Context.BIND_AUTO_CREATE) ) {
            WifiLog.e(TAG, "Bind Service failed!");
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override @DebugLog
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get
            // LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mWifiService = binder.getService();
            mBound = true;
        }

        @Override @DebugLog
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

    public static WifiSDKManager getInstance() {
        if (mInstance == null) {
            throw new RuntimeException("Initilze first");
        }
        return mInstance;
    }
}
