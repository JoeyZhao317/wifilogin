package com.shixun.wifisdk.core;

import com.shixun.wifisdk.plugin.AccountPluginBase;

/**
 * OnAccountDetectListener is used for detecting AccountType
 * @author yizhao
 *
 */
public interface OnAccountDetectListener {
    public void OnAccountDetectFinish(AccountPluginBase account);
}
