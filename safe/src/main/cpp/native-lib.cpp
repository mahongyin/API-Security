#include <jni.h>
#include <string>
#include "android/log.h"
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, "安全", __VA_ARGS__))
const char * SHA1 = "B5:AA:30:95:6A:AA:67:F3:74:FB:CB:91:A6:1C:A2:E2:A8:61:87:8B";
enum DialogType{
    EMULATOR,//模拟器
    JKSERROR,//签名错误
    WIFIPROXY,//WIFI代理
};

void showDialog(JNIEnv *env,DialogType dialogType,jobject activity){
    std::string messageText = "";
    switch (dialogType) {
        case DialogType::EMULATOR:
            messageText = "请不要在模拟器上运行";
            break;
        case DialogType::JKSERROR:
            messageText = "请使用正版应用";
            break;
        case DialogType::WIFIPROXY:
            messageText = "请不要使用网络代理";
            break;
    }

    //创建AlertDialog.Builder
    jclass builderClazz = env->FindClass("android/app/AlertDialog$Builder");
    jmethodID initMethodID = env->GetMethodID(builderClazz,"<init>","(Landroid/content/Context;)V");
    jobject builder = env->NewObject(builderClazz,initMethodID,activity);
    jmethodID setPositiveButtonMethodID = env->GetMethodID(builderClazz,"setPositiveButton","(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;");

    //设置确认按钮和点击事件
    jclass onClickClazz = env->FindClass("com/mhy/safe/dialog/MDialogClickListener");
    jmethodID onClickInitMethodID = env->GetMethodID(onClickClazz,"<init>", "()V");
    jobject onClickListener = env->NewObject(onClickClazz,onClickInitMethodID);
    std::string buttonText = "确定";
    env->CallObjectMethod(builder,setPositiveButtonMethodID,env->NewStringUTF(buttonText.c_str()),onClickListener);

    //设置标题
    jmethodID setTitleMethodID = env->GetMethodID(builderClazz,"setTitle","(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;");
    std::string titleText = "提示";
    env->CallObjectMethod(builder,setTitleMethodID,env->NewStringUTF(titleText.c_str()));

    //设置提示内容
    jmethodID setMessageMethodID = env->GetMethodID(builderClazz,"setMessage","(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;");
    env->CallObjectMethod(builder,setMessageMethodID,env->NewStringUTF(messageText.c_str()));

    //显示弹窗
    jmethodID createMethodID = env->GetMethodID(builderClazz,"create",
                                                "()Landroid/app/AlertDialog;");
    jobject alertDialog = env->CallObjectMethod(builder,createMethodID);
    jclass alertDialogClazz = env->GetObjectClass(alertDialog);

    //设置Dialog关闭的监听
    jmethodID setOnDismissListenerMethodID = env->GetMethodID(alertDialogClazz,"setOnDismissListener",
                                                              "(Landroid/content/DialogInterface$OnDismissListener;)V");
    jclass onDismissListenerClazz = env->FindClass("com/mhy/safe/dialog/DialogDismissListener");
    jmethodID onDismissListenerInitMethodID = env->GetMethodID(onDismissListenerClazz,"<init>", "()V");
    jobject onDismissListener = env->NewObject(onDismissListenerClazz,onDismissListenerInitMethodID);
    env->CallVoidMethod(alertDialog,setOnDismissListenerMethodID,onDismissListener);

    //显示
    jmethodID showMethodID = env->GetMethodID(alertDialogClazz,"show", "()V");
    env->CallVoidMethod(alertDialog,showMethodID);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_mhy_safe_utils_CheckUtil_init(JNIEnv *env, jclass clazz, jint layout_id,
                                                 jobject activity) {
    //设置布局文件
    jclass activityClazz = env->GetObjectClass(activity);
    //获取Java层方法的id
    char* methodName = "setContentView";
    char* methodSig = "(I)V";
    jmethodID setContentViewMethodId = env->GetMethodID(activityClazz,methodName,methodSig);
    //调用方法
    env->CallVoidMethod(activity, setContentViewMethodId,layout_id);


    //检测是否是模拟器
    jclass deviceUtilsClazz = env->FindClass("com/blankj/utilcode/util/DeviceUtils");
    jmethodID isEmulatorMethodId = env->GetStaticMethodID(deviceUtilsClazz,"isEmulator", "()Z");
    jboolean isEmulator = env->CallStaticBooleanMethod(deviceUtilsClazz,isEmulatorMethodId);
    if(isEmulator){
        showDialog(env,DialogType::EMULATOR,activity);
        return;
    }

    //检测签名是否正确
    jclass appUtilsClazz = env->FindClass("com/blankj/utilcode/util/AppUtils");
    jmethodID getAppSignaturesSHA1MethodId = env->GetStaticMethodID(appUtilsClazz,"getAppSignaturesSHA1", "()Ljava/util/List;");
    jobject sha1ArrayList = env->CallStaticObjectMethod(appUtilsClazz,getAppSignaturesSHA1MethodId);
    jclass listClazz = env->FindClass("java/util/List");
    jmethodID getMethodId = env->GetMethodID(listClazz,"get", "(I)Ljava/lang/Object;");
    jstring javaSha1 = (jstring)env->CallObjectMethod(sha1ArrayList,getMethodId,0);
    char * cSha1 = (char*)env->GetStringUTFChars(javaSha1,0);
    //将获取到的SHA-1值与正确签名的SHA-1值比较
    if(strcmp(cSha1,SHA1) != 0){
        showDialog(env,DialogType::JKSERROR,activity);
        return;
    }


    //检测是否使用了WIFI代理
    jclass wifiUtilsClazz = env->FindClass("com/mhy/safe/utils/WifiUtil");
    jmethodID isWifiProxyMethodId = env->GetStaticMethodID(wifiUtilsClazz,"isWifiProxy",
                                                           "(Landroid/content/Context;)Z");
    jboolean isWifiProxy = env->CallStaticBooleanMethod(wifiUtilsClazz,isWifiProxyMethodId,activity);
    if(isWifiProxy){
        showDialog(env,DialogType::WIFIPROXY,activity);
        return;
    }
}