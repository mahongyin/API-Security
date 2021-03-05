#include <jni.h>
#include <string>
#include <android/log.h>
#include <cstring>
#include <string.h>
#include <dirent.h>
#include <unistd.h>
//系统信息
#include <sys/system_properties.h>

//log定义
#define  LOG    "mhyLog_APISECURITY" // 这个是自定义的LOG的TAG
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__)

#define ALGORITHM_SHA1 "SHA1"
#define ALGORITHM_MD5 "MD5"

//此处改为你的APP签名
#define SHA1 "a8e3d91a4f77dd7ccb8d43ee5046a4b6833f4785"//真实test.keystore
//#define SHA1 "04c1411b0662acd9e4aa300559677e5f106a5255"//区分da小写 55
//此处改为你的APP包名
#define APP_PKG "cn.android.sample"
#define APPLICATION_NAME "cn.android.sample.MyApplication"
//此处填写API盐值
#define API_SECRET "ABC1234567"//设置api 密钥  MD5加盐


static bool isInit = false;
static char *secret;

jint version() {
    // 1. 获取 SDK 版本号 , 存储于 C 字符串 sdk_verison_str 中
    char sdk[128] = "0";
    // 获取版本号方法
    __system_property_get("ro.build.version.sdk", sdk);
    //将版本号转为 int 值
    int sdk_verison = atoi(sdk);
    return sdk_verison;
}

jint getDeviceVersion(JNIEnv *env) {
    jclass build_claz = env->FindClass("android/os/Build$VERSION");
    jfieldID sdk_int = env->GetStaticFieldID(build_claz, "SDK_INT", "I");
    return env->GetStaticIntField(build_claz, sdk_int);
}

//void printByte(JNIEnv *env, jbyteArray jbytes) {
//    //转换成char
//    jsize array_size = env->GetArrayLength(jbytes);
//    jbyte *sha1 = env->GetByteArrayElements(jbytes, nullptr);
//
//    char *hexA = new char[array_size * 2 + 1]();
//    for (int i = 0; i < array_size; ++i) {
//        sprintf(hexA + 2 * i, "%02x", (u_char) sha1[i]);
//    }
//    LOGD("printByte:%s", hexA);
//}

char *digest(JNIEnv *env, const char *algorithm, jbyteArray cert_byte) {
    jclass message_digest_class = env->FindClass("java/security/MessageDigest");
    jmethodID methodId = env->GetStaticMethodID(message_digest_class, "getInstance",
                                                "(Ljava/lang/String;)Ljava/security/MessageDigest;");
    jstring algorithm_jstring = env->NewStringUTF(algorithm);
    jobject digest = env->CallStaticObjectMethod(message_digest_class, methodId, algorithm_jstring);
    methodId = env->GetMethodID(message_digest_class, "digest", "([B)[B");

    jbyteArray sha1_byte = (jbyteArray) env->CallObjectMethod(digest, methodId, cert_byte);
    env->DeleteLocalRef(message_digest_class);

    //转换成char
    jsize array_size = env->GetArrayLength(sha1_byte);
    jbyte *sha1 = env->GetByteArrayElements(sha1_byte, nullptr);
    char *hex = new char[array_size * 2 + 1]();
    for (int i = 0; i < array_size; ++i) {
        sprintf(hex + 2 * i, "%02x", (unsigned char) sha1[i]);
    }
//    LOGD("%s:%s", algorithm, hex);
    return hex;
}


/**
 * 获取PackageManager
 */
jobject getPackageManager(JNIEnv *env, jobject context_object, jclass context_class) {

    jmethodID methodId = env->GetMethodID(context_class, "getPackageManager",
                                          "()Landroid/content/pm/PackageManager;");
    return env->CallObjectMethod(context_object, methodId);
}

/**
 * 获取getPackageName
 */
jstring getPackageName(JNIEnv *env, jclass context_class, jobject context_object) {
    jmethodID methodId = env->GetMethodID(context_class, "getPackageName", "()Ljava/lang/String;");
    jstring packageName = (jstring) env->CallObjectMethod(context_object, methodId);
    return packageName;
}

/**
 * 获取PackageInfo对象  PackageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
 */
