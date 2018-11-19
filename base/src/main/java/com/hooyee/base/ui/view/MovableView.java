package com.hooyee.base.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.hooyee.base.R;

/**
 * @author maohui
 * @date Created on 2018/10/24.
 * @description
 * @added
 */

public class MovableView extends AutoMovableView<String> {
    private TextView mTitleTv;

    public MovableView(@NonNull Context context) {
        super(context);
    }

    public MovableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MovableView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initMovableContent() {
        mTitleTv = findViewById(R.id.tv_title);
    }

    @Override
    public int movableContent() {
        return R.layout.view_auto_movable;
    }

    @Override
    protected void onAnimationRepetition(String s) {
        mTitleTv.setText(s);
    }
}
