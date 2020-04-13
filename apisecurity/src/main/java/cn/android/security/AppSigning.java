package cn.android.security;

/**
 * @author mahongyin
 * @Project Android-API-Security-master
 * @Package cn.android.sample
 * @data 2020-04-11 22:20
 * @CopyRight mhy.work@qq.com
 * @description:
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author lWX537240
 * @date 2019/6/13
 */

public class AppSigning {

    static String TAG = "AppSigning";

    public final static String MD5 = "MD5";
    public final static String SHA1 = "SHA1";
    public final static String SHA256 = "SHA256";

    /**
     * 返回一个签名的对应类型的字符串
     *
     * @param context
     * @param packageName
     * @param type
     * @return
     */
    public static String getSingInfo(Context context, String packageName, String type) {
        String tmp = "error!";
        try {
            Signature[] signs = getSignatures(context, packageName);
//            Log.e(TAG, "signs =  " + Arrays.asList(signs));
            Signature sig = signs[0];

            if (MD5.equals(type)) {
                tmp = getSignatureString(sig, MD5);
            } else if (SHA1.equals(type)) {
                tmp = getSignatureString(sig, SHA1);
            } else if (SHA256.equals(type)) {
                tmp = getSignatureString(sig, SHA256);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmp;
    }

    /**
     * 返回对应包的签名信息
     *
     * @param context
     * @param packageName
     * @return
     */
    public static Signature[] getSignatures(Context context, String packageName){
        try {

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            PackageInfo appInfo = context.getPackageManager().getPackageInfo(packageName.trim(), PackageManager.GET_SIGNING_CERTIFICATES);

            return appInfo.signingInfo.getApkContentsSigners();
        } else {
            PackageInfo appInfo = context.getPackageManager().getPackageInfo(packageName.trim(), PackageManager.GET_SIGNATURES);

            return appInfo.signatures;
        }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取相应的类型的字符串（把签名的byte[]信息转换成16进制）
     *
     * @param sig
     * @param type
     * @return
     */
    public static String getSignatureString(Signature sig, String type) {
        byte[] hexBytes = sig.toByteArray();
        String fingerprint = "error!";
        try {
            StringBuffer buffer = new StringBuffer();
            MessageDigest digest = MessageDigest.getInstance(type);
            if (digest != null) {
                digest.reset();
                digest.update(hexBytes);
                byte[] byteArray = digest.digest();
                for (int i = 0; i < byteArray.length; i++) {
                    if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                        buffer.append("0").append(Integer.toHexString(0xFF & byteArray[i])); //补0，转换成16进制
                    } else {
                        buffer.append(Integer.toHexString(0xFF & byteArray[i]));//转换成16进制
                    }
                }
                fingerprint = buffer.toString().toLowerCase()/*.toUpperCase()*/; //转换成大写
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return fingerprint;
    }




}

