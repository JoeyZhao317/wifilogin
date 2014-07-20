package com.shixun.wifisdk.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class Utils {
    private static final String TAG = "Utils";
    private static final int MIN_RSSI = -100;
    private static final int MAX_RSSI = -50;
    private static int mDisplayWidth = -1;
    private static int mDisplayHeight = -1;
    private static final String LAST_SYNC_SERVER_TIME_KEY = "sync_server_time";
    private static final String IS_FIRST_LAUNCH_KEY = "is_first_launch";
    private static final String PROFILE_ICON_URL_KEY = "profile_icon_url";
    // private static final String LAST_UPDATE_HOT_WORD_KEY =
    // "update_hot_word_time";

    @SuppressWarnings("boxing")
    private final static ArrayList<Integer> channelsFrequency = new ArrayList<Integer>(
            Arrays.asList(0, 2412, 2417, 2422, 2427, 2432, 2437, 2442, 2447,
                    2452, 2457, 2462, 2467, 2472, 2484));

    public static Integer getFrequencyFromChannel(int channel) {
        return channelsFrequency.get(channel);
    }

    public static int getChannelFromFrequency(int frequency) {
        return channelsFrequency.indexOf(Integer.valueOf(frequency));
    }

    public static String getUserName(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.USER_NAME_KEY, null);
    }

    public static String getPassword(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.PASSWORD_KEY, null);
    }

    public static boolean setPref(Context context, String key, String newValue) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(key, newValue);
        return editor.commit();
    }

    public static void setUserName(Context context, String name) {
        setPref(context, Constants.USER_NAME_KEY, name);
    }

    public static void setPassword(Context context, String password) {
        setPref(context, Constants.PASSWORD_KEY, password);
    }

    public static void clearAccount(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.remove(Constants.USER_NAME_KEY);
        editor.remove(Constants.PASSWORD_KEY);
        editor.commit();
    }

    public static boolean getEnableAutoLogin(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.AUTO_LOGIN_KEY, true);
    }

    public static boolean getEnableHotword(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.ENABLE_HOT_WORD, true);
    }

    public static boolean getEnableNotification(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.ENABLE_NOTIFICATION_KEY, true);
    }

    public static boolean getEnableShowChannel(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.ENABLE_SHOW_CHANNEL, false);
    }

    public static boolean getEnableShowRssi(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.ENABLE_SHOW_RSSI, false);
    }

    public static boolean getEnableServiceNotification(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.ENABLE_SERVICE_NOTIFICATION, true);
    }

    public static void setLoginStatus(Context context, int status) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putInt(Constants.LOGIN_STATUS_KEY, status);
        editor.commit();
    }

    public static boolean getIsServiceChangeStatus(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(Constants.IS_SERVICE_UPDATE, false);
    }

    public static void setIsServiceUpdate(Context context, boolean update) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putBoolean(Constants.IS_SERVICE_UPDATE, update);
        editor.commit();
    }

    public static int getLoginStatus(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getInt(Constants.LOGIN_STATUS_KEY, 1);
    }

    public static boolean check591Server(Context context) {
        WifiLog.d(TAG, "Check591 Server");
        HttpURLConnection urlc = null;
        boolean result = false;

        try {
            WifiLog.d(TAG, "server:" + Utils.getGateway(context));
            URL url = new URL("http://" + Utils.getGateway(context));
            WifiLog.d(TAG, "URL: " + url.toString());
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("http.keepAlive", "false");
            urlc.setConnectTimeout(10000);
            urlc.setReadTimeout(10000);
            urlc.setRequestMethod("GET");
            urlc.setDoInput(true);
            urlc.connect();

            int responeCode = urlc.getResponseCode();
            if (responeCode == HttpURLConnection.HTTP_OK
                    || responeCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                WifiLog.d(TAG, "Find 591WiFi Server!!");
                result = true;
            }
        } catch (MalformedURLException e) {
            WifiLog.d(TAG, e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            WifiLog.d(TAG, e.toString());
            e.printStackTrace();
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
        return result;
    }

    public static int checkTestUrl(Context context) {
        WifiLog.d(TAG, "Check baidu");
        HttpURLConnection urlc = null;
        InputStream in = null;
        int result = Constants.CANNOT_CONNECT;
        try {
            URL url = new URL("http://" + Constants.TEST_SHORT_BAIDU
                    + "?portalfrom=app");
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(10000);
            urlc.setReadTimeout(10000);
            urlc.setRequestMethod("GET");
            urlc.setInstanceFollowRedirects(false);
            urlc.setDoInput(true);
            urlc.connect();
            int responeCode = urlc.getResponseCode();
            if (responeCode == HttpURLConnection.HTTP_OK) {
                Map<String, List<String>> headers = urlc.getHeaderFields();
                for (Entry<String, List<String>> item : headers.entrySet()) {
                    WifiLog.d(TAG, item.getKey() + item.getValue().get(0));
                }
                in = urlc.getInputStream();
                String s = Utils.inputStream2String(in);
                WifiLog.d(TAG, "check result: " + s);
                if (s.contains(Constants.TEST_KEYWORD)) {
                    WifiLog.d(TAG, "Have connected");
                    result = Constants.CONNECTED;
                } else {
                    WifiLog.d(TAG, "NO 571WIFI Server");
                    result = Constants.NOT_FIND_SERVER;
                }
            }
            if (responeCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = urlc.getHeaderField("Location");
                WifiLog.d(TAG, "Location: " + location);
                if (!TextUtils.isEmpty(location)) {
                    if (location.contains(Constants.WIFI_SERVER_KEY)) {
                        WifiLog.d(TAG, "Find 591 Server!!!");
                        result = Constants.HAVE_LOGOUT;
                        String gateway = getGateway(location);
                        WifiLog.d(TAG, "gateway: " + gateway);
                        if (gateway != null) {
                            Utils.setGateway(context, gateway);
                        }
                    } else {
                        result = Constants.NOT_FIND_SERVER;
                    }
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlc != null) {
                urlc.disconnect();
            }
        }
        return result;
    }

    public synchronized static String executeReq(String hostname) {
        WifiLog.d(TAG, "doInBackground: Start");
        HttpURLConnection urlc = null;
        InputStream in = null;
        String result = null;
        try {
            URL url = new URL("http://" + Constants.TEST_URL);
            urlc = (HttpURLConnection) url.openConnection();
            WifiLog.d(TAG, "doInBackground: CheckPoint 0");
            urlc.setConnectTimeout(10000);
            urlc.setReadTimeout(10000);
            urlc.setRequestMethod("GET");
            urlc.setDoInput(true);
            urlc.connect();

            int responeCode = urlc.getResponseCode();
            if (responeCode == HttpURLConnection.HTTP_OK) {
                in = urlc.getInputStream();
                result = Utils.inputStream2String(in);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlc != null) {
                urlc.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String inputStream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    public static boolean isHasWifiConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static long getLastLoginTime(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getLong(Constants.LAST_LOGIN_TIME, -1);
    }

    public static void setLastLoginTime(Context context, long time) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putLong(Constants.LAST_LOGIN_TIME, time);
        editor.commit();
    }

    public static long getDataTarfficOfLogin(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getLong(Constants.DATA_TRAFFIC, -1);
    }

    public static void setDataTrafficWhenLogin(Context context, long data) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putLong(Constants.DATA_TRAFFIC, data);
        editor.commit();
    }

    public static String getGateway(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.GATE_CONFIGURE_KEY,
                Constants.DEFAULT_WIFI_SERVER);
    }

    public static void setGateway(Context context, String gateway) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.GATE_CONFIGURE_KEY, gateway);
        editor.commit();
    }

    public static String getGateway(String address) {
        if (address.contains(Constants.PARAMTER_LOGINURL)) {
            address = address.substring(address
                    .indexOf(Constants.PARAMTER_LOGINURL)
                    + Constants.PARAMTER_LOGINURL.length());
            try {
                address = URLDecoder.decode(address, "utf-8");
                WifiLog.d(TAG, "address: " + address);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
        return getGatewayFromNewUrl(address);
    }

    private static String getGatewayFromNewUrl(String address) {
        StringBuilder builder = new StringBuilder();
        String ip = null;
        String port = null;
        try {
            URI uri = new URI(address);
            List<NameValuePair> values = URLEncodedUtils.parse(uri, "utf-8");
            for (NameValuePair item : values) {
                if (TextUtils.equals(item.getName(), Constants.PARAMTER_IP)) {
                    ip = item.getValue();
                }
                if (TextUtils.equals(item.getName(), Constants.PARAMTER_PORT)) {
                    port = item.getValue();
                }
            }
            builder.append(ip).append(":").append(port);
        } catch (URISyntaxException e) {
            WifiLog.d(TAG, "Parse Gateway Error!");
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static String getErrorMessage(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.LOGIN_ERROR_MESSAGE,
                Constants.LOGIN_ERROR_MESSAGE);
    }

    public static void setErrorMessage(Context context, String gateway) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.LOGIN_ERROR_MESSAGE, gateway);
        editor.commit();
    }

    public static String getUpdateUrl(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.UPDATE_APK_URL,
                Constants.WRONG_UPDATE_URL);
    }

    public static void setUpdateUrl(Context context, String updateUrl) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.UPDATE_APK_URL, updateUrl);
        editor.commit();
    }

    public static int getFilterMode(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getInt(Constants.FILTER_MODE_KEY,
                Constants.FILTER_MODE_VISIBLE);
    }

    public static void setFilterMode(Context context, int mode) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putInt(Constants.FILTER_MODE_KEY, mode);
        editor.commit();
    }

    public static String getUserAddress(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.MAIL_ADDRESS, null);
    }

    public static void setUserAddress(Context context, String updateUrl) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.MAIL_ADDRESS, updateUrl);
        editor.commit();
    }

    public static int getPercentageOfdBm(int dBm) {
        int result = 0;
        if (dBm > MAX_RSSI) {
            result = 100;
        } else if (dBm < MIN_RSSI) {
            result = 0;
        } else {
            result = 2 * (dBm + 100);
        }
        return result;
    }
