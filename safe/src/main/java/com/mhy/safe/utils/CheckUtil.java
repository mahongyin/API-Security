package com.mhy.safe.utils;

import android.app.Activity;

/**
 * @ProjectName: SafeDemo
 * @Package: com.mhy.safe.utils
 * @ClassName: CheckUtil
 * @Description: java类作用描述
 * @Author: itfitness
 * @CreateDate: 2021/11/1 11:34
 * @UpdateUser: 更新者：
 * @UpdateDate: 2021/11/1 11:34
 * @UpdateRemark: 更新说明：
 */
public class CheckUtil {
    static {
        System.loadLibrary("native-lib");
    }

    public native static void init(int layoutId, Activity activity);
}
