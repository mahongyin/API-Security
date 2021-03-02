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

import cn.android.security.AppSigning;
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
        app = this;
        //在签名校验被hook 之后重置PackageManager
        /*在这里 重置PackageManager 只要在验证前重置即可*/
        AppSigning.resetPackageManager(getBaseContext());
    }

    public static Context getContext() {
        if (app == null) {
            app = new HookApplication();
            app.onCreate();
        }
        return app;
    }


    @Override
    protected void attachBaseContext(Context context) {
        //在这里hook 签名校验被
        hook(context);
        super.attachBaseContext(context);
    }

    /**
     * 全局hook 签名校验
     *
     * @param context
     */
    private void hook(Context context) {
        try {
            String relay="308203273082020fa003020102020477d6d1f6300d06092a864886f70d01010b05003044310c300a06035504061303303231310e300c060355040813056368696e613111300f060355040713087368616e676861693111300f0603550403130877757a6f6e67626f301e170d3139303330353032343132345a170d3434303232373032343132345a3044310c300a06035504061303303231310e300c060355040813056368696e613111300f060355040713087368616e676861693111300f0603550403130877757a6f6e67626f30820122300d06092a864886f70d01010105000382010f003082010a0282010100a3ac52268a32e8420a20a727c184c133d513998a207e198f5a535d628a436ba5e095e7ba3f92535234a83fb6272e70ed6113d8f6facc3dee2cfc076a3bd93dad3520fd5d9d9ae4c48afe56e7b421f5de2adfbc23e450f7a5f71e0afdec047b1ce8d7be62ef754a9d43bf36d9b9e0728fc268cb845b464cce1370573dfafd6c40b2efb98ba1f20c5a63c417264b69d86adb839241dc37d1a7113295a9c51623e51e9408f9623ed49a63a3ba6269172872088213332f38370af530d5be56e54115b0884ace6813911bfc6873bea28207741f4b2471b797bab156e4c6ead91659076553cee1db82c0cebdd17b64802a20c7ee6a3414f959133e6c435efe9241ab7d0203010001a321301f301d0603551d0e041604143806aea351c74f2a8b83fa26c0a9e3d3820b6699300d06092a864886f70d01010b050003820101006ebcc664b996f15c1e03d041eebbdf74a0976d117d68f34d21ef67855b614f5a2bfede66c9d4ea78fe3b50e3673890dfa2eb9eaf4321b30eb76be6f5944004b6501b2629ae4f2c6750f784ea2f9be6c26318258f98772fd3ff0c6ea817fb76d9ae02daa1fa1b91653d531db345f52aa4e7b21e8f92387a2d15d1afd5556213b0c32aadd529bac330516536948bcf85398fb86a65dbae95ef0e5582a87e26b1dbcceeaf77e6e93c63042acdf49c74927561df508020547426ad37776e360feb219523ef4e2a6f5f41a43cd0c0514c53f8644c71014080cfbe036f120a6daad6e12d6b1a07939ca840af2b3373388c0ed6b18594dd838122174304d5eb720f1cef";
            String singnStr = "AQAAAjAwggIsMIIBlaADAgECAgMY2gowDQYJKoZIhvcNAQEFBQAwWzELMAkGA1UEBhMCQ04xCzAJ\nBgNVBAgTAmhlMQwwCgYDVQQHEwNzanoxDDAKBgNVBAoTA2VkdTEPMA0GA1UECxMGc2Nob29sMRIw\nEAYDVQQDEwltYWhvbmd5aW4wHhcNMTgxMDExMDcwMjE1WhcNNDMxMDA1MDcwMjE1WjBbMQswCQYD\nVQQGEwJDTjELMAkGA1UECBMCaGUxDDAKBgNVBAcTA3NqejEMMAoGA1UEChMDZWR1MQ8wDQYDVQQL\nEwZzY2hvb2wxEjAQBgNVBAMTCW1haG9uZ3lpbjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA\nr0aFNvrxBnBEEbAANDcsrmBlcQBGJKsvT5onXngek2ZbkWZx8/1o8nbgCBSjAZvnXEYYjjkC5k+A\nIne1PJUF5bPKTjIQepNmtK+KVHsAJLjn6rG4fQ3oaeu0vvNBehuzt54bACbzkXZj9nV5rs8OllD9\nRronLsOb3DVJ95DyLIMCAwEAATANBgkqhkiG9w0BAQUFAAOBgQCpF6kB++zR0FW4eZaJCEAnQNP0\nGtwAnrXEpvP7ePcakk/JT/e56uTS/OAbpmM/tWETvPtx9hOB4RoPwRl3Q0G1ieCMeVyIABmGAeOk\ntARqtiExfHvorrmk4mxVIiPTwUJSWzAKuhLV93pMxTFZSZK0iTJFVVM/l8Wh3CTdFtpW+w==\n";
            DataInputStream dataInputStream = new DataInputStream(
                    new ByteArrayInputStream(Base64.decode(singnStr, 0)));
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
        if ("getPackageInfo".equals(method.getName())) {//方法名对上
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