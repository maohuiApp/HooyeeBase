package com.hooyee.base.ui.mvp;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by maoh on 2018/7/4.
 */

public abstract class BaseFragment extends Fragment {
    protected static final String PARAM_KEY = "param";
    protected ViewGroup mRootView;
    protected String TAG = getClass().getSimpleName();

    public static BaseFragment newInstance(@NonNull Class clazz) {
        return newInstance(clazz, null);
    }

    public static BaseFragment newInstance(@NonNull Class clazz, Parcelable param) {
        Bundle args = new Bundle();
        args.putParcelable(PARAM_KEY, param);
        BaseFragment fragment = null;
        try {
            Object obj = clazz.newInstance();
            if (obj instanceof BaseFragment) {
                fragment = (BaseFragment) obj;
                fragment.setArguments(args);
            } else {
                throw new IllegalArgumentException("传入类型错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (ViewGroup) inflater.inflate(bindLayoutRes(), container, false);
        } else {
            // 同一个parent不能添加相同的view，因此要先移除
            ViewGroup parent = (ViewGroup) mRootView.getChildAt(0).getParent();
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    protected abstract void initAdapter();

    protected abstract @LayoutRes
    int bindLayoutRes();

    /**
     * 初始化mLayoutId布局文件中的 view
     *
     * @param root
     * @return
     */
    public abstract View initView(View root);

}
