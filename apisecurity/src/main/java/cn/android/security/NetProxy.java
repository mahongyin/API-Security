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
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;


/**
 * Created By Mahongyin
 * Date    2021/3/14 23:43
 */
public class NetProxy {
    public static boolean isProxy(Context ctx) {
        final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String protStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((protStr != null ? protStr : "-1"));
        }else{
            proxyAddress=android.net.Proxy.getHost(ctx);
            proxyPort=android.net.Proxy.getPort(ctx);
        }
        return (!TextUtils.isEmpty(proxyAddress))&&(proxyPort!=-1);
    }
    private void noProxy(String urlStr){
//        try {
//            URL url =new URL(urlStr);
//            HttpURLConnection  urlConnection=(HttpURLConnection)url.openConnection(Proxy.NO_PROXY);
//            OkHttpClient client =new OkhttpClient().newBuilder().proxy(Proxy.NO_PROXY).build;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
