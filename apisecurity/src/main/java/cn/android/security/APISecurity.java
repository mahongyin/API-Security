package cn.android.security;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
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

    public static native boolean init(Context context);

    /**
     * 被Native调用的Java方法
     *
     * @param msg
     */
    public void javaMethod(String msg) {
        Log.e("错误代码",msg);
//        System.exit(1);
    }

    private   void verify(Context context){
//        String ppp = runCommand().get(0);

        Log.e("包路径文件签名", getApkSignatures(context,"com.tencent.mm"));

        Log.e("已安装APP签名", AppSigning.getSingInfo(context, "com.tencent.mm", AppSigning.SHA1));
        //通过获取其他应用的签名 如果一样那么被hook了
    }

//从安装文件获取签名
    public static String getApkSignatures(Context context, String packname) {
        String sign = "";
        String path = null;
        try {
            path = context.getPackageManager().getApplicationInfo(packname, 0).sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
//        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packname, PackageManager.GET_META_DATA);
//        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
//        String path =applicationInfo.publicSourceDir;//sourceDir; // 获取当前apk包的绝对路径
        File apkFile=new File(path);
        if (apkFile != null && apkFile.exists()) {
            Log.e("pppp",apkFile.getAbsolutePath());
            PackageManager pm = context.getPackageManager();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_SIGNING_CERTIFICATES);
                if (pkgInfo != null && pkgInfo.signingInfo != null && pkgInfo.signingInfo.getApkContentsSigners().length > 0) {
//                    sign = pkgInfo.signingInfo.getApkContentsSigners()[0].toCharsString();
                    sign =  AppSigning.getSignatureString(pkgInfo.signingInfo.getApkContentsSigners()[0],AppSigning.SHA1);
                }
            } else {
                PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_SIGNATURES);
                if (pkgInfo != null && pkgInfo.signatures != null && pkgInfo.signatures.length > 0) {
//                    sign = pkgInfo.signatures[0].toCharsString();
                    sign = AppSigning.getSignatureString(pkgInfo.signatures[0],AppSigning.SHA1);
                }
            }
        }
        return sign;
    }

    //获取已安装的app签名
    private static String getInstalledAPKSignature(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
//        String packageName="com.android.calendar";
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                PackageInfo appInfo = pm.getPackageInfo(packageName.trim(), PackageManager.GET_SIGNING_CERTIFICATES);
                if (appInfo == null || appInfo.signingInfo == null)
                    return "";
//                return appInfo.signingInfo.getApkContentsSigners()[0].toCharsString();
                return AppSigning.getSignatureString(appInfo.signingInfo.getApkContentsSigners()[0],AppSigning.SHA1);
            } else {
                PackageInfo appInfo = pm.getPackageInfo(packageName.trim(), PackageManager.GET_SIGNATURES);
                if (appInfo == null || appInfo.signatures == null)
                    return "";
//                return appInfo.signatures[0].toCharsString();
                return AppSigning.getSignatureString(appInfo.signatures[0],AppSigning.SHA1);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }
//需要读取应用列表权限
    private void getAppList(Context context) {
        PackageManager pm = context.getPackageManager();
        // Return a List of all packages that are installed on the device.
        List<PackageInfo> packages = pm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            // 判断系统/非系统应用
            if ((
                    packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) // 非系统应用
            {
                System.out.println("MainActivity.getAppList, packageInfo=" + packageInfo.packageName);
            } else {
                // 系统应用
            }
        }
    }
    static ArrayList<String> list = new ArrayList<>();
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
            System.out.println("runCommand,e=" + e);
        }
        return list;
    }

    /**
     * 检测动态调试
     */
    public void detectedDynamicDebug(){
        if (!BuildConfig.DEBUG){
            if (Debug.isDebuggerConnected()){
                //进程自杀
                int myPid = android.os.Process.myPid();
                android.os.Process.killProcess(myPid);

                //异常退出虚拟机
                System.exit(1);
            }
        }
    }
}

