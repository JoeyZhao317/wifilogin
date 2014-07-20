package com.shixun.wifisdk.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager {
    private static AccountManager mInstance = new AccountManager();
    private Map<String, AccountPluginBase> mAccountMap = new ConcurrentHashMap<String, AccountPluginBase>();
    private List<AccountPluginBase> mAccounts = Collections
            .synchronizedList(new ArrayList<AccountPluginBase>());

    private AccountManager() {
        loadAllPlugins();
    }

    private void loadAllPlugins() {
        ShixunAccountPlugin shixunPlugin = new ShixunAccountPlugin();
        mAccounts.add(shixunPlugin);
        mAccountMap.put(ShixunAccountPlugin.ACCOUNT, shixunPlugin);
    }

    public static AccountManager getInstance() {
        return mInstance;
    }

    public List<AccountPluginBase> getAllAccounts() {
        return mAccounts;
    }
}
