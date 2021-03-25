package cn.android.security;

import android.annotation.SuppressLint;
import android.app.Application;
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
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名验证
 *
 * @author lWX537240
 * @date 2019/6/13
 */

public class AppSigning {

    /**
     * 注解限定String类型为指定
     */
    @StringDef({MD5, SHA1, SHA256})
    @Retention(RetentionPolicy.SOURCE)
    @interface SigniType {
    }
    public final static String MD5 = "MD5";
    public final static String SHA1 = "SHA1";
    public final static String SHA256 = "SHA256";

    /**
     * 提早检测签名
     */
  public static void earlyCheckSign() {
        // 手动构造 context
        try {
            Context context = createContext();
            //用新 context 校验签名的过程(正常的检测一样)
            String sing = APISecurity.getInstalledAPKSignature(context, context.getPackageName());
            Log.e("mhyLog手动构造", sing);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 手动构建 Context
     */
    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    public static Context createContext() throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException,
            NoSuchFieldException,
            NullPointerException {
        // 反射获取 ActivityThread 的 currentActivityThread 获取 mainThread
        Class activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod =
                activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object mainThreadObj = currentActivityThreadMethod.invoke(null);

        // 反射获取 mainThread 实例中的 mBoundApplication 字段
        Field mBoundApplicationField = activityThreadClass.getDeclaredField("mBoundApplication");
        mBoundApplicationField.setAccessible(true);
        Object mBoundApplicationObj = mBoundApplicationField.get(mainThreadObj);

        // 获取 mBoundApplication 的 packageInfo 变量
        if (mBoundApplicationObj == null)
            throw new NullPointerException("mBoundApplicationObj 反射值空");
        Class mBoundApplicationClass = mBoundApplicationObj.getClass();
        Field infoField = mBoundApplicationClass.getDeclaredField("info");
        infoField.setAccessible(true);
        Object packageInfoObj = infoField.get(mBoundApplicationObj);

        // 反射调用 ContextImpl.createAppContext(ActivityThread mainThread, LoadedApk packageInfo)
        if (mainThreadObj == null) throw new NullPointerException("mainThreadObj 反射值空");
        if (packageInfoObj == null) throw new NullPointerException("packageInfoObj 反射值空");
        Method createAppContextMethod = Class.forName("android.app.ContextImpl").getDeclaredMethod(
                "createAppContext",
                mainThreadObj.getClass(),
                packageInfoObj.getClass());
        createAppContextMethod.setAccessible(true);
        return (Context) createAppContextMethod.invoke(null, mainThreadObj, packageInfoObj);

    }
    /**
     * 校验 application
     */
    public static boolean sameApplication(){
        //在这里使用反射 获取比较靠谱 如果 被替换换 就查出来了
        Application nowApplication = getApplicationByReflect();
        String trueApplicationName = APISecurity.getRealyAppName();
        //getSimpleName()自己的Application类名 防止替换
        String nowApplicationName = nowApplication.getClass().getName();//当前app类全名
        Log.e("mhyLogAppName", "反射获取:"+nowApplicationName);
        return trueApplicationName.equals(nowApplicationName);
    }

    /**
     * 通过反射获取 当前application
     */
    @SuppressLint("PrivateApi")
    public static Application getApplicationByReflect() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("you should init first");
            }
            return (Application) app;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new NullPointerException("you should init first");
    }

