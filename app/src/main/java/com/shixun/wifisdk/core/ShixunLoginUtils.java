package com.shixun.wifisdk.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import android.content.Context;
import android.text.TextUtils;

import com.shixun.wifisdk.util.Constants;
import com.shixun.wifisdk.util.Utils;
import com.shixun.wifisdk.util.WifiLog;

public class ShixunLoginUtils {

    private static final String TAG = "LoginUtil";

    private static final String CHALLENGE = "GET / HTTP/1.0\nUser-Agent: Wget/1.12(cygwin)\r\nAccept: */*\r\nHost: 1.1.1.1\r\nConnection: Close\r\n\r\n";
    private static final String LOGIN_FMT = "GET http://%s:%d/logon?username=%s&response=%s&userurl=%s HTTP/1.0\r\nUser-Agent: Wget/1.12(cygwin)\r\nAccept: */*\r\nHost: %s:%d\r\nConnection: Close\r\n\r\n";
    private static final String LOGOUT_FMT = "GET /logoff HTTP/1.0\nUser-Agent: Wget/1.12(cygwin)\r\nAccept: */*\r\nHost: %s\r\nConnection: Close\r\n\r\n";
    // private static final String PRELOGIN = "GET /prelogin HTTP/1.0";

    private static final int CACHE_LEN = 4096;
    private byte[] CACHE = new byte[CACHE_LEN];
    private String mServer;
    private int mPort;
    private Context mContext;
    private static ShixunLoginUtils sInstance;

    private ShixunLoginUtils(Context context) {
        mContext = context.getApplicationContext();
    }

