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
        if (application == null) {//这时候context有可能没准备好 可以通过反射获取
            application = AppSigning.getApplicationByReflect();
        }
        Log.e("mhyLog", "initContentProvider:");
        chekSignature(application);

        Log.e("mhyLog检测Provider_APP", String.valueOf(AppSigning.checkApplication()));

        return true;
    }

    /**
     * 检查 PM是否被代理！ 被代理我就恢复
     */
    void chekSignature(Context application){
        //防hook 并检测
        if (AppSigning.checkPMProxy(application)) {
            AppSigning.resetPackageManager(application);
            String sing = APISecurity.getInstalledAPKSignature(application, application.getPackageName());
            Log.e("mhyLog手动InitProvider", sing);
        }

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

