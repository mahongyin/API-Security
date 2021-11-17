/*
 * Copyright (c) 2020-2021.  安卓
 * FileName: ${NAME}
 * Author: ${USER}
 * Date: ${DATE} ${TIME}
 * Description: ${DESCRIPTION}
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 * 本代码未经许可，不得私自修改何使用
 */

package cn.android.security;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;


/**
 * Created By Mahongyin
 * Date    2021/3/14 23:43
 */
public class NetProxy {

// 忽视代理
//    OkHttpClient okHttpClient = new OkHttpClient.Builder()
//            .proxy(Proxy.NO_PROXY)
//            .build();
    private void noProxy(String urlStr){
//        try {
//            URL url =new URL(urlStr);
//            HttpURLConnection  urlConnection=(HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
//            OkHttpClient client =new OkhttpClient().newBuilder().proxy(Proxy.NO_PROXY).build;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public static boolean isHookNet(Context context){
        return isProxy(context)||isVpnUsed();
    }
    /**
     * 网络是否代理
     */
    private static boolean isProxy(Context ctx) {
        final boolean iSICSORLATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (iSICSORLATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String protStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((protStr != null ? protStr : "-1"));
        }else{
            proxyAddress=android.net.Proxy.getHost(ctx);
            proxyPort=android.net.Proxy.getPort(ctx);
        }
        return (!TextUtils.isEmpty(proxyAddress))&&(proxyPort!=-1);
    }

    /**
     * 是否正在使用VPN
     */
    private static boolean isVpnUsed() {
        try {
            Enumeration<?> niList = NetworkInterface.getNetworkInterfaces();
            if(niList != null) {
                for (Object obj : Collections.list(niList)) {
                    NetworkInterface intf=(NetworkInterface)obj;
                    if(!intf.isUp() || intf.getInterfaceAddresses().size() == 0) {
                        continue;
                    }
                    Log.d("-----", "isVpnUsed() NetworkInterface Name: " + intf.getName());
                    if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())){
                        return true; // The VPN is up
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

}