    /**
     * 获取相应的类型的签名信息（把签名的byte[]信息转换成16进制）
     */
    public static String getSignatureString(Signature[] sigs, @SigniType String type) {
        String fingerprint = "error!";
        if (sigs.length > 0) {
            for (Signature sig : sigs) {
//                Log.e("mhyLog", "Signature64:" + Base64.encodeToString(sig.toCharsString().getBytes(), Base64.DEFAULT));
                Log.e("mhyLog", "SignatureHash:" + sig.toCharsString().hashCode());
            }
            byte[] hexBytes = sigs[0].toByteArray();
            try {
                StringBuilder buffer = new StringBuilder();//单线程 无所谓
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


    @SuppressLint("PackageManagerGetSignatures")
    public static int getSignatureHash(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        StringBuilder sb = new StringBuilder();
        try {
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
                if (pi != null && pi.signingInfo != null) {
                    signatures = pi.signingInfo.getApkContentsSigners();
                    for (Signature signature : signatures) {
                        sb.append(signature.toCharsString());
                    }
                    return sb.toString().hashCode();
                }
                //如果 失败了继续
            }
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            if (pi != null) {
                signatures = pi.signatures;
                for (Signature signature : signatures) {
                    sb.append(signature.toCharsString());
                }
                return sb.toString().hashCode();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 防破签名 3  使原生检测签名能获取真实值 助力防破签名1而存在
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
     * 检测 PackageManager 被代理
     */
    @SuppressLint("PrivateApi")
    public static boolean checkPMProxy(Context context) {
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
            Log.e("mhyLog_PM代理",truePMName+"=="+nowPMName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 类名改变说明被代理了
        return !truePMName.equals(nowPMName);
    }

    /**
     * 防破签名 2
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

                if (Build.VERSION.SDK_INT >= 28){
                    Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", pkgParserPkg.getClass(), Boolean.TYPE);//true skipVerify
                    pkgParser_collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, Build.VERSION.SDK_INT >= Build.VERSION_CODES.P);

//                    Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", pkgParserPkg.getClass(), Boolean.TYPE);
//                    pkgParser_collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, false);

                    Field mSigningDetailsField = pkgParserPkg.getClass().getDeclaredField("mSigningDetails"); // SigningDetails
                    mSigningDetailsField.setAccessible(true);

                    Object mSigningDetails = mSigningDetailsField.get(pkgParserPkg);
                    Field infoField = mSigningDetails.getClass().getDeclaredField("signatures");
                    infoField.setAccessible(true);
                    Signature[] info = (Signature[]) infoField.get(mSigningDetails);
                    return getSignatureString(info,AppSigning.SHA1);
                }else {
                    Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", pkgParserPkg.getClass(), Integer.TYPE);
                    pkgParser_collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, PackageManager.GET_SIGNATURES);

                    Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField("mSignatures");
                    Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);
                    return getSignatureString(info,AppSigning.SHA1);
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
                return getSignatureString(info,AppSigning.SHA1);
            }
        } catch (Exception e) {
            Log.e("getAPKSignatures", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 所以我们需要考虑读取data/app/packagename-1(2)/base.apk，硬编码读取这个apk的信息，可以考虑用PackageParser来读取
     */
    public static String showUninstallAPKSignatures(String apkPath) {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        try {
            // apk包的文件路径
            // 这是一个Package 解释器, 是隐藏的
            // 构造函数的参数只有一个, apk文件的路径
            // PackageParser packageParser = new PackageParser(apkPath);
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
//            MediaApplication.logD(DownloadApk.class, "pkgParser:" + pkgParser.toString());
            // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            // PackageParser.Package mPkgInfo = packageParser.parsePackage(new
            // File(apkPath), apkPath,
            // metrics, 0);
            typeArgs = new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",
                    typeArgs);
            valueArgs = new Object[4];
            valueArgs[0] = new File(apkPath);
            valueArgs[1] = apkPath;
            valueArgs[2] = metrics;
            valueArgs[3] = PackageManager.GET_SIGNATURES;
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

            typeArgs = new Class[2];
            typeArgs[0] = pkgParserPkg.getClass();
            typeArgs[1] = Integer.TYPE;
            Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates",
                    typeArgs);
            valueArgs = new Object[2];
            valueArgs[0] = pkgParserPkg;
            valueArgs[1] = PackageManager.GET_SIGNATURES;
            pkgParser_collectCertificatesMtd.invoke(pkgParser, valueArgs);
            // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
            Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField("mSignatures");
            Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);
//            MediaApplication.logD(DownloadApk.class, "size:"+info.length);
//            MediaApplication.logD(DownloadApk.class, info[0].toCharsString());
            return getSignatureString(info,AppSigning.SHA1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过判断云端MD5判断apk是否被改过
     */
    public static String apkMD5(Context context){
       return getFileMD5(new File(context.getPackageCodePath()),16);
      //  context.getPackageResourcePath();
    }
    /**
     * 获取单个文件的MD5值
     * @param file 文件
     * @param radix  位 16 32 64
     */

    public static String getFileMD5(File file,int radix) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance(MD5);
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        return bigInt.toString(radix);
    }
    /**
       * 获取文件夹中文件的MD5值
       * @param file
       * @param listChild ;true递归子目录中的文件
       */
    public static Map<String, String> getDirMD5(File file, boolean listChild) {
        if (!file.isDirectory()) {
            return null;
        }
        Map<String, String> map = new HashMap<>();
        String md5;
        File files[] = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory() && listChild) {
                map.putAll(getDirMD5(f, listChild));
            } else {
                md5 = getFileMD5(f,16);
                if (md5 != null) {
                    map.put(f.getPath(), md5);
                }
            }
        }
        return map;
    }
}

