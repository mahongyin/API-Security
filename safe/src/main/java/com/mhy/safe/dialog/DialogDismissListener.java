package com.mhy.safe.dialog;

import android.content.DialogInterface;

import com.blankj.utilcode.util.AppUtils;

/**
 * @ProjectName: SafeDemo
 * @Package: com.mhy.safe.dialog
 * @ClassName: DialogDismissListener
 * @Description: java类作用描述
 * @Author: itfitness
 * @CreateDate: 2021/11/1 14:05
 * @UpdateUser: 更新者：
 * @UpdateDate: 2021/11/1 14:05
 * @UpdateRemark: 更新说明：
 */
public class DialogDismissListener implements DialogInterface.OnDismissListener{
    @Override
    public void onDismiss(DialogInterface dialog) {
        AppUtils.exitApp();
    }
}
