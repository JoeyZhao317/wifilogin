package com.shixun.wifisdk.plugin;

import com.shixun.wifisdk.core.AccountInfo;
import com.shixun.wifisdk.core.OnLoginListener;
import com.shixun.wifisdk.core.WifiEntry;

abstract public class AccountPluginBase {

   public boolean checkWifiType() {
       return false;
   }

   abstract public String getWifiType(OnLoginListener listener);
   abstract public void login(OnLoginListener listener);
   abstract public void logout();
   abstract public AccountInfo getAccountInfo(WifiEntry entry);
   abstract public boolean isLogined();
}
