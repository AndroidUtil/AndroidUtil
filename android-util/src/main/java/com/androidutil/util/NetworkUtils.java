package com.androidutil.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:jenly1314@gmail.com">Jenly</a>
 */
public class NetworkUtils {


    private NetworkUtils(){
        throw new AssertionError();
    }

    /**
     * 获取 DhcpInfo
     * @param context
     * @return
     */
    public static DhcpInfo getDhcpInfo(Context context){
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wifiManager.getDhcpInfo();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将 ip 字符串转换为 int 类型的数字
     * <p>
     * 思路就是将 ip 的每一段数字转为 8 位二进制数，并将它们放在结果的适当位置上
     *
     * @param ipString ip字符串，如 127.0.0.1
     * @return ip字符串对应的 int 值
     */
    public static int ip2Int(String ipString) {
        // 取 ip 的各段
        String[] ipSlices = ipString.split("\\.");
        int rs = 0;
        for (int i = 0; i < ipSlices.length; i++) {
            // 将 ip 的每一段解析为 int，并根据位置左移 8 位
            int intSlice = Integer.parseInt(ipSlices[i]) << 8 * i;
            // 求与
            rs = rs | intSlice;
        }
        return rs;
    }

    /**
     * 将 int 转换为 ip 字符串
     *
     * @param ipInt 用 int 表示的 ip 值
     * @return ip字符串，如 127.0.0.1
     */
    public static String int2Ip(int ipInt) {
        return (0xFF & ipInt)
                + "." + (0xFF & ipInt >> 8)
                + "." + (0xFF & ipInt >> 16)
                + "." + (0xFF & ipInt >> 24);
    }


    /**
     * 将 int 转换为 InetAddress
     * @param hostAddress
     * @return
     */
    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }


    /**
     * 是否有网络
     * @param context
     * @return
     */
    public static boolean isNetWorkActive(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return connectivityManager.getActiveNetwork() != null;
        }
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnected();
    }


    /**
     * WIFI是否连接
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected()) {
            return true;
        }

        return false;
    }
}