jobject getPackageInfo(JNIEnv *env, jobject package_manager, jstring package_name) {
    jclass pack_manager_class = env->GetObjectClass(package_manager);
    jmethodID methodId = env->GetMethodID(pack_manager_class, "getPackageInfo",
                                          "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    env->DeleteLocalRef(pack_manager_class);
    jobject package_info;
    if (version() >= 28) {
        package_info = env->CallObjectMethod(package_manager, methodId, package_name,
                                             0x08000000);//安卓9 0x08000000
    } else {
        package_info = env->CallObjectMethod(package_manager, methodId, package_name,
                                             0x00000040);//安卓9 0x08000000
    }
    return package_info;
}

/**
 *  PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_SIGNATURES);
 * */
jobject getPackageInfoArchive(JNIEnv *env, jobject package_manager, jstring apkPath) {
    jclass pack_manager_class = env->GetObjectClass(package_manager);
    jmethodID methodId = env->GetMethodID(pack_manager_class, "getPackageArchiveInfo",
                                          "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    env->DeleteLocalRef(pack_manager_class);
    jobject package_info = env->CallObjectMethod(package_manager, methodId, apkPath,
                                                 0x00000040);//0x08000000
    return package_info;
}

/**安装目录获取ApplicationInfo
 *  ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(packname, 0);
 *  path = applicationInfo.sourceDir;
 * */
jobject getApplicationInfo(JNIEnv *env, jobject package_manager, jstring package_name) {
    jclass pack_manager_class = env->GetObjectClass(package_manager);
    jmethodID methodId = env->GetMethodID(pack_manager_class, "getApplicationInfo",
                                          "(Ljava/lang/String;I)Landroid/content/pm/ApplicationInfo;");
    env->DeleteLocalRef(pack_manager_class);
    jobject applicationInfo = env->CallObjectMethod(package_manager, methodId, package_name, 0x00);
    return applicationInfo;
}

jstring getApkPath(JNIEnv *env, jobject applicationInfo_object) {
    jclass applicationInfo_class = env->GetObjectClass(applicationInfo_object);//ApplicationInfo
    jfieldID sourceDir = env->GetFieldID(applicationInfo_class, "sourceDir",
                                         "Ljava/lang/String;");//获取此类中的sourceDir成员id   P->publicSourceDir
    env->DeleteLocalRef(applicationInfo_class);
    jstring apkPath = (jstring) env->GetObjectField(applicationInfo_object, sourceDir);
    return apkPath;
}

//context.getPackageResourcePath()
jstring getApkResPath(JNIEnv *env, jclass context_class, jobject context_object) {
    jmethodID methodId = env->GetMethodID(context_class, "getPackageResourcePath",
                                          "()Ljava/lang/String;");
    jstring apkPath = (jstring) env->CallObjectMethod(context_object, methodId);
    return apkPath;
}

//弃用 安装路径 package_info= pm.getPackageInfo(packname, 0x80).applicationInfo
jstring getAbsolutePath(JNIEnv *env, jobject package_info) {
    jclass package_info_class = env->GetObjectClass(package_info);//packageInfo
    jfieldID field = env->GetFieldID(package_info_class, "applicationInfo",
                                     "Landroid/content/pm/ApplicationInfo;");//applicationInfo获取类对象 以获取方法
    env->DeleteLocalRef(package_info_class);
    jobject apppack_info_object = (jstring) env->GetObjectField(package_info,
                                                                field);//applicationInfo
    jclass apppack_info_class = env->GetObjectClass(apppack_info_object);//ApplicationInfo
    jfieldID appfield = env->GetFieldID(apppack_info_class, "sourceDir",
                                        "Ljava/lang/String;");//获取此类中的sourceDir成员id   P->publicSourceDir
    env->DeleteLocalRef(apppack_info_class);
    jstring absoluteapp = (jstring) env->GetObjectField(apppack_info_object, appfield);
    if (absoluteapp == nullptr)
        return env->NewStringUTF("");
    return absoluteapp;
}

/**
 * 获取签名信息
 * ;
    //jint sdk = getDeviceVersion(env);
    jint sdk = version();
    if (sdk >= 28) {
        LOGE("sdk版本%d", sdk);
        signature_object = getSignature28(env, package_info);
    }
 */
//需要sdk28  新API
jobject getSignature28(JNIEnv *env, jobject package_info) {
    jclass package_info_class = env->FindClass("android/content/pm/PackageInfo");
    jfieldID signingInfo = env->GetFieldID(package_info_class, "signingInfo",
                                           "Landroid/content/pm/SigningInfo;");
    env->DeleteLocalRef(package_info_class);
    jobject signingInfo_object = env->GetObjectField(package_info, signingInfo);
    LOGE("%s", "fieldId:signingInfo");
    if (signingInfo_object == nullptr) {
        LOGE("fsigningInfo_object空");
        return nullptr;//为空咋回事？
    }
    jclass signingInfo_class = env->FindClass("android/content/pm/SigningInfo");
    jmethodID methodId = env->GetMethodID(signingInfo_class, "getApkContentsSigners",
                                          "()[Landroid/content/pm/Signature;");
    env->DeleteLocalRef(signingInfo_class);
    jobjectArray signature_object_array = (jobjectArray) env->CallObjectMethod(signingInfo_object,
                                                                               methodId);
    LOGE("%s", "methodId:getApkContentsSigners");
    if (signature_object_array == nullptr){
        LOGE("signature_object_array空");
        return nullptr;
    }
    return env->GetObjectArrayElement(signature_object_array, 0);
}

jobject getSignature(JNIEnv *env, jobject package_info) {
    jclass package_info_class = env->GetObjectClass(package_info);
    jfieldID fieldId = env->GetFieldID(package_info_class, "signatures",
                                       "[Landroid/content/pm/Signature;");
    env->DeleteLocalRef(package_info_class);
    jobjectArray signature_object_array = (jobjectArray) env->GetObjectField(package_info, fieldId);
    if (signature_object_array == nullptr)
        return nullptr;
    return env->GetObjectArrayElement(signature_object_array, 0);
}

jbyteArray getSHA1(JNIEnv *env, jobject signature_object) {
    //签名信息转换成sha1值
    jclass signature_class = env->GetObjectClass(signature_object);
    jmethodID methodId = env->GetMethodID(signature_class, "toByteArray", "()[B");
    env->DeleteLocalRef(signature_class);
    jbyteArray signature_byte = (jbyteArray) env->CallObjectMethod(signature_object, methodId);
    jclass byte_array_input_class = env->FindClass("java/io/ByteArrayInputStream");
    methodId = env->GetMethodID(byte_array_input_class, "<init>", "([B)V");
    jobject byte_array_input = env->NewObject(byte_array_input_class, methodId, signature_byte);
    jclass certificate_factory_class = env->FindClass("java/security/cert/CertificateFactory");
    methodId = env->GetStaticMethodID(certificate_factory_class, "getInstance",
                                      "(Ljava/lang/String;)Ljava/security/cert/CertificateFactory;");
    jstring x_509_jstring = env->NewStringUTF("X.509");
    jobject cert_factory = env->CallStaticObjectMethod(certificate_factory_class, methodId,
                                                       x_509_jstring);
    methodId = env->GetMethodID(certificate_factory_class, "generateCertificate",
                                ("(Ljava/io/InputStream;)Ljava/security/cert/Certificate;"));
    jobject x509_cert = env->CallObjectMethod(cert_factory, methodId, byte_array_input);
    env->DeleteLocalRef(certificate_factory_class);
    jclass x509_cert_class = env->GetObjectClass(x509_cert);
    methodId = env->GetMethodID(x509_cert_class, "getEncoded", "()[B");
    jbyteArray cert_byte = (jbyteArray) env->CallObjectMethod(x509_cert, methodId);
    env->DeleteLocalRef(x509_cert_class);
    return cert_byte;
}

/**
 * apk安装路径 获取签名信息 getApkPathSignatures(Context)
 */
jboolean getApkPathSignatures(JNIEnv *env, jobject context_object) {
    //上下文对象
    jclass context_class = env->GetObjectClass(context_object);

    //反射获取PackageManager
    jobject package_manager = getPackageManager(env, context_object, context_class);
    if (package_manager == nullptr)
        return JNI_FALSE;
    //反射获取包名
    jstring package_name = getPackageName(env, context_class, context_object);
    if (package_name == nullptr)
        return JNI_FALSE;
//插空 替换下面 214-218
    jstring apkPath = getApkResPath(env, context_class, context_object);
    env->DeleteLocalRef(context_class);//等用完再释放啦
//    //获取applictionInfo
//    jobject applicationInfo = getApplicationInfo(env, package_manager, package_name);
//    if (applicationInfo == nullptr)
//        return JNI_FALSE;
//    jstring apkPath = getApkPath(env, applicationInfo);
    if (apkPath == nullptr)
        return JNI_FALSE;
    //获取PackageInfo对象
    jobject package_info = getPackageInfoArchive(env, package_manager, apkPath);
    if (package_info == nullptr)
        return JNI_FALSE;
    env->DeleteLocalRef(package_manager);
    //获取签名信息
    jobject signature_object = getSignature(env, package_info);
    if (signature_object == nullptr)
        return JNI_FALSE;
    env->DeleteLocalRef(package_info);
    jbyteArray cert_byte = getSHA1(env, signature_object);

    char *hex_sha = digest(env, ALGORITHM_SHA1, cert_byte);

    if (strcmp(hex_sha, SHA1) != 0) {//签名不同
        LOGE("非法调用4，SHA1: %s", hex_sha);
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/**
 * 调用 init
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_cn_android_security_APISecurity_init(
        JNIEnv *env,
        jclass clazz,
        jobject context_object) {

    //上下文对象
    jclass context_class = env->GetObjectClass(context_object);

    //反射获取PackageManager
    jobject package_manager = getPackageManager(env, context_object, context_class);
    if (package_manager == nullptr)
        return JNI_FALSE;

    //反射获取包名
    jstring package_name = getPackageName(env, context_class, context_object);
    if (package_name == nullptr)
        return JNI_FALSE;
    env->DeleteLocalRef(context_class);

    //获取PackageInfo对象
    jobject package_info = getPackageInfo(env, package_manager, package_name);
    if (package_info == nullptr){
        LOGE("package_info空");
        return JNI_FALSE;
    }
    env->DeleteLocalRef(package_manager);
//获取签名信息
    jobject signature_object;
    if (version() >= 28) {
        signature_object = getSignature28(env, package_info);
    } else {
        signature_object = getSignature(env, package_info);
    }
    if (signature_object == nullptr){
        LOGE("signature_object空");
        return JNI_FALSE;
    }
    env->DeleteLocalRef(package_info);
    jbyteArray cert_byte = getSHA1(env, signature_object);

    char *hex_sha = digest(env, ALGORITHM_SHA1, cert_byte);

    if (strcmp(hex_sha, SHA1) != 0) {//签名不对
        LOGE("非法调用1，SHA1: %s", hex_sha);
        return JNI_FALSE;
    }
//包名验证
    const char *pkgName = env->GetStringUTFChars(package_name, nullptr);
    if (strcmp(pkgName, APP_PKG) == 0) {
        secret = API_SECRET;//包名匹配 拿取api
    } else {
        LOGE("非法调用2，Package: %s", pkgName);
        return JNI_FALSE;
    }
/*********接着调用Java方法验证 安装目录apk文件de签名**/
    jclass cls_util = env->FindClass(
            "cn/android/security/APISecurity");
    //注意，这里的使用的斜杠而不是点
    if (cls_util == nullptr) {
        return JNI_FALSE;
    }
    jobject j_obj = env->AllocObject(cls_util);
    //**这里是关键**类,方法,(参数类型)返回类型
    jmethodID mtd_static_method = env->GetStaticMethodID(cls_util,
                                                         "getApkSignatures",
                                                         "(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;");
    if (mtd_static_method == nullptr) {
        return JNI_FALSE;
    }
    //调用Java方法
    jstring sigin = (jstring) env->CallStaticObjectMethod(cls_util, mtd_static_method,
                                                          context_object, package_name);
    const char *ss = env->GetStringUTFChars(sigin, nullptr);
    //删除引用
    env->DeleteLocalRef(cls_util);
    env->DeleteLocalRef(j_obj);
//调用Java方法结束
    if (strcmp(ss, SHA1) != 0) {
        LOGE("非法调用3，SHA1: %s", ss);
        return JNI_FALSE;
    }
/*******************调用Java方法结束***/
    //加强验证
    if (!getApkPathSignatures(env, context_object)) {
        return JNI_FALSE;
    }
    isInit = true;
    LOGI("初始化成功！");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_cn_android_security_APISecurity_sign(
        JNIEnv *env,
        jclass clazz,
        jstring str) {

    if (!isInit) {
        LOGE("请先初始化！");

        jclass cls_util = env->FindClass(
                "cn/android/security/APISecurity");//注意，这里的使用的斜杠而不是点
        if (cls_util == nullptr) {
            return env->NewStringUTF("");
        }
        //调用Java 方法
        jobject j_obj = env->AllocObject(cls_util);
        //**这里是关键**GetMethodID是普通方法   GetStaticMethodID静态方法
        jmethodID mtd_static_method = env->GetMethodID(cls_util,
                                                       "javaMethod",
                                                       "(Ljava/lang/String;)V");
        if (mtd_static_method == nullptr) {
            return env->NewStringUTF("");
        }

        jstring data = env->NewStringUTF("验证失败");

        if (data != nullptr) {//nullptr
            //调用Java方法
            env->CallVoidMethod(j_obj, mtd_static_method, data);

            //删除引用
            env->DeleteLocalRef(cls_util);
            env->DeleteLocalRef(j_obj);
            env->DeleteLocalRef(data);
        }
        return env->NewStringUTF("");
    }
//已经通过初始化
    const char *sx;
    sx = env->GetStringUTFChars(str, nullptr);
//通过传来计算
    char *full = new char[strlen(sx) + strlen(secret) + 1]();
    strcat(full, sx);

    strcat(full, secret);

    int len = (jsize) strlen(full);
    jbyteArray array = env->NewByteArray(len);
    env->SetByteArrayRegion(array, 0, len, (jbyte *) full);

    char *sign = digest(env, ALGORITHM_MD5, array);//返回密钥md5

    return env->NewStringUTF(sign);
}

/**1.调试端口检测 读取/proc/net/tcp，查找IDA远程调试所用的23946端口，若发现说明进程正在被IDA调试。*/
void checkPort23946ByTcp() {
    FILE *pfile = nullptr;
    char buf[0x1000] = {0};
// 执行命令//5D8A转化成十进制就是23946
    char *strCatTcp = "cat /proc/net/tcp |grep :5D8A";
//打开进程 去执行 char* strNetstat="netstat |grep :23946";
    pfile = popen(strCatTcp, "r");
    if (nullptr == pfile) {
        LOGD("checkPort23946ByTcp popen打开命令失败!\n");
        return;
    }
// 获取结果
    while (fgets(buf, sizeof(buf), pfile)) {
// 执行到这里，判定为调试状态
        LOGD("执行cat strCatTcp的结果:\n %s", buf);
        isInit = false;
    }//while
    pclose(pfile);//关闭由popen打开的进程
}


/**5.APK线程检测
正常apk进程一般会有十几个线程在运行(比如会有jdwp线程)，
自己写可执行文件加载so一般只有一个线程，
可以根据这个差异来进行调试环境检测
 */

void checkTaskCount() {
    char buf[0x100] = {0};
    char *str = "/proc/%d/task";
    snprintf(buf, sizeof(buf), str, getpid());
// 打开目录:
    DIR *pdir = opendir(buf);
    if (!pdir) {
        perror("checkTaskCount open() fail.\n");
        return;
    }
// 查看目录下文件个数:
    struct dirent *pde = nullptr;
    int Count = 0;
    while ((pde = readdir(pdir))) {
// 字符过滤
        if ((pde->d_name[0] <= '9') && (pde->d_name[0] >= '0')) {
            ++Count;
            LOGD("%d 线程名称:%s\n", Count, pde->d_name);
        }
    }
    LOGD("线程个数为：%d", Count);
    if (1 >= Count) {
// 此处判定为调试状态.
        LOGD("调试状态!");
        isInit = false;
    }

}
/**
 * Application.getClass().getName(); 验证application是否被替换
 */
extern "C"
JNIEXPORT void JNICALL
Java_cn_android_security_APISecurity_verifyApp(JNIEnv *env, jclass clazz,
                                               jobject application_by_reflect) {
    // jclass application_clazz=env->GetObjectClass(application_by_reflect);
    jclass object_clazz = env->FindClass("java/lang/Object");
    jmethodID getClass = env->GetMethodID(object_clazz, "getClass", "()Ljava/lang/Class;");
    jobject clazz_object = env->CallObjectMethod(application_by_reflect, getClass);
    jclass mClazz = env->FindClass("java/lang/Class");
    jmethodID getNameId = env->GetMethodID(mClazz, "getName", "()Ljava/lang/String;");
    jstring appname = (jstring) env->CallObjectMethod(clazz_object, getNameId);
    const char *ss = env->GetStringUTFChars(appname, nullptr);
    if (strcmp(ss, APPLICATION_NAME) != 0) {
        LOGE("非法调用5，SHA1: %s", ss);
        isInit = false;
    }
}