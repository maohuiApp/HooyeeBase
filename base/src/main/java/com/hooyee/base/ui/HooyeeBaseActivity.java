package com.hooyee.base.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.hooyee.base.util.ActivityUtils;
import com.hooyee.base.R;
import com.hooyee.base.ui.mvp.BaseFragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author maohui
 * @date Created on 2018/7/6.
 * @description
 */

public class HooyeeBaseActivity extends AppCompatActivity {
    protected Toolbar mToolbar;
    protected TextView mTitleTx;

    protected BaseFragment mFragment;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_base);
        if (!hideTitle()) {
            initToolbar();
        }
    }

    /**
     * 是否显示标题栏的返回键
     *
     * @return
     */
    protected boolean isHaveBackButton() {
        return false;
    }

    protected boolean isLightToolbar() {
        return true;
    }

    protected boolean isWebActivity() {
        return false;
    }

    public void setTitle(String title) {
        mTitleTx.setText(title);
    }

    /**
     * 是否显示toolbar
     *
     * @return
     */
    protected boolean hideTitle() {
        return false;
    }

    private void initToolbar() {
        ViewStub viewStub;
        if (isWebActivity()) {
//            viewStub = findViewById(R.id.stub_toolbar_web);
            viewStub = findViewById(R.id.stub_toolbar);
        } else {
            viewStub = findViewById(R.id.stub_toolbar);
        }
        mToolbar = (Toolbar) viewStub.inflate();
        mTitleTx = findViewById(R.id.tx_title);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        if (isHaveBackButton()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            getWindow().setStatusBarColor(mToolbar.getDrawingCacheBackgroundColor());
            //设置透明状态栏,这样才能让 ContentView 向上
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
//            mToolbar.setFitsSystemWindows(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            mToolbar.setFitsSystemWindows(true);
        }

        if (isLightToolbar()) {
            setStatusBarFontDark(true);
            changeTitleColor(Color.BLACK);
            mToolbar.setBackgroundColor(Color.WHITE);
            // 修改返回按钮颜色
            Drawable upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            mToolbar.requestLayout();
//            if (isWebActivity()) {
//                TextView v = mToolbar.findViewById(R.id.tv_top_back);
//                v.setTextColor(Color.BLACK);
//                Drawable back = v.getCompoundDrawables()[0];
//                back.setColorFilter(getResources().getColor(R.color.ablack), PorterDuff.Mode.SRC_ATOP);
//                v.setCompoundDrawables(back, null, null, null);
//                v = mToolbar.findViewById(R.id.tv_top_close);
//                v.setTextColor(Color.BLACK);
//                v = mToolbar.findViewById(R.id.tv_top_share);
//                v.setTextColor(Color.BLACK);
//                Drawable share = ContextCompat.getDrawable(this, R.drawable.share);
//                share.setColorFilter(getResources().getColor(R.color.ablack), PorterDuff.Mode.SRC_ATOP);
//                v = mToolbar.findViewById(R.id.tv_top_share);
//                v.setCompoundDrawables(share, null, null, null);
//            }
        }
    }

    public void changeTitleColor(int color) {
        if (hideTitle()) return;
        mTitleTx.setTextColor(color);
    }

    /**
     * 修改状态栏的信息文字的颜色为黑色
     *
     * @param dark
     */
    protected void setStatusBarFontDark(boolean dark) {
        // 小米MIUI
        try {
            Window window = getWindow();
            Class clazz = getWindow().getClass();
            Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            if (dark) {    //状态栏亮色且黑色字体
                extraFlagField.invoke(window, darkModeFlag, darkModeFlag);
            } else {       //清除黑色字体
                extraFlagField.invoke(window, 0, darkModeFlag);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 魅族FlymeUI
        try {
            Window window = getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (dark) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            window.setAttributes(lp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // android6.0+系统
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dark) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    protected void addLayoutToBase(@LayoutRes int layoutResID) {
        FrameLayout llContent = findViewById(R.id.contentFrame); //v_content是在基类布局文件中预定义的layout区域
//        通过LayoutInflater填充基类的layout区域
        View v = LayoutInflater.from(this).inflate(layoutResID, null);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        llContent.addView(v, params);
    }

    /**
     * 初始化fragment
     */
    protected void initContent(Class clazz) {
        mFragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (mFragment == null) {
            mFragment = BaseFragment.newInstance(clazz);
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), mFragment, R.id.contentFrame);
        }
    }

    public static void startActivity(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        context.startActivity(intent);
    }

    public static void startActivityWithClearTask(Context context, Class clazz) {
        Intent intent = new Intent(context, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

}
