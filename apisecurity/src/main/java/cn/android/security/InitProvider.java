/*
 * Copyright (c) 2020-2021.  安卓
 * FileName: ${NAME}
 * Author: ${USER}
 * Date: ${DATE} ${TIME}
 * Description: ${DESCRIPTION}
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 * 本代码未经许可，不得私自修改何使用
 */

package cn.android.security;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created By Mahongyin
 * Date    2021/3/2 17:24
 * ContentProvider的onCreate会先于Appliction的onCreate调用  防止application被掉包后 仍有这里可以重置pm代理+检测
 */
public class InitProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        Context application = getContext().getApplicationContext();
        if (application == null) {
            application = getApplicationByReflect();
        }
        Log.e("mhyLog", "initContentProvider:");
//        chekSignature(application);
        checkApplication();
        return true;
    }
    void chekSignature(Context application){
        //防hook 并检测
        if (AppSigning.checkPMProxy(application)) {
            AppSigning.resetPackageManager(application);
            String sing = APISecurity.getInstalledAPKSignature(application, application.getPackageName());
            Log.e("mhyLog手动InitProvider", sing);
        }

    }
    /**
     * 校验 application
     */
    private boolean checkApplication(){
        //在这里使用反射 获取比较靠谱 如果 被替换换 就查出来了
        Application nowApplication = getApplicationByReflect();
        String trueApplicationName = "MyApplication";//自己的Application类名 防止替换
        String nowApplicationName = nowApplication.getClass().getSimpleName();
        Log.e("mhyLogAppName", "反射获取:"+nowApplicationName);
        return trueApplicationName.equals(nowApplicationName);
    }

    /**
     * 通过反射获取 当前application
     */
    @SuppressLint("PrivateApi")
    public Application getApplicationByReflect() {
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

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return getType(uri);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return insert(uri, values);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return delete(uri, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return update(uri, values, selection, selectionArgs);
    }


}

