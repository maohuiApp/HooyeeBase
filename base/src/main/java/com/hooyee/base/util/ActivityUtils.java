package com.hooyee.base.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.WindowManager;

import java.util.List;

/**
 * This provides methods to help Activities load their UI.
 */
public class ActivityUtils {

    /**
     * 判断是否平板设备
     *
     * @param context
     * @return true:平板,false:手机
     */
    public static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
                Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param context   Context
     * @param className 界面的类名
     * @return 是否在前台显示
     */
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className))
            return false;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName()))
                return true;
        }
        return false;
    }

    /**
     * The {@code fragment} is added to the container view with id {@code frameId}. The operation is
     * performed by the {@code fragmentManager}.
     *
     */
    public static void addFragmentToActivity (@NonNull FragmentManager fragmentManager,
                                              @NonNull Fragment fragment, int frameId) {
        checkNotNull(fragmentManager);
        checkNotNull(fragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(frameId, fragment);
        transaction.commit();
    }

    public static void replaceFragmentOfActivity (@NonNull FragmentManager fragmentManager,
                                                  @NonNull Fragment from, @NonNull Fragment to, int frameId) {
        checkNotNull(fragmentManager);
        checkNotNull(from);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (to.isAdded()) {
            transaction.hide(from).show(to).commit();
        } else {
            transaction.add(frameId, to).hide(from).show(to).commit();
        }
//        transaction.replace(frameId, fragment);
//        transaction.commit();
    }

    public static void hideFragment (@NonNull Fragment fragment) {
        checkNotNull(fragment);
        if(fragment.getFragmentManager() == null) {
            return;
        }
        FragmentTransaction transaction = fragment.getFragmentManager().beginTransaction();
        transaction.hide(fragment);
        transaction.commit();
    }

    public static void showFragment (@NonNull Fragment fragment) {
        checkNotNull(fragment);
        if(fragment.getFragmentManager() == null) {
            return;
        }
        FragmentTransaction transaction = fragment.getFragmentManager().beginTransaction();
        transaction.show(fragment);
        transaction.commit();
    }

    public static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new RuntimeException();
        }
        return obj;
    }


    public static int dp2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        return width;
    }
}