//
//    public static int getResIdofStatus(int status) {
//        int resId = R.string.wifi_login_unknow_status;
//
//        switch (status) {
//            case Constants.CANNOT_CONNECT:
//                resId = R.string.wifi_no_connection_satus;
//                break;
//            case Constants.HAVE_LOGIN:
//                resId = R.string.wifi_login_successfully_status;
//                break;
//            case Constants.HAVE_LOGOUT:
//                resId = R.string.wifi_logout_successfully_status;
//                break;
//            case Constants.NOT_FIND_SERVER:
//                resId = R.string.wifi_cannot_login_status;
//                break;
//            case Constants.NO_WIFI:
//                resId = R.string.wifi_no_wifi_status;
//                break;
//            case Constants.NOT_NEED_LOGIN:
//                resId = R.string.wifi_not_need_login_status;
//                break;
//            case Constants.LOGIN_FALLURE:
//                resId = R.string.wifi_login_fail_status;
//                break;
//            case Constants.STATUS_UNKOWN:
//                resId = R.string.wifi_login_unknow_status;
//                break;
//            default:
//                break;
//        }
//        return resId;
//    }

    public static String getOperatorWifiName(String ssid) {
        if (TextUtils.equals(ssid, "CMCC")) {
            return Constants.CMCC;
        } else if (TextUtils.equals(ssid, "ChinaNet")) {
            return Constants.CHINANET;
        } else if (TextUtils.equals(ssid, "ChinaUnicom")) {
            return Constants.CHINA_UNICOM;
        } else {
            return ssid;
        }
    }

    public static void installUpdateApk(Context context, String fullPath) {
        File apkfile = new File(fullPath);
        if (!apkfile.exists()) {
            return;
        }
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        context.startActivity(installIntent);
    }

    public static boolean checkAndCreatePath() {
        boolean result = false;
        File file = new File(Environment.getExternalStorageDirectory()
                + Constants.SAVE_PATH);
        if (!file.exists()) {
            result = file.mkdirs();
        }
        result = file.canWrite();
        return result;
    }

    public static String getAppList(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.APP_LIST, null);
    }

    public static void setAppList(Context context, String applist) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.APP_LIST, applist);
        editor.commit();
    }

    @SuppressWarnings("deprecation")
    public static int getActivityDisplayWidth(Activity activity) {
        if (mDisplayWidth == -1) {
            WindowManager wm = activity.getWindowManager();
            Display display = wm.getDefaultDisplay();
            mDisplayWidth = display.getWidth();
        }
        return mDisplayWidth;
    }

    @SuppressWarnings("deprecation")
    public static int getActivityDisplayHeight(Activity activity) {
        if (mDisplayHeight == -1) {
            WindowManager wm = activity.getWindowManager();
            Display display = wm.getDefaultDisplay();
            mDisplayHeight = display.getHeight();
        }
        return mDisplayHeight;
    }

    public static String getHotwordList(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.HOT_WORD, null);
    }

    public static void setHotwordList(Context context, String hotwordList) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.HOT_WORD, hotwordList);
        editor.commit();
    }

    public static long getHotwordUpdateTime(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getLong(Constants.HOT_WORD_TIME, 0);
    }

    public static void setHotwordUpdateTime(Context context, long updatTime) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putLong(Constants.HOT_WORD_TIME, updatTime);
        editor.commit();
    }

    public static void setLastLatitdue(Context context, double lastLat) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putFloat(Constants.LAST_LAT, (float) lastLat);
        editor.commit();
    }

    public static double getLastLatitude(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return (double) sPref.getFloat(Constants.LAST_LAT, 0);
    }

    public static void setLastLongitude(Context context, double lastLat) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putFloat(Constants.LAST_LON, (float) lastLat);
        editor.commit();
    }

    public static double getLastLongitude(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return (double) sPref.getFloat(Constants.LAST_LON, 0);
    }

    public static void setLastSSID(Context context, String ssid) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.LAST_AP_SSID, ssid);
        editor.commit();
    }

    public static String getLastSSID(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.LAST_AP_SSID, "");
    }

    public static void setFirstLaunch(Context context, boolean isFirstLauncher) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putBoolean(IS_FIRST_LAUNCH_KEY, isFirstLauncher);
        editor.commit();
    }

    public static boolean isFirtLaunch(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getBoolean(IS_FIRST_LAUNCH_KEY, true);
    }

    public static void setLastPassword(Context context, String ssid) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.LAST_AP_PASSWORD, ssid);
        editor.commit();
    }

    public static String getLastPassword(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.LAST_AP_PASSWORD, "");
    }

    public static void setProfileURl(Context context, String url) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(PROFILE_ICON_URL_KEY, url);
        editor.commit();
    }

    public static String getProfileUrl(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(PROFILE_ICON_URL_KEY, "");
    }

    public static void setLastBSSID(Context context, String ssid) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putString(Constants.LAST_AP_BSSID, ssid);
        editor.commit();
    }

    public static long getLastAPSyncTime(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getLong(Constants.LAST_AP_SYNC_DATE, 0);
    }

    public static void setLastAPSyncTime(Context context, long lastUpdateTime) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putLong(Constants.LAST_AP_SYNC_DATE, lastUpdateTime);
        editor.commit();
    }

    public static String getLastBSSID(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getString(Constants.LAST_AP_BSSID, "");
    }

    public static String getLocationUrl(double latitude, double longititude) {
        return Constants.GETCODER_URL + latitude + "," + longititude;
    }

    public static long getLastSyncTime(Context context) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sPref.getLong(LAST_SYNC_SERVER_TIME_KEY, 0);
    }

    public static void setLastSyncTime(Context context, long syncTime) {
        SharedPreferences sPref = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sPref.edit();
        editor.putLong(LAST_SYNC_SERVER_TIME_KEY, syncTime);
        editor.commit();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp,
            final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static byte[] readFromFile(String fileName, int offset, int len) {
        if (fileName == null) {
            return null;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            Log.i(TAG, "readFromFile: file not found");
            return null;
        }

        if (len == -1) {
            len = (int) file.length();
        }

        Log.d(TAG, "readFromFile : offset = " + offset + " len = " + len
                + " offset + len = " + (offset + len));

        if (offset < 0) {
            Log.e(TAG, "readFromFile invalid offset:" + offset);
            return null;
        }
        if (len <= 0) {
            Log.e(TAG, "readFromFile invalid len:" + len);
            return null;
        }
        if (offset + len > (int) file.length()) {
            Log.e(TAG, "readFromFile invalid file len:" + file.length());
            return null;
        }

        byte[] b = null;
        try {
            RandomAccessFile in = new RandomAccessFile(fileName, "r");
            b = new byte[len];
            in.seek(offset);
            in.readFully(b);
            in.close();

        } catch (Exception e) {
            Log.e(TAG, "readFromFile : errMsg = " + e.getMessage());
            e.printStackTrace();
        }
        return b;
    }

    public static String formatTime(long t) {
        long now = System.currentTimeMillis();
        long time = now - t;
        if (time <= 1000 * 60) {
            return "刚刚";
        } else if (time < 1000 * 60 * 2) {
            return "2分钟前";
        } else if (time < 1000 * 60 * 5) {
            return "5分钟前";
        } else if (time < 1000 * 60 * 10) {
            return "10分钟前";
        } else if (time < 1000 * 60 * 30) {
            return "半小时前";
        } else if (time < 1000 * 60 * 60) {
            return "1小时前";
        } else if (time < 1000 * 60 * 60 * 2) {
            return "2小时前";
        } else if (time < 1000 * 60 * 60 * 6) {
            return "半天前";
        } else if (time < 1000 * 60 * 60 * 24) {
            return "1天前";
        } else if (time < 1000 * 60 * 60 * 24 * 2) {
            return "2天前";
        } else if (time < 1000 * 60 * 60 * 24 * 5) {
            return "5天前";
        } else if (time < 1000 * 60 * 60 * 24 * 7) {
            return "一周前";
        }
        return convertDateToString(t);
    }

    public static String convertDateToString(long dateInMils) {
        SimpleDateFormat parseDate = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        Date date = new Date(dateInMils);
        return parseDate.format(date);
    }

    public static byte[] getBytesFromDrawableRes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @SuppressLint("SimpleDateFormat")
    public static String getEsFormatDateTime(final long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm");
        return dateFormat.format(new Date(calendar.getTimeInMillis()));
    }

    public static String getDefaultUserName(String address) {
        String suffix = address.substring(address.length() - 4);
        return Constants.ANONYMOUS_USER + suffix;
    }

    public static String getDefaultAgentName(String address) {
        String suffix = address.substring(address.length() - 4);
        return Constants.ANONYMOUS_AGENT + suffix;
    }

    public static String encoder(String original) {
        String afterEncoder = original;
        try {
            if (TextUtils.isEmpty(original)) {
                afterEncoder = "";
            } else {
                afterEncoder = URLEncoder.encode(original, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            WifiLog.d(TAG, "Encode error: " + e.getMessage());
            e.printStackTrace();
        }
        return afterEncoder;
    }

    public static String decoder(String codeString) {
        String original = codeString;
        try {
            if (TextUtils.isEmpty(codeString)) {
                original = "";
            } else {
                original = URLDecoder.decode(codeString, "utf-8");
            }
        } catch (UnsupportedEncodingException e) {
            WifiLog.d(TAG, "Decode error: " + e.getMessage());
            e.printStackTrace();
        }
        return original;
    }
}
