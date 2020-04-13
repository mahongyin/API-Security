#include <jni.h>
#include <string>
#include <android/log.h>

//log定义
#define  LOG    "APISECURITY" // 这个是自定义的LOG的TAG
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG,__VA_ARGS__)
#define LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG,__VA_ARGS__)

//此处改为你的APP签名
#define SHA1 "a8e3d91a4f77dd7ccb8d43ee5046a4b6833f4785"//区分小写
//#define SHA1 "04c1411b0662acd9e4aa300559677e5f106a5255"
#define ALGORITHM_SHA1 "SHA1"
#define ALGORITHM_MD5 "MD5"

//此处改为你的APP包名
#define APP_PKG "cn.android.sample"
//此处填写API盐值
#define API_SECRET "ABC1234567"//设置api 密钥  返回md5


static bool isInit = false;
static char *secret;


//void printByte(JNIEnv *env, jbyteArray jbytes) {
//    //转换成char
//    jsize array_size = env->GetArrayLength(jbytes);
//    jbyte *sha1 = env->GetByteArrayElements(jbytes, NULL);
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
    jbyte *sha1 = env->GetByteArrayElements(sha1_byte, NULL);
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
 * 获取PackageInfo对象
 */
jobject getPackageInfo(JNIEnv *env, jobject package_manager, jstring package_name) {
    jclass pack_manager_class = env->GetObjectClass(package_manager);
    jmethodID methodId = env->GetMethodID(pack_manager_class, "getPackageInfo",
                                          "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    env->DeleteLocalRef(pack_manager_class);
    jobject package_info = env->CallObjectMethod(package_manager, methodId, package_name, 0x40);//安卓10 0x80
    return package_info;
}
//目录获取PackageInfo
jobject getPackageArchiveInfo(JNIEnv *env, jobject package_manager, jstring absolutePath) {
    jclass pack_manager_class = env->GetObjectClass(package_manager);
    jmethodID methodId = env->GetMethodID(pack_manager_class, "getPackageArchiveInfo",
                                          "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    env->DeleteLocalRef(pack_manager_class);
    jobject package_info = env->CallObjectMethod(package_manager, methodId, absolutePath, 0x80);
    return package_info;
}
//安装路径
jstring getAbsolutePath(JNIEnv *env, jobject package_info) {//packageInfo
    jclass package_info_class = env->GetObjectClass(package_info);//applicationInfo获取类对象 以获取方法
    //pack_manager_class.getApplicationInfo()
    jfieldID field = env->GetFieldID(package_info_class,"applicationInfo","Landroid/content/pm/ApplicationInfo;");//获取此类中的sourceDir成员id
    jobject apppack_info_object=(jstring)env ->GetObjectField(package_info,field);//applicationInfo

    jclass apppack_info_class = env->GetObjectClass(apppack_info_object);//ApplicationInfo
    jfieldID appfield = env->GetFieldID(apppack_info_class,"sourceDir","Ljava/lang/String;");//获取此类中的sourceDir成员id   P->publicSourceDir
//    env->DeleteLocalRef(pack_manager_class);
    jstring absoluteapp=(jstring)env ->GetObjectField(apppack_info_object,appfield);
    if (absoluteapp == NULL)
        return env->NewStringUTF("");
    return absoluteapp;
}

/**
 * 获取签名信息
 */
jobject getSignature(JNIEnv *env, jobject package_info) {
    jclass package_info_class = env->GetObjectClass(package_info);
    jfieldID fieldId = env->GetFieldID(package_info_class, "signatures",
                                       "[Landroid/content/pm/Signature;");
    env->DeleteLocalRef(package_info_class);
    jobjectArray signature_object_array = (jobjectArray) env->GetObjectField(package_info, fieldId);
    if (signature_object_array == NULL)
        return NULL;
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


extern "C" JNIEXPORT jboolean JNICALL
Java_cn_android_security_APISecurity_init(
        JNIEnv *env,
        jclass clazz,
        jobject context_object) {

    //上下文对象
    jclass context_class = env->GetObjectClass(context_object);

    //反射获取PackageManager
    jobject package_manager = getPackageManager(env, context_object, context_class);
    if (package_manager == NULL)
        return JNI_FALSE;

    //反射获取包名
    jstring package_name = getPackageName(env, context_class, context_object);
    if (package_name == NULL)
        return JNI_FALSE;
    env->DeleteLocalRef(context_class);

    //获取PackageInfo对象
    jobject package_info = getPackageInfo(env, package_manager, package_name);
    if (package_info == NULL)
        return JNI_FALSE;
    env->DeleteLocalRef(package_manager);

    //获取签名信息
    jobject signature_object = getSignature(env, package_info);
    if (signature_object == NULL)
        return JNI_FALSE;
    env->DeleteLocalRef(package_info);
    jbyteArray cert_byte = getSHA1(env, signature_object);

    char *hex_sha = digest(env, ALGORITHM_SHA1, cert_byte);

    if (strcmp(hex_sha, SHA1) != 0) {//签名不对
        LOGE("非法调用1，SHA1: %s:%s", hex_sha, SHA1);
        return JNI_FALSE;
    }

    const char *pkgName = env->GetStringUTFChars(package_name, NULL);

    if (strcmp(pkgName, APP_PKG) == 0) {
        secret = API_SECRET;//包名匹配 拿取api
    } else {
        LOGE("非法调用2，Package: %s:%s", pkgName, APP_PKG);
        return JNI_FALSE;
    }
// 接着调用Java方法验证 安装目录文件签名
    jclass cls_util = env->FindClass(
            "cn/android/security/APISecurity");//注意，这里的使用的斜杠而不是点
    if (cls_util == NULL) {
        return JNI_FALSE;
    }
    jobject j_obj = env->AllocObject(cls_util);//**这里是关键**类,方法,(参数类型)返回类型
    jmethodID mtd_static_method = env->GetStaticMethodID(cls_util,
            "getApkSignatures",
            "(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;");
    if (mtd_static_method == NULL) {
        return JNI_FALSE;
    }
    //调用Java方法
   jstring sigin= static_cast<jstring>(env->CallStaticObjectMethod(cls_util, mtd_static_method,
                                                                   context_object, package_name));
    const char *ss=env->GetStringUTFChars(sigin,NULL);
    //删除引用
    env->DeleteLocalRef(cls_util);
    env->DeleteLocalRef(j_obj);
//调用Java方法结束
    if (strcmp(ss, SHA1) != 0) {
        LOGE("非法调用3，SHA1: %s:%s", ss, SHA1);
        return JNI_FALSE;
    }
    //
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
            if (cls_util == NULL) {
                return env->NewStringUTF("");
            }
            jobject j_obj = env->AllocObject(cls_util);//**这里是关键**
            jmethodID mtd_static_method = env->GetMethodID(cls_util,
                                                           "javaMethod",
                                                           "(Ljava/lang/String;)V");
            if (mtd_static_method == NULL) {
                return env->NewStringUTF("");
            }

         jstring data = env->NewStringUTF("noinit");

        if (data == NULL) {}//调用Java方法
            env->CallVoidMethod(/*context_object*/j_obj, mtd_static_method, data);

            //删除引用
            env->DeleteLocalRef(cls_util);
            env->DeleteLocalRef(j_obj);
            env->DeleteLocalRef(data);

        return env->NewStringUTF("");
    }
//已经通过初始化
    const char *sx;
    sx = env->GetStringUTFChars(str, NULL);
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

//extern "C"
//JNIEXPORT jboolean JNICALL
//Java_cn_android_security_APISecurity_check(JNIEnv *env, jclass clazz, jstring str) {
//    const char *sx;
//    sx = env->GetStringUTFChars(str, NULL);
//    char name[512];
//    strcpy(name, sx);
//
//    return 0 == strcmp(SHA1, "md5");
//}
//APISecurity.adbshell("pm list package -3",getFilesDir().getPath() + File.separator + "files"+File.separator + "adbshell.txt");
//extern "C"
//JNIEXPORT jstring JNICALL
//Java_cn_android_security_APISecurity_adbshell(JNIEnv *env, jclass clazz, jstring str,jstring path) {
//    //    int ret= system("pm list package -3");//获取安装应用
//
//    char* str2=(char *)env->GetStringUTFChars(str, NULL);
//    char* path2=(char *)env->GetStringUTFChars(path, NULL);
//    strcat(str2," > ");
//    strcat(str2,path2);
//    system(str2);
//    return env->NewStringUTF(str2);
//}
