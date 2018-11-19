/*
 * Copyright (c) 2018.
 * last modified : 18-10-24 上午11:42
 * author                             bug                             date                            comment
 * maohui----------------------------------------------------------2018-10-24--------------------------init
 */

package com.hooyee.base.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.hooyee.base.R;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * @author maohui
 * @date Created on 2018/10/24.
 * @description
 * @added
 */

public abstract class AutoMovableView<T> extends FrameLayout {
    protected Queue<T> queue = new ArrayDeque<>();
    private boolean actioning;
    private AnimatorSet mAnimatorSet;
    private int duration;
    private int hideTime;
    private int showTime;

    public AutoMovableView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public AutoMovableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AutoMovableView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoMovableView);
            hideTime = typedArray.getInteger(R.styleable.AutoMovableView_custom_hide_time, 5000);
            showTime = typedArray.getInteger(R.styleable.AutoMovableView_custom_show_time, 3000);
            duration = typedArray.getInteger(R.styleable.AutoMovableView_custom_show_time, 1000);
        }
        LayoutInflater.from(context).inflate(movableContent(), this, true);
        setAlpha(0);
        initMovableContent();
    }

    public void addData(T d) {
        queue.add(d);
        startAnimation();
    }

    public void startAnimation() {
        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            AnimatorSet animatorSet1 = new AnimatorSet();
            animatorSet1.setDuration(duration);
            Animator alphaAnim = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f);
            Animator translationAnim = ObjectAnimator.ofFloat(this, "translationY", 0f, 100f);
            animatorSet1.playTogether(alphaAnim, translationAnim);

            AnimatorSet animatorSet2 = new AnimatorSet();
            animatorSet2.setDuration(duration);
            Animator alphaAnim1 = ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f);
            Animator translationAnim1 = ObjectAnimator.ofFloat(this, "translationY", 100f, 0f);
            animatorSet2.playTogether(alphaAnim1, translationAnim1);
            animatorSet2.setStartDelay(showTime);
            mAnimatorSet.playSequentially(animatorSet1, animatorSet2);

            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (animation.isRunning()) {
                        actioning = false;
                        mAnimatorSet.setStartDelay(hideTime);
                        startAnimation();
                    }
                }
            });
        }

        if (!actioning && queue.size() > 0) {
            T t = queue.poll();
            onAnimationRepetition(t);
            mAnimatorSet.start();
            actioning = true;
        }
        return;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
    }

    protected abstract void initMovableContent();

    public abstract @LayoutRes
    int movableContent();

    protected abstract void onAnimationRepetition(T t);

}
