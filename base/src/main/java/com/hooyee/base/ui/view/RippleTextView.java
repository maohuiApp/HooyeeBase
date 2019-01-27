package com.hooyee.base.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

import com.hooyee.base.R;


/**
 * Created by maohui on 2017/3/6.
 * mail: hooyee01_moly@foxmail.com
 * @descript 点击附带水波纹特效，主要应对设计图上没有给出点击互动效果，同时又改了背景颜色的button时使用（平时自然可以使用）
 *             同时需要注意由于做了onTouch拦截，且未对onLongClick专项处理，故需要长按事件的，需要做处理，类似click的方式即可
 *             不过需要反射拿到ListenerInfo，本人较懒
 */

public class RippleTextView extends android.support.v7.widget.AppCompatTextView {
    private SimpleRippleDrawable rippleDrawable;
    private RippleTextView.IRippleCompleteListener completeListener = new IRippleCompleteListener() {
        @Override
        public void onRippleComplete() {
            if (hasOnClickListeners()) {
                callOnClick();
            }
        }
    };

    public RippleTextView(Context context) {
        this(context, null);
    }

    public RippleTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        int color = Color.parseColor("#dedede");
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleTextView);
            color = typedArray.getColor(R.styleable.RippleTextView_custom_ripple_color, color);
            typedArray.recycle();
        }
        rippleDrawable = new SimpleRippleDrawable(color);
        rippleDrawable.setCallback(this);
        rippleDrawable.setCompleteListener(completeListener);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rippleDrawable.setBounds(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isClickable()) {
            rippleDrawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP){
            float x = event.getX();
            float y = event.getY();
            if (getWidth() < x || getHeight() < y || x < 0 || y < 0) {
                event.setAction(MotionEvent.ACTION_CANCEL);
            }
        }
        if (isClickable()) {
            rippleDrawable.onTouch(event);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == rippleDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        rippleDrawable.detach();
    }

    class SimpleRippleDrawable extends Drawable {
        static final int ANIMATION_DURATION = 300;
        static final int ANIMATION_DURATION_LONG = 5000;
        static final int STATUS_CANCEL = -1;
        static final int STATUS_NORMAL = 1;


        private IRippleCompleteListener completeListener;

        private Paint paint;
        private float pointX;
        private float pointY;
        private ObjectAnimator mAnimator;
        private float radius;
        private int status;

        public SimpleRippleDrawable(int color) {
            paint = new Paint();
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setAlpha(50);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawCircle(pointX, pointY, radius, paint);
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        public void onTouch(MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    pointX = event.getX();
                    pointY = event.getY();
                    status = STATUS_NORMAL;
                    startAnimation(ANIMATION_DURATION_LONG);
                    break;
                case MotionEvent.ACTION_UP:
                    status = STATUS_NORMAL;
                    startAnimation(ANIMATION_DURATION);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    status = STATUS_CANCEL;
                    mAnimator.cancel();
                    break;
            }
        }

        public void startAnimation(int duration) {
            if (mAnimator == null) {
                Rect r = getBounds();
                mAnimator = ObjectAnimator.ofFloat(this, "radius", radius, r.right + 100);
                mAnimator.setInterpolator(new LinearInterpolator());
                mAnimator.setDuration(duration);
                mAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setRadius(0);
                        mAnimator.setDuration(ANIMATION_DURATION_LONG);
                        if (getCompleteListener() != null && status != STATUS_CANCEL) {
                            getCompleteListener().onRippleComplete();
                        }
                    }
                });
                mAnimator.start();
            } else {
                float fraction = mAnimator.getAnimatedFraction();
                float hasProcessed = fraction * mAnimator.getDuration();
                int d = (int) (hasProcessed + (1 - fraction) * duration);
                mAnimator.setDuration(d);
                if (!mAnimator.isStarted() && !mAnimator.isRunning()) {
                    mAnimator.start();
                }
            }
        }

        public IRippleCompleteListener getCompleteListener() {
            return completeListener;
        }

        public void setCompleteListener(IRippleCompleteListener completeListener) {
            this.completeListener = completeListener;
        }

        public void detach() {
            status = STATUS_CANCEL;
            if (mAnimator != null) {
                mAnimator.cancel();
            }
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
            invalidateSelf();
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }
    }

    interface IRippleCompleteListener {
        void onRippleComplete();
    }
}
