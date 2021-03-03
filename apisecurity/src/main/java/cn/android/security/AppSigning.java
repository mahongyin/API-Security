package cn.android.security;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.StringDef;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * 签名验证
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
     * 注解限定String类型为指定
     */
    @StringDef({MD5, SHA1, SHA256})
    @Retention(RetentionPolicy.SOURCE)
    @interface SigniType {
    }

    /**
     * 获取相应的类型的签名信息（把签名的byte[]信息转换成16进制）
     */
    public static String getSignatureString(Signature[] sigs, @SigniType String type) {
        String fingerprint = "error!";
        if (sigs.length>0) {
            for (Signature sig : sigs) {
                Log.e("mhyLog", "Signature64:" + Base64.encodeToString(sig.toCharsString().getBytes(),Base64.DEFAULT));
            }
            byte[] hexBytes = sigs[0].toByteArray();
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
        }
        return fingerprint;
    }


    public static int getSignatureHash(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        StringBuilder sb = new StringBuilder();
        int flags = PackageManager.GET_SIGNATURES;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), flags);
            Signature[] signatures = pi.signatures;
            for (Signature signature : signatures) {
                sb.append(signature.toCharsString());
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return sb.toString().hashCode();
    }

    //这个是获取SHA1的方法  上面那个方法是获取签名的hash值 这个和cmd里面获取的是一样的
    public static String getCertificateSHA1Fingerprint(Context context) {
        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取当前要获取SHA1值的包名，也可以用其他的包名，但需要注意，
        //在用其他包名的前提是，此方法传递的参数Context应该是对应包的上下文。
        String packageName = context.getPackageName();
        //返回包括在包中的签名信息
        int flags = PackageManager.GET_SIGNATURES;
        PackageInfo packageInfo = null;
        try {
            //获得包的所有内容信息类
            packageInfo = pm.getPackageInfo(packageName, flags);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //签名信息
        Signature[] signatures = packageInfo.signatures;
        byte[] cert = signatures[0].toByteArray();
        //将签名转换为字节数组流
        InputStream input = new ByteArrayInputStream(cert);
        //证书工厂类，这个类实现了出厂合格证算法的功能
        CertificateFactory cf = null;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        //X509证书，X.509是一种非常通用的证书格式
        X509Certificate c = null;
        try {
            c = (X509Certificate) cf.generateCertificate(input);
        } catch (CertificateException e) {
            e.printStackTrace();
        }
        String hexString = null;
        try {
            //加密算法的类，这里的参数可以使MD4,MD5等加密算法
            MessageDigest md = MessageDigest.getInstance("SHA1");
            //获得公钥
            byte[] publicKey = md.digest(c.getEncoded());
            //字节到十六进制的格式转换
            hexString = byte2HexFormatted(publicKey);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return hexString;
    }

    //这里是将获取到得编码进行16进制转换
    private static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1)
                h = "0" + h;
            if (l > 2)
                h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1))
                str.append(':');
        }
        return str.toString();
    }

    /** 防破签名 3  使原生检测签名能获取真实值 助力防破签名1而存在
     * 通过重置PackageManager防止getPackageInfo方法被代理设置
     * 亲测MT管理器（当前2.9.1）的一键去签名校验(包括加强版)无效！
     * 当然如果别人反编译把代码删除的话那就没办法了
     * getBaseContext()
     */
    public static void resetPackageManager(Context baseContext) {
        try {
            //重置全局sPackageManager对象
            reset1:
            {
                Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
                sPackageManagerField.setAccessible(true);
                sPackageManagerField.set(activityThreadClass, null);
                //因为上面已经把sPackageManager变量设置为null了，调用这个方法重新赋值
                Method getPackageManagerMethod = activityThreadClass.getDeclaredMethod("getPackageManager");
                getPackageManagerMethod.setAccessible(true);
                getPackageManagerMethod.invoke(activityThreadClass);
            }
            //重置当前上下文mPackageManager对象
            reset2:
            {
                Class<?> baseContextClass = baseContext.getClass();
                Field mPackageManagerField = baseContextClass.getDeclaredField("mPackageManager");
                mPackageManagerField.setAccessible(true);
                mPackageManagerField.set(baseContext, null);
                //重新设置为已经重置好的sPackageManager
                Method getPackageManagerMethod = baseContextClass.getDeclaredMethod("getPackageManager");
                getPackageManagerMethod.setAccessible(true);
                getPackageManagerMethod.invoke(baseContext);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    /**
     * 检测 PackageManager 代理
     */
    @SuppressLint("PrivateApi")
    public static boolean checkPMProxy(Context context){
        String truePMName = "android.content.pm.IPackageManager$Stub$Proxy";
        String nowPMName = "";
        try {
            // 被代理的对象是 PackageManager.mPM
            PackageManager packageManager = context.getPackageManager();
            Field mPMField = packageManager.getClass().getDeclaredField("mPM");
            mPMField.setAccessible(true);
            Object mPM = mPMField.get(packageManager);
            // 取得类名
            nowPMName = mPM.getClass().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 类名改变说明被代理了
        return truePMName.equals(nowPMName);
    }

    /** 防破签名 2
     * 安装路径获取签名
     * PackageParser 28新api
     */
    @SuppressLint("PrivateApi")
    public static String getAPKSignatures(String apkPath) {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        try {
            // apk包的文件路径
            // 这是一个Package 解释器, 是隐藏的
            // 构造函数的参数只有一个, apk文件的路径
            // PackageParser packageParser = new PackageParser(apkPath);
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            // 这个是与显示有关的, 里面涉及到一些像素显示等等
            // 本来是用来根据屏幕大小解析对应资源，但是签名校验与屏幕大小无关，
            // 所以setDefault来使用默认的情况
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            Constructor pkgParserCt = null;
            Object pkgParser = null;
            if (Build.VERSION.SDK_INT > 20) {
                pkgParserCt = pkgParserCls.getConstructor();
                pkgParser = pkgParserCt.newInstance();
                Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", File.class, int.class);
                Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, new File(apkPath), PackageManager.GET_SIGNATURES);

                if (Build.VERSION.SDK_INT >= 28) {
                    Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", pkgParserPkg.getClass(), Boolean.TYPE);//true skipVerify
                    pkgParser_collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, Build.VERSION.SDK_INT > 28);

//                    Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", pkgParserPkg.getClass(), Boolean.TYPE);
//                    pkgParser_collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, false);

                    Field mSigningDetailsField = pkgParserPkg.getClass().getDeclaredField("mSigningDetails"); // SigningDetails
                    mSigningDetailsField.setAccessible(true);

                    Object mSigningDetails = mSigningDetailsField.get(pkgParserPkg);
                    Field infoField = mSigningDetails.getClass().getDeclaredField("signatures");
                    infoField.setAccessible(true);
                    Signature[] info = (Signature[]) infoField.get(mSigningDetails);
                    return info[0].toCharsString();

                } else {
                    Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", pkgParserPkg.getClass(), Integer.TYPE);
                    pkgParser_collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, PackageManager.GET_SIGNATURES);

                    Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField("mSignatures");
                    Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);
                    return info[0].toCharsString();
                }


            } else {
                pkgParserCt = pkgParserCls.getConstructor(typeArgs);
                pkgParser = pkgParserCt.newInstance(apkPath);
                Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", File.class, String.class, DisplayMetrics.class, Integer.TYPE);
                Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, new File(apkPath), apkPath, metrics, PackageManager.GET_SIGNATURES);
                Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", pkgParserPkg.getClass(), Integer.TYPE);
                pkgParser_collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, PackageManager.GET_SIGNATURES);
                // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
                Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField("mSignatures");
                Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);
                return info[0].toCharsString();
            }
        } catch (Exception e) {
            Log.e("getAPKSignatures", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}

