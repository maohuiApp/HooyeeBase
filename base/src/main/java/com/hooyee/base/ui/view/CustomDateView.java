package com.hooyee.base.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hooyee.base.R;

import java.util.Locale;

/**
 * @author maohui
 * @date Created on 2018/12/18.
 * @description
 * @added
 */

public class CustomDateView extends LinearLayout {
    private TextView mDayTv;
    private TextView mHourTv;
    private TextView mMinuteTv;
    private TextView mSecondTv;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            long timeLimit = (long) msg.obj;
            if (timeLimit <= 0) {
            } else {
                long day = timeLimit / (3600 * 24);
                String hour = String.format(Locale.CHINA, "%02d", timeLimit / 3600 % 24);
                String minute = String.format(Locale.CHINA, "%02d", timeLimit % 3600 / 60);
                String second = String.format(Locale.CHINA, "%02d", timeLimit % 60);
                if (day > 0) {
                    mDayTv.setText(day + "天");
                    mDayTv.setVisibility(VISIBLE);
                } else {
                    mDayTv.setVisibility(GONE);
                }
                mHourTv.setText(hour);
                mMinuteTv.setText(minute);
                mSecondTv.setText(second);
            }
        }
    };

    private Thread mThread;
    private InnerRunnable mRunnable;

    public CustomDateView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public CustomDateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public CustomDateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_custom_date, this);
        TextView tipTv = findViewById(R.id.tv_tip);
        TextView dayTv = findViewById(R.id.tv_day);
        TextView hourTv = findViewById(R.id.tv_hour);
        TextView minuteTv = findViewById(R.id.tv_minute);
        TextView secondTv = findViewById(R.id.tv_second);
        TextView hourSeparatorTv = findViewById(R.id.tv_hour_separator);
        TextView minuteSeparatorTv = findViewById(R.id.tv_minute_separator);

        mDayTv = dayTv;
        mHourTv = hourTv;
        mMinuteTv = minuteTv;
        mSecondTv = secondTv;

        mDayTv.setText("wewr");

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomDateView);
            String tip = typedArray.getString(R.styleable.CustomDateView_custom_tip_text);
            int tipColor = typedArray.getColor(R.styleable.CustomDateView_custom_tip_text_color, Color.GRAY);
            int tipSize = typedArray.getDimensionPixelSize(R.styleable.CustomDateView_custom_tip_text_size, 12);
            int dateColor = typedArray.getColor(R.styleable.CustomDateView_custom_date_text_color, Color.GRAY);
            int dateBackgroundColor = typedArray.getColor(R.styleable.CustomDateView_custom_date_background_color, Color.YELLOW);
            int dateSize = typedArray.getDimensionPixelSize(R.styleable.CustomDateView_custom_date_text_size, 12);
            int separatorColor = typedArray.getColor(R.styleable.CustomDateView_custom_separator_color, Color.YELLOW);
            int drawableId = typedArray.getResourceId(R.styleable.CustomDateView_custom_date_background, 0);

            tipTv.setTextColor(tipColor);
            tipTv.setText(tip);
            tipTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tipSize);

            dayTv.setTextColor(dateColor);
            hourTv.setTextColor(dateColor);
            minuteTv.setTextColor(dateColor);
            secondTv.setTextColor(dateColor);

            dayTv.setBackgroundColor(dateBackgroundColor);
            hourTv.setBackgroundColor(dateBackgroundColor);
            minuteTv.setBackgroundColor(dateBackgroundColor);
            secondTv.setBackgroundColor(dateBackgroundColor);
            dayTv.setBackgroundResource(drawableId);

            dayTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize);
            hourTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize);
            minuteTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize);
            secondTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dateSize);

            hourSeparatorTv.setTextColor(separatorColor);
            minuteSeparatorTv.setTextColor(separatorColor);
            typedArray.recycle();
        }
    }

    public void setTime(long second) {
        if (mThread == null) {
            mRunnable = new InnerRunnable(second);
            mThread = new Thread(mRunnable);
            mThread.start();
        } else {
            mRunnable.second = second;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mThread != null) {
            mThread.interrupt();
        }
    }

    private class InnerRunnable implements Runnable {
        long second;

        public InnerRunnable(long second) {
            this.second = second;
        }

        @Override
        public void run() {
            while (second >= 0) {
                if (Thread.interrupted()) {
                    return;
                }
                Message message = new Message();
                message.obj = second;
                mHandler.sendMessage(message);
                try {
                    Thread.sleep(1000);
                    second--;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
