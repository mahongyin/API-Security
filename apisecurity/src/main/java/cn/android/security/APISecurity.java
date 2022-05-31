package cn.android.security;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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

    public static native String getRealyAppName();

    public static native void verifyApp(Application applicationByReflect);

    public static native boolean verifyApplication();

    public static native boolean init(Context context);

    /**
     * 被Native调用的Java方法
     */
    public void javaMethod(String msg) {
        Log.e("mhyLog错误", msg);
//        System.exit(1);
    }

    public static void verify(Context context) {
//        Log.e("mhyLog", "hash:" + AppSigning.getSignatureHash(context));
//        Log.e("mhyLog", "sha1:" + getSignSha1(context));
        //runCommand();
        // Log.e("mhyLog包文件", "签名:"+getApkSignatures(context, context.getPackageName()));
        //Log.e("mhyLog已安装", "签名："+getInstalledAPKSignature(context, context.getPackageName()));
        //通过获取其他应用的签名 如果一样那么被hook了
    }

    /**
     * 防破签名 1
     * 获取已安装的app签名
     */
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
    /**
     * Return the application's signature from application
     */
    public static Signature[] getAppSignatures(Context app,final String packageName) {
        if (TextUtils.isEmpty(packageName)) return null;
        try {
            PackageManager pm = app.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                if (pi == null) return null;

                SigningInfo signingInfo = pi.signingInfo;
                if (signingInfo.hasMultipleSigners()) {
                    return signingInfo.getApkContentsSigners();
                } else {
                    return signingInfo.getSigningCertificateHistory();
                }
            } else {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                if (pi == null) return null;

                return pi.signatures;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the application's signature from apk file
     */
    @Nullable
    public static Signature[] getAppSignatures(Context app,final File file) {
        if (file == null) return null;
        PackageManager pm = app.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageInfo pi = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_SIGNING_CERTIFICATES);
            if (pi == null) return null;

            SigningInfo signingInfo = pi.signingInfo;
            if (signingInfo.hasMultipleSigners()) {
                return signingInfo.getApkContentsSigners();
            } else {
                return signingInfo.getSigningCertificateHistory();
            }
        } else {
            PackageInfo pi = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_SIGNATURES);
            if (pi == null) return null;

            return pi.signatures;
        }
    }

    /**
     * 防破签名 2
     * C调用Java 从源安装文件获取签名信息
     * 有bug
     */
    public static String getApkSignatures(Context context, String packname) {
        String path = getApkPath(context, packname);
        File apkFile = new File(path);
        if (apkFile.exists()) {
            Log.e("mhyLog包安装路径", apkFile.getAbsolutePath());
            PackageManager pm = context.getPackageManager();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                //TODO 这里获取的signingInfo 为空 猜想是flag不对 但看源码好像 目前只能使【GET_SIGNATURES 对应signatures】
                PackageInfo packageInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_SIGNING_CERTIFICATES);
//                PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES);
                if (packageInfo != null && packageInfo.signingInfo != null) {
                    Signature[] signatures = packageInfo.signingInfo.getApkContentsSigners();
                    return AppSigning.getSignatureString(signatures, AppSigning.SHA1);
                } else {
                    return AppSigning.getAPKSignatures(path);
                }
                //如果获取失败就用下面方法喽
            } else {
                PackageInfo packageInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_SIGNATURES);
                if (packageInfo != null) {
                    Signature[] signatures = packageInfo.signatures;
                    return AppSigning.getSignatureString(signatures, AppSigning.SHA1);
                } else {
                    return AppSigning.showUninstallAPKSignatures(path);
                }
            }
        }
        return "";
    }

    /**
     * //获取此包安装路径
     */
    public static String getApkPath(Context context, String packname) {
        String path = "";
        if (!TextUtils.isEmpty(packname)) {
            try {
                //第一种方法
                ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packname, 0);
                path = applicationInfo.sourceDir;
                //第二种方法
//                Log.e("mhyLog其他已知包名apk的安装路径", applicationInfo.sourceDir + "&---&" + applicationInfo.publicSourceDir);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                try {
                    ApplicationInfo applicationInfo = context.getPackageManager().getPackageInfo(packname, PackageManager.GET_META_DATA).applicationInfo;
                    path = applicationInfo.publicSourceDir;//sourceDir; // 获取当前apk包的绝对路径
                } catch (PackageManager.NameNotFoundException exception) {
                    exception.printStackTrace();
                    //第三中方法 本包
                    path = context.getApplicationInfo().sourceDir;
                }
//                Log.e("mhyLOg在apk中获取自身安装路径", path);
            }
        } else {
            //第四中方法 本包
            path = context.getPackageResourcePath();
            //第五种方法
//            path = context.getPackageCodePath();
        }
        return path;
    }


    //**安装列表**需要读取应用列表权限
    public static List<String> getAppList(Context context) {
        List<String> list = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packages = pm.getInstalledPackages(0);
//        pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);// GET_UNINSTALLED_PACKAGES代表已删除，但还有安装目录的
        for (PackageInfo packageInfo : packages) {
            // 判断系统/非系统应用
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {// 非系统应用
                Log.e("mhyLog", "packageInfo=" + packageInfo.packageName);
                list.add(packageInfo.packageName);
            } else {
                // 系统应用
            }
        }
        return list;
    }

    /**
     * 通过已安装app 获取当前app签名
     */
    private static String getSignSha1(Context context) {
        List<PackageInfo> apps;
        PackageManager pm = context.getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            apps = pm.getInstalledPackages(PackageManager.GET_SIGNING_CERTIFICATES);
        } else {
            apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        }
        for (PackageInfo packageinfo : apps) {
            String packageName = packageinfo.packageName;
            if (packageName.equals(context.getPackageName())) {
                Log.e("mhyLog", "packageInfo=" + packageinfo.packageName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    if (packageinfo.signingInfo != null) {
                        return AppSigning.getSignatureString(packageinfo.signingInfo.getApkContentsSigners(), AppSigning.SHA1);
                    }
                } else {
                    if (packageinfo.signatures != null) {
                        return AppSigning.getSignatureString(packageinfo.signatures, AppSigning.SHA1);
                    }
                }
            }
        }
        return "";
    }


    /**
     * 通过指令获取已安装的包
     */
    private static ArrayList<String> runCommand() {
        ArrayList<String> list = new ArrayList<>();
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
            Log.e("runCommand", "e=" + e.getMessage());
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

}

