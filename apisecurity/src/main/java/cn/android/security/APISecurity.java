package cn.android.security;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Debug;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class APISecurity {
    static {
        System.loadLibrary("apisecurity-lib");
    }

    /**
     * 通过次Native方法调用Java的方法
     *
     * @param str
     */
    public static native String sign(String str);

    public static native boolean init(Context context);

    /**
     * 被Native调用的Java方法
     */
    public void javaMethod(String msg) {
        Log.e("mhyLog错误代码", msg);
//        System.exit(1);
    }

    public static void verify(Context context) {
        Log.e("mhyLog", "hash:"+AppSigning.getSignatureHash(context));
        //runCommand();
        Log.e("mhyLog包文件签名", getApkSignatures(context,context.getPackageName()));
        Log.e("mhyLog已安装签名", getInstalledAPKSignature(context, context.getPackageName()));
        //通过获取其他应用的签名 如果一样那么被hook了
    }

    /** 防破签名 1
     * 获取已安装的app签名
     * */
    public static String getInstalledAPKSignature(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo appInfo = pm.getPackageInfo(packageName.trim(), PackageManager.GET_SIGNING_CERTIFICATES);
                if (appInfo == null || appInfo.signingInfo == null)
                    return "";
                return AppSigning.getSignatureString(appInfo.signingInfo.getApkContentsSigners(), AppSigning.SHA1);
            } else {
                PackageInfo appInfo = pm.getPackageInfo(packageName.trim(), PackageManager.GET_SIGNATURES);
                if (appInfo == null || appInfo.signatures == null)
                    return "";
                return AppSigning.getSignatureString(appInfo.signatures, AppSigning.SHA1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /** 防破签名 2
     * C调用Java 从源安装文件获取签名信息
     * 有bug
     * */
    public static String getApkSignatures(Context context, String packname) {
        String path="";
        if (!TextUtils.isEmpty(packname)) {
            try {//获取此包安装路径
                //第一种方法
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packname, 0);
                path = applicationInfo.sourceDir;
                //第二种方法
//            ApplicationInfo applicationInfo = context.getPackageManager().getPackageInfo(packname, PackageManager.GET_META_DATA).applicationInfo;
//            path = applicationInfo.publicSourceDir;//sourceDir; // 获取当前apk包的绝对路径
//                Log.e("mhyLog其他已知包名apk的安装路径", applicationInfo.sourceDir + "&---&" + applicationInfo.publicSourceDir);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                //第三中方法
//                path = context.getPackageResourcePath();
                path= context.getApplicationInfo().sourceDir;
//                Log.e("mhyLOg在apk中获取自身安装路径", path);
            }
        }else {
        //第三中方法
//            path= context.getApplicationInfo().sourceDir;
            path=context.getPackageResourcePath();
        }
        File apkFile = new File(path);
        if (apkFile.exists()) {
            Log.e("mhyLog包安装路径", apkFile.getAbsolutePath());
            PackageManager pm = context.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_SIGNING_CERTIFICATES);
                if (pkgInfo != null && pkgInfo.signingInfo != null) {
                    return AppSigning.getSignatureString(pkgInfo.signingInfo.getApkContentsSigners(), AppSigning.SHA1);
                }
            } else {
                PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_SIGNATURES);
                if (pkgInfo != null && pkgInfo.signatures != null) {
                    return AppSigning.getSignatureString(pkgInfo.signatures, AppSigning.SHA1);
                }
            }
        }
        return "";
    }

    /**
     * 手动构建 Context
     */
    @SuppressLint({"DiscouragedPrivateApi","PrivateApi"})
    public static Context createContext() throws ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            IllegalAccessException,
            NoSuchFieldException,
            NullPointerException{

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
        if (mBoundApplicationObj == null) throw new NullPointerException("mBoundApplicationObj 反射值空");
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

    //需要读取应用列表权限
    public void getAppList(Context context) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            // 判断系统/非系统应用
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {// 非系统应用
                System.out.println("MainActivity.getAppList, packageInfo=" + packageInfo.packageName);
            } else {
                // 系统应用
            }
        }
    }

    static ArrayList<String> list = new ArrayList<>();

    /**
     * 通过指令获取已安装的包
     */
    private static ArrayList<String> runCommand() {
        list.clear();
        try {
            Process process = Runtime.getRuntime().exec("pm list package -3");
            BufferedReader bis = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = null;
            while ((line = bis.readLine()) != null) {
                Log.e("runCommand", "line=" + line);
                list.add(line.split(":")[1]);
            }
        } catch (IOException e) {
            Log.e("runCommand", "e=" + e);
        }
        return list;
    }

    /**
     * 检测动态调试检查应用是否处于调试状态
     * 这个也是借助系统的一个api来进行判断isDebuggerConnected()
     * jdb -connect com.sun.jdi.SocketAttach:hostname=127.0.0.1,port=8700，当连接成功之后，这个方法就会返回true
     */
    public static void detectedDynamicDebug() {
        if (!BuildConfig.DEBUG) {
            if (Debug.isDebuggerConnected()) {
                //进程自杀
                int myPid = android.os.Process.myPid();
                android.os.Process.killProcess(myPid);
                //异常退出虚拟机
                System.exit(1);
            }
        }
    }

    /**
     * 检查应用是否属于debug模式
     * 直接调用Android中的flag属性：ApplicationInfo.FLAG_DEBUGGABLE;
     * 判断是否属于debug模式：防调试
     */
    public void checkDebug(Context context) {
        int i = context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE;
        if (0 != (context.getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
            /**
             *
             * 验证是否可以调试
             * i != 0 已经打开可调式
             */
            Log.e("debug", "被调试");

        }
        boolean debuggerConnected = Debug.isDebuggerConnected();
        Log.e("debug", "是否连接调试  ： " + debuggerConnected);
        /**
         *
         * 获取TracerPid来判断
         *获取获取TracerPid来判断（TracerPid正常情况是0，如果被调试这个是不为0的）
         */
        int pid = android.os.Process.myPid();
        String info = null;
        File file = new File("/proc/" + pid + "/status");
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);
            while ((info = bufferedReader.readLine()) != null) {
                Log.e("debug", "proecc info :  " + info);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /***
     * 防代理
     */
    private boolean isProxy(Context context) {
        String proxyAddress = "";
        int proxyPort = 0;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            proxyAddress = System.getProperty("http.proxyHost");
            String proxyPortString = System.getProperty("http.proxyPort");
            proxyPort = Integer.parseInt((proxyPortString != null ? proxyPortString : "-1"));
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = android.net.Proxy.getPort(context);
        }
        if (!TextUtils.isEmpty(proxyAddress) && proxyPort != -1) {
            return true;
        }
        return false;
    }
// 忽视代理
//    OkHttpClient okHttpClient = new OkHttpClient.Builder()
//            .proxy(Proxy.NO_PROXY)
//            .build();
}

