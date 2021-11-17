package com.mhy.safe.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

/**
 * @ProjectName: SafeDemo
 * @Package: com.mhy.safe.utils
 * @ClassName: WifiUtil
 * @Description: java类作用描述
 * @Author: itfitness
 * @CreateDate: 2021/11/1 15:17
 * @UpdateUser: 更新者：
 * @UpdateDate: 2021/11/1 15:17
 * @UpdateRemark: 更新说明：
 */
public class WifiUtil {
    /*
     * 判断设备 是否使用代理上网
     * */
    public static boolean isWifiProxy(Context context) {
        // 是否大于等于4.0
        final boolean IS_ICS_OR_LATER = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
        String proxyAddress;
        int proxyPort;
        if (IS_ICS_OR_LATER) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((portStr != null ? portStr : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        return (!TextUtils.isEmpty(proxyAddress)) && (proxyPort != -1);
    }
}