    public static synchronized ShixunLoginUtils getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ShixunLoginUtils(context);
        }

        return sInstance;
    }

    private String talkWithServer(String host, int port, String content) {
        InputStream is = null;
        OutputStream os = null;
        Socket socket = null;
        try {
            socket = new Socket();
            InetSocketAddress add = new InetSocketAddress(host, port);
            // Socket socket = new Socket(host, port);
            socket.connect(add, 10000);
            socket.setSoTimeout(10000);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            int contentLen = content.length();
            // byte[] isbyte = new Byte[contentLen];
            // content.getBytes(0, contentLen, isbyte, 0);
            byte[] isbyte = content.getBytes();
            os.write(isbyte, 0, contentLen);
            os.flush();
            int recv = 0, received = 0;
            recv = is.read(CACHE, received, CACHE_LEN);
            if (recv != -1) {
                CACHE[recv] = 0;
            }
            if (recv == -1) {
                return null;
            }
        } catch (IOException ioe) {
            WifiLog.d(TAG, ioe.toString());
            ioe.printStackTrace();
            return null;
        } catch (Exception e) {
            WifiLog.d(TAG, e.toString());
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (os != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }

        try {
            return new String(CACHE, "GBK");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    private String getChallenge() {
        WifiLog.d(TAG, "getChallenge");
        HttpURLConnection urlc = null;
        InputStream in = null;
        String challenge = null;
        try {
            URL url = new URL("http://" + Constants.TEST_SHORT_BAIDU);
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(10000);
            urlc.setReadTimeout(10000);
            urlc.setRequestMethod("GET");
            urlc.setInstanceFollowRedirects(false);
            urlc.setDoInput(true);
            urlc.connect();
            int responeCode = urlc.getResponseCode();
            if (responeCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = urlc.getHeaderField("Location");
                WifiLog.d(TAG, "Location: " + location);
                challenge = getChallengeFromLocation(location);
            } else {
                WifiLog.d(TAG, "getChallenge Error ");
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
        return challenge;
    }

    // private String getChallenage() {
    // String result = talkWithServer("1.1.1.1", 80, CHALLENGE);
    // Logger.debug(TAG, "getChallenage(), Result: " + result);
    // if (result == null) {
    // return null;
    // }
    // try {
    // result = URLDecoder.decode(result, "UTF-8");
    // Logger.debug(TAG, "result: " + result);
    // } catch (UnsupportedEncodingException e) {
    // Logger.debug(TAG, e.toString());
    // e.printStackTrace();
    // }
    // if (TextUtils.isEmpty(result)) {
    // return null;
    // }
    //
    // final String LOCATION = "Location:";
    // int index = result.indexOf(LOCATION);
    // if (index < 0) {
    // return null;
    // }

    // int startIndex = index + LOCATION.length();
    // int endIndex = startIndex + 5;
    //
    // char[] buf = result.toCharArray();
    // while (buf[startIndex] == ' ') {
    // startIndex++;
    // endIndex++;
    // }
    //
    // String subString = result.substring(startIndex, endIndex);
    // if (subString.compareToIgnoreCase("http:") == 0) {
    // int serverStart = endIndex;
    // int portStart = result.indexOf(":", endIndex + 1);
    // int portEnd = result.indexOf("/", portStart + 1);
    // if (portStart < 0 || portEnd < 0) {
    // Logger.error(TAG, "portStart < 0 || portEnd < 0");
    // return null;
    // }
    //
    // while (buf[serverStart] == ' ') {
    // serverStart++;
    // }
    //
    // while (buf[serverStart] == '/') {
    // serverStart++;
    // }
    //
    // mServer = result.substring(serverStart, portStart);
    // Logger.debug(TAG, "mServer: " + mServer);
    // while (buf[portStart] == ':') {
    // portStart++;
    // }
    // String port = result.substring(portStart, portEnd);
    // try {
    // mPort = Integer.parseInt(port);
    // } catch (NumberFormatException e) {
    //
    // }
    // Logger.debug(TAG, "mPort: " + mPort);
    //
    // } else {
    // return null;
    // }
    //
    // final String CHALLENGE = "challenge";
    // index = result.indexOf(CHALLENGE);
    // index += CHALLENGE.length();
    // endIndex = result.indexOf("\n", index + 1);
    // if (endIndex < 0 || index < 0) {
    // Logger.debug(TAG, "challenge:endIndex < 0 || index < 0");
    // return null;
    // }
    //
    // int start = index;
    // while (buf[start] == '=') {
    // start++;
    // }
    //
    // String challenge = getChallengeFromLocation(result);
    // challenge = challenge.toUpperCase(Locale.getDefault());
    // Logger.debug(TAG, "challenge is : " + challenge);
    // return challenge;
    // }

    private String getChallengeFromLocation(String result) {
        if (result.contains(Constants.PARAMTER_LOGINURL)) {
            result = result.substring(result
                    .indexOf(Constants.PARAMTER_LOGINURL)
                    + Constants.PARAMTER_LOGINURL.length());
            try {
                result = URLDecoder.decode(result, "utf-8");
                WifiLog.d(TAG, "address: " + result);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
        return getChallegeFromNewUrl(result);
    }

    private String getChallegeFromNewUrl(String address) {
        String challenge = null;
        try {
            URI uri = new URI(address);
            List<NameValuePair> values = URLEncodedUtils.parse(uri, "utf-8");
            for (NameValuePair item : values) {
                if (TextUtils.equals(item.getName(), Constants.PARAMTER_IP)) {
                    mServer = item.getValue();
                    WifiLog.d(TAG, "mServer: " + mServer);
//                    PrefsHelper.getInstance(mContext).saveServer(mServer);
                }
                if (TextUtils.equals(item.getName(), Constants.PARAMTER_PORT)) {
                    try {
                        WifiLog.d(TAG, "mPort: " + item.getValue());
                        mPort = Integer.parseInt(item.getValue());
//                        PrefsHelper.getInstance(mContext).savePort(mPort);
                    } catch (NumberFormatException e) {
                        WifiLog.d(TAG, "ParsePort error");
                    }
                }
                if (TextUtils.equals(item.getName(),
                        Constants.PARAMTER_CHALLENGE)) {
                    challenge = item.getValue();
                }
            }
        } catch (URISyntaxException e) {
            WifiLog.d(TAG, "Parse Gateway Error!");
            e.printStackTrace();
        }
        return challenge;
    }

    private byte[] ASC2Hex(final char[] str) {
        int nstrlen = str.length;
        int nlen = 0;
        if (nstrlen % 2 != 0) {
            nlen = -1;
            return null;
        }

        nlen = nstrlen / 2;

        byte[] Hex = new byte[nlen];
        for (int i = 0; i < nlen; i++) {
            int pos = i * 2;

            int value = (int) (charToByte(str[pos]) << 4 | charToByte(str[pos + 1]));
            Hex[i] = (byte) (value & 0xFF);
        }

        return Hex;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private String getNewChallenage(String nameSecret, String challenge) {
        challenge = challenge.toUpperCase();
        byte[] src = ASC2Hex(challenge.toCharArray());

        byte[] newBytes = new byte[256];
        System.arraycopy(src, 0, newBytes, 0, 16);

        byte[] nameBytes = nameSecret.getBytes();
        System.arraycopy(nameBytes, 0, newBytes, 16, nameBytes.length);

        return toMd5(newBytes, 16 + nameBytes.length);
    }

    private String getResponse(String pwd, String newChallenge) {
        newChallenge = newChallenge.toUpperCase();
        byte[] src = ASC2Hex(newChallenge.toCharArray());

        byte[] newBytes = new byte[256];
        newBytes[0] = 0;

        byte[] pwdBytes = pwd.getBytes();
        System.arraycopy(pwdBytes, 0, newBytes, 1, pwdBytes.length);

        System.arraycopy(src, 0, newBytes, pwdBytes.length + 1, 16);
        return toMd5(newBytes, 16 + pwdBytes.length + 1);
    }

    private String toMd5(byte[] bytes, int len) {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(bytes, 0, len);

            byte[] result = algorithm.digest();
            StringBuilder hexString = new StringBuilder();

            for (byte b : result) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String loginHub(String name, String pwd, String url) {
        String content, newChanllenge, response;
        String challenge = getChallenge();
        WifiLog.d(TAG, "loginHub: start to loginHub + challenge: "
                + challenge);

        if (TextUtils.isEmpty(challenge) || TextUtils.isEmpty(pwd)) {
            return Constants.LOGIN_ERROR_MESSAGE;
        }

        newChanllenge = getNewChallenage("591wificom", challenge);
        response = getResponse(pwd, newChanllenge);
        response = response.toLowerCase();
        WifiLog.d(TAG, "loginHub: start to loginHub1");

        content = String.format(Locale.getDefault(), LOGIN_FMT, mServer, mPort,
                name, response, url, mServer, mPort);

        String retStr = talkWithServer(mServer, mPort, content);
        WifiLog.d(TAG, "loginHub: start to loginHub2");

        String checkRet = checkLogin(retStr);
        // if (checkRet != null) {
        // Toast.makeText(mContext, checkRet, 5000).show();
        // return false;
        // }

        // talkWithServer(mServer, mPort, PRELOGIN);

        return checkRet;
    }

    private String checkLogin(String strReturn) {
        WifiLog.d(TAG, "strReturn: " + strReturn);
        if (strReturn == null) {
            return Constants.LOGIN_ERROR_MESSAGE;
        }
        int httpIndex = strReturn.indexOf("Location:");
        if (httpIndex < 0)
            return Constants.LOGIN_ERROR_MESSAGE;
        else {
            int resIndex = strReturn.indexOf("res=", httpIndex);
            if (resIndex < 0) {
                return Constants.LOGIN_ERROR_MESSAGE;
            }
            String strFalse = strReturn.substring(resIndex + 4, resIndex + 10);
            if (strFalse.compareToIgnoreCase("failed") == 0) {
                int reasonEnd = strReturn.indexOf("&", resIndex + 11);
                // reason = strReturn.substring(resIndex + 17, reasonEnd -
                // resIndex - 17);
                String strReplyStart = "<ReplyMessage>";
                String strReplyEnd = "</ReplyMessage>";
                int messageIndex = strReturn.indexOf(strReplyStart, reasonEnd
                        - resIndex - 17);
                if (messageIndex > 0) {
                    int messageEndIndex = strReturn.indexOf(strReplyEnd,
                            messageIndex);
                    return strReturn.substring(
                            messageIndex + strReplyStart.length(),
                            messageEndIndex);
                }
                return Constants.LOGIN_ERROR_MESSAGE;
            }
        }
        return null;
    }

    public boolean logoutHub() {
        WifiLog.d(TAG, "LogoutHub");
        return requestLogout();
    }

    private boolean requestLogout() {
        WifiLog.d(TAG, "sendLogout: ");
        boolean result = false;
        HttpURLConnection urlc = null;
        InputStream in = null;
        String gateway = Utils.getGateway(mContext);
        //TODO 
        String[] res = gateway.split(":");
        if (res.length != 2) {
            return false;
        }
        WifiLog.d(TAG, "server: " + res[0]);
        WifiLog.d(TAG, "port: " + res[1]);
        try {
            URL url = new URL("http://" + gateway + "/logoff");
            urlc = (HttpURLConnection) url.openConnection();
            urlc.setConnectTimeout(10000);
            urlc.setReadTimeout(10000);
            urlc.setRequestMethod("GET");
            urlc.setInstanceFollowRedirects(false);
            urlc.setDoInput(true);
            urlc.connect();
            int responeCode = urlc.getResponseCode();
            if (responeCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                WifiLog.d(TAG, "Logout: successfully");
                result = true;
            } else {
                WifiLog.d(TAG, "Logout: Error ");
            }
        } catch (MalformedURLException e) {
            WifiLog.d(TAG, "Logout: MalformedURLException ");
            e.printStackTrace();
        } catch (IOException e) {
            WifiLog.d(TAG, "Logout: IOException " + e.toString());
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
}
