package cn.android.sample;

/**
 * 项目名 FileTransfer
 * 所在包 bin.mt.apksignaturekillerplus
 * 作者 mahongyin
 * 时间 2020-03-16 10:55
 * 邮箱 mhy.work@qq.com
 * 描述 说明: 将application替换到AndroidMainfest.xml  代码里改放APPlication
 * 代码里替换要hook的签名值 始终返回原始签名，达到欺骗
 */

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
//继承原application

public class HookApplication extends Application implements InvocationHandler {
    private static final int GET_SIGNATURES = 64;
    private String appPkgName = "";
    private Object base;
    private byte[][] sign;
    static HookApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app=this;
    }

    public static Context getContext() {
        if (app==null){
            app=new HookApplication();
            app.onCreate();
        }
        return app;
    }


    @Override
    protected void attachBaseContext(Context context) {
        hook(context);
        super.attachBaseContext(context);
    }
    /**
     * 全局hook 签名校验
     * @param context
     */
    private void hook(Context context) {
        try {
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(Base64.decode("AQAAAjAwggIsMIIBlaADAgECAgMY2gowDQYJKoZIhvcNAQEFBQAwWzELMAkGA1UEBhMCQ04xCzAJ\nBgNVBAgTAmhlMQwwCgYDVQQHEwNzanoxDDAKBgNVBAoTA2VkdTEPMA0GA1UECxMGc2Nob29sMRIw\nEAYDVQQDEwltYWhvbmd5aW4wHhcNMTgxMDExMDcwMjE1WhcNNDMxMDA1MDcwMjE1WjBbMQswCQYD\nVQQGEwJDTjELMAkGA1UECBMCaGUxDDAKBgNVBAcTA3NqejEMMAoGA1UEChMDZWR1MQ8wDQYDVQQL\nEwZzY2hvb2wxEjAQBgNVBAMTCW1haG9uZ3lpbjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA\nr0aFNvrxBnBEEbAANDcsrmBlcQBGJKsvT5onXngek2ZbkWZx8/1o8nbgCBSjAZvnXEYYjjkC5k+A\nIne1PJUF5bPKTjIQepNmtK+KVHsAJLjn6rG4fQ3oaeu0vvNBehuzt54bACbzkXZj9nV5rs8OllD9\nRronLsOb3DVJ95DyLIMCAwEAATANBgkqhkiG9w0BAQUFAAOBgQCpF6kB++zR0FW4eZaJCEAnQNP0\nGtwAnrXEpvP7ePcakk/JT/e56uTS/OAbpmM/tWETvPtx9hOB4RoPwRl3Q0G1ieCMeVyIABmGAeOk\ntARqtiExfHvorrmk4mxVIiPTwUJSWzAKuhLV93pMxTFZSZK0iTJFVVM/l8Wh3CTdFtpW+w==\n", 0)));
            byte[][] bArr = new byte[(dataInputStream.read() & 255)][];
            for (int i = 0; i < bArr.length; i++) {
                bArr[i] = new byte[dataInputStream.readInt()];
                dataInputStream.readFully(bArr[i]);
            }
            Class cls = Class.forName("android.app.ActivityThread");
            Object invoke = cls.getDeclaredMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field declaredField = cls.getDeclaredField("sPackageManager");
            declaredField.setAccessible(true);
            Object obj = declaredField.get(invoke);
            Class cls2 = Class.forName("android.content.pm.IPackageManager");
            this.base = obj;
            this.sign = bArr;
            this.appPkgName = context.getPackageName();
            Object newProxyInstance = Proxy.newProxyInstance(cls2.getClassLoader(), new Class[]{cls2}, this);
            declaredField.set(invoke, newProxyInstance);
            PackageManager packageManager = context.getPackageManager();
            Field declaredField2 = packageManager.getClass().getDeclaredField("mPM");
            declaredField2.setAccessible(true);
            declaredField2.set(packageManager, newProxyInstance);
            System.out.println("PmsHook success.");
        } catch (Exception e) {
            System.err.println("PmsHook failed.");
            e.printStackTrace();
        }
    }


    @Override
    public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
        if ("getPackageInfo".equals(method.getName())) {
            String str = (String) objArr[0];
            if ((((Integer) objArr[1]).intValue() & 64) != 0 && this.appPkgName.equals(str)) {
                PackageInfo packageInfo = (PackageInfo) method.invoke(this.base, objArr);
                packageInfo.signatures = new Signature[this.sign.length];
                for (int i = 0; i < packageInfo.signatures.length; i++) {
                    packageInfo.signatures[i] = new Signature(this.sign[i]);
                }
                return packageInfo;
            }
        }
        return method.invoke(this.base, objArr);
    }


}