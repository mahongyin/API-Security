package com.mhy.safe.base;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mhy.safe.utils.CheckUtil;

/**
 * @ProjectName: SafeDemo
 * @Package: com.mhy.safe.base
 * @ClassName: BaseActivity
 * @Description: java类作用描述
 * @Author: itfitness
 * @CreateDate: 2021/11/1 11:36
 * @UpdateUser: 更新者：
 * @UpdateDate: 2021/11/1 11:36
 * @UpdateRemark: 更新说明：
 */
public abstract class BaseActivity extends AppCompatActivity {
    @LayoutRes
    protected abstract int getLayoutId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CheckUtil.init(getLayoutId(),this);
    }
}
