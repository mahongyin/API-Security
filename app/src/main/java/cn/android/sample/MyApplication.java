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

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.android.security.APISecurity;
import cn.android.security.AppSigning;


public class MyApplication extends Application implements InvocationHandler {
    private static final int GET_SIGNATURES = 64;
    private static final int GET_SIGNING_CERTIFICATES = 134217728;
    private String appPkgName = "";
    private File apkPath;//源 apk目录 这样使读取源文件签名达到骗过验证  针对sdk28 新API
    private Object base;//全局sPackageManager对象
    private byte[][] sign;//源 签名数组
    static MyApplication app;

    public MyApplication() {
        // 在构造函数里提早检测
      //  earlyCheckSign();
    }

    void earlyCheckSign() {
        // 手动构造 context
        try {
            Context context = APISecurity.createContext();
            //用新 context 校验签名的过程(正常的检测一样)
            String sing = APISecurity.getInstalledAPKSignature(context, context.getPackageName());
            Log.e("mhyLog手动构造", sing);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void copyFile(Context context, final String fileName) {
        //data/app/packagename/lib/arm64/libold.so  将源文件搞到此目录
        String libPath = context.getApplicationInfo().nativeLibraryDir+File.separator+fileName;
        apkPath=new File(libPath);
        Log.e("mhyLog","File:"+libPath);
        if (!apkPath.exists()){
           throw new RuntimeException("old.so未就位，libs/ABI对应目录/libold.so");
        }
    }
    @Override
    protected void attachBaseContext(Context context) {
        //在这里hook 签名校验被
        hook(context);
        super.attachBaseContext(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //再这看 不一定靠谱
        //在签名校验被hook 之后重置PackageManager
        /*在这里 重置PackageManager 只要在验证前重置即可*/
       // AppSigning.resetPackageManager(getBaseContext());
    }

    /**
     * 全局hook 签名校验
     *
     * @param context
     */
    @SuppressLint("PrivateApi")
    private void hook(Context context) {
        copyFile(context, "libold.so");//源apk
        try {
//            String realy = "308203273082020fa003020102020477d6d1f6300d06092a864886f70d01010b05003044310c300a06035504061303303231310e300c060355040813056368696e613111300f060355040713087368616e676861693111300f0603550403130877757a6f6e67626f301e170d3139303330353032343132345a170d3434303232373032343132345a3044310c300a06035504061303303231310e300c060355040813056368696e613111300f060355040713087368616e676861693111300f0603550403130877757a6f6e67626f30820122300d06092a864886f70d01010105000382010f003082010a0282010100a3ac52268a32e8420a20a727c184c133d513998a207e198f5a535d628a436ba5e095e7ba3f92535234a83fb6272e70ed6113d8f6facc3dee2cfc076a3bd93dad3520fd5d9d9ae4c48afe56e7b421f5de2adfbc23e450f7a5f71e0afdec047b1ce8d7be62ef754a9d43bf36d9b9e0728fc268cb845b464cce1370573dfafd6c40b2efb98ba1f20c5a63c417264b69d86adb839241dc37d1a7113295a9c51623e51e9408f9623ed49a63a3ba6269172872088213332f38370af530d5be56e54115b0884ace6813911bfc6873bea28207741f4b2471b797bab156e4c6ead91659076553cee1db82c0cebdd17b64802a20c7ee6a3414f959133e6c435efe9241ab7d0203010001a321301f301d0603551d0e041604143806aea351c74f2a8b83fa26c0a9e3d3820b6699300d06092a864886f70d01010b050003820101006ebcc664b996f15c1e03d041eebbdf74a0976d117d68f34d21ef67855b614f5a2bfede66c9d4ea78fe3b50e3673890dfa2eb9eaf4321b30eb76be6f5944004b6501b2629ae4f2c6750f784ea2f9be6c26318258f98772fd3ff0c6ea817fb76d9ae02daa1fa1b91653d531db345f52aa4e7b21e8f92387a2d15d1afd5556213b0c32aadd529bac330516536948bcf85398fb86a65dbae95ef0e5582a87e26b1dbcceeaf77e6e93c63042acdf49c74927561df508020547426ad37776e360feb219523ef4e2a6f5f41a43cd0c0514c53f8644c71014080cfbe036f120a6daad6e12d6b1a07939ca840af2b3373388c0ed6b18594dd838122174304d5eb720f1cef";
//            realy= Base64.encodeToString(realy.getBytes(),Base64.DEFAULT);
            String singnStr = "AQAAAjAwggIsMIIBlaADAgECAgMY2gowDQYJKoZIhvcNAQEFBQAwWzELMAkGA1UEBhMCQ04xCzAJBgNVBAgTAmhlMQwwCgYDVQQHEwNzanoxDDAKBgNVBAoTA2VkdTEPMA0GA1UECxMGc2Nob29sMRIwEAYDVQQDEwltYWhvbmd5aW4wHhcNMTgxMDExMDcwMjE1WhcNNDMxMDA1MDcwMjE1WjBbMQswCQYDVQQGEwJDTjELMAkGA1UECBMCaGUxDDAKBgNVBAcTA3NqejEMMAoGA1UEChMDZWR1MQ8wDQYDVQQLEwZzY2hvb2wxEjAQBgNVBAMTCW1haG9uZ3lpbjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAr0aFNvrxBnBEEbAANDcsrmBlcQBGJKsvT5onXngek2ZbkWZx8/1o8nbgCBSjAZvnXEYYjjkC5k+AIne1PJUF5bPKTjIQepNmtK+KVHsAJLjn6rG4fQ3oaeu0vvNBehuzt54bACbzkXZj9nV5rs8OllD9RronLsOb3DVJ95DyLIMCAwEAATANBgkqhkiG9w0BAQUFAAOBgQCpF6kB++zR0FW4eZaJCEAnQNP0GtwAnrXEpvP7ePcakk/JT/e56uTS/OAbpmM/tWETvPtx9hOB4RoPwRl3Q0G1ieCMeVyIABmGAeOktARqtiExfHvorrmk4mxVIiPTwUJSWzAKuhLV93pMxTFZSZK0iTJFVVM/l8Wh3CTdFtpW+w==";
            DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(Base64.decode(singnStr, Base64.DEFAULT)));
            byte[][] bArr = new byte[(dataInputStream.read() & 255)][];
            for (int i = 0; i < bArr.length; i++) {
                bArr[i] = new byte[dataInputStream.readInt()];
                dataInputStream.readFully(bArr[i]);
            }
//          hook全局sPackageManager对象
            Class<?> cls = Class.forName("android.app.ActivityThread");
            Object invoke = cls.getDeclaredMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field declaredField = cls.getDeclaredField("sPackageManager");
            declaredField.setAccessible(true);
            Object obj = declaredField.get(invoke);
            Class<?> cls2 = Class.forName("android.content.pm.IPackageManager");
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

    @SuppressLint({"PrivateApi","DiscouragedPrivateApi"})
    @Override
    public Object invoke(Object obj, Method method, Object[] objArr) throws Throwable {
        if ("getPackageInfo".equals(method.getName())) {//方法名对上
            Log.e("mhyLogHook","getPackageInfo"+"_flag:"+(int)objArr[1]);
            String packageName = (String) objArr[0];//64 134217728
           // int flag=((Integer) objArr[1]).intValue();
            if (this.appPkgName.equals(packageName)) {
                PackageInfo packageInfo = (PackageInfo) method.invoke(this.base, objArr);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&((int) objArr[1] & GET_SIGNING_CERTIFICATES) != 0) {//a&b!=0 -> a=b

                    Class<?> pkgParserCls = Class.forName("android.content.pm.PackageParser");
                    Constructor<?> pkgParserCt = pkgParserCls.getConstructor();//可以反射任何构造器，可以反射私有构造器
                    Object pkgParser = pkgParserCt.newInstance();//PackageParser()
                    //Package parsePackage(File packageFile, int flags)
                    Method parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", File.class, int.class);
                    Object pkgParserPkg = parsePackageMtd.invoke(pkgParser, apkPath, PackageManager.GET_SIGNING_CERTIFICATES);//Package
                    Method collectCertificatesMtd = pkgParserCls.getDeclaredMethod("collectCertificates", pkgParserPkg.getClass(), Boolean.TYPE);
                    if (!collectCertificatesMtd.isAccessible()){collectCertificatesMtd.setAccessible(true);}
                    //void collectCertificates(Package pkg, boolean skipVerify)
                    collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, true);//执行后 mSigningDetails就获取了值 到这里算是就给赋值了
//                   collectCertificatesMtd.invoke(pkgParser, pkgParserPkg, false);// Build.VERSION.SDK_INT > 28?:

                    // PackageParser$Package里的属性 mSigningDetails public
                    Field mSigningDetailsField = pkgParserPkg.getClass().getDeclaredField("mSigningDetails");
                    mSigningDetailsField.setAccessible(true);
                    //获取PackageParser$Package里mSigningDetails 对象
                    Object mSigningDetails = mSigningDetailsField.get(pkgParserPkg);

                    Class<?> sigInfo_clazz=Class.forName("android.content.pm.SigningInfo");
//                   Class<?> mSigningDetails_clazz=Class.forName("android.content.pm.PackageParser$SigningDetails");
                    /*clazz.newInstance();//只能反射()无参的构造器，需要构造器可见；*/
                    //利用Constructor.newInstance()反射私有构造方法   @hide
                    Constructor<?> sifInfoCt = sigInfo_clazz.getDeclaredConstructor(mSigningDetails.getClass());
                    if(!sifInfoCt.isAccessible())
                        sifInfoCt.setAccessible(true);
                    Object sifInfo_object = sifInfoCt.newInstance(mSigningDetails);//PackageParser()

//                    Constructor<?> sifInfoCt = sigInfo_clazz.getConstructor(mSigningDetails.getClass());
//                    Object sifInfo_object = sifInfoCt.newInstance(mSigningDetails);//可以反射任何构造器，可以反射私有构造器

                    packageInfo.signingInfo= (SigningInfo) sifInfo_object;

                    Field infoField = mSigningDetails.getClass().getDeclaredField("signatures");//final的
                    infoField.setAccessible(true);
                    //验证 是否hook
                    Signature[] info2 = (Signature[]) infoField.get(mSigningDetails);
                    if (info2.length>0){
                    Log.e("mhyLogHook2后",AppSigning.getSignatureString(info2,AppSigning.SHA1));
                    }
                } //else { //双hook
                if ((((Integer) objArr[1]).intValue() & GET_SIGNATURES) != 0){
                    packageInfo.signatures = new Signature[this.sign.length];
                    for (int i = 0; i < packageInfo.signatures.length; i++) {
                        packageInfo.signatures[i] = new Signature(this.sign[i]);
                    }
                    Log.e("mhyLogHook1后",AppSigning.getSignatureString(packageInfo.signatures,AppSigning.SHA1));
                }
                return packageInfo;
            }
        }
        // IPackageManager接口中无getPackageArchiveInfo 方法无法代理
        return method.invoke(this.base, objArr);
    }


}