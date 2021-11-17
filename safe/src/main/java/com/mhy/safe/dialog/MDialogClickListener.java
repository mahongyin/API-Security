package com.mhy.safe.dialog;

import android.content.DialogInterface;

/**
 * @ProjectName: SafeDemo
 * @Package: com.mhy.safe.dialog
 * @ClassName: MDialogClickListener
 * @Description: java类作用描述
 * @Author: itfitness
 * @CreateDate: 2021/11/1 12:09
 * @UpdateUser: 更新者：
 * @UpdateDate: 2021/11/1 12:09
 * @UpdateRemark: 更新说明：
 */
public class MDialogClickListener implements DialogInterface.OnClickListener {
    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
    }
}
