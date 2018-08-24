package com.hooyee.base.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hooyee.base.R;
import com.hooyee.base.util.ActivityUtils;

/**
 * @author maohui
 * @date Created on 2018/7/10.
 * @description
 */

public class LeftRightAlignTextView extends ConstraintLayout {
    private TextView mRightTv;
    private TextView mLeftTv;
    private EditText mRightEdit;
    private View mBottomView;
    private ImageView mNextIv;
    private boolean hasRightEdit;
    private OnClickListenerWrap mOnClickListenerWrap = new OnClickListenerWrap();

    private boolean isOpen;
    private boolean keepText;

    public LeftRightAlignTextView(Context context) {
        this(context, null);
    }

    public LeftRightAlignTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeftRightAlignTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
        super.setOnClickListener(mOnClickListenerWrap);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_left_right_align_textview, this);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LeftRightAlignTextView);
        String leftText = typedArray.getString(R.styleable.LeftRightAlignTextView_custom_left_text);
        String rightText = typedArray.getString(R.styleable.LeftRightAlignTextView_custom_right_text);
        Drawable rightIcon = typedArray.getDrawable(R.styleable.LeftRightAlignTextView_custom_right_icon);
        boolean hasNextIcon = typedArray.getBoolean(R.styleable.LeftRightAlignTextView_custom_has_next_icon, true);
        boolean rightEdit = typedArray.getBoolean(R.styleable.LeftRightAlignTextView_custom_right_edit, false);
        boolean singleLine = typedArray.getBoolean(R.styleable.LeftRightAlignTextView_custom_single_line, true);
        keepText = typedArray.getBoolean(R.styleable.LeftRightAlignTextView_custom_keep_text_from_tv, false);
        int maxLength = typedArray.getInteger(R.styleable.LeftRightAlignTextView_custom_right_edit_max_length, Integer.MAX_VALUE);

        TextView leftTx = findViewById(R.id.tx_left_tip);
        TextView rightTx = findViewById(R.id.tx_right_tip);
        EditText rightEv = findViewById(R.id.edit_right_tip);
        ImageView nextIcon = findViewById(R.id.iv_next);

        // 默认只显示一行
        if (!singleLine) {
            rightTx.setGravity(Gravity.RIGHT);
            rightEv.setGravity(Gravity.RIGHT);
        }
        rightEv.setSingleLine(singleLine);

        rightEv.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        if (!hasNextIcon) {
            nextIcon.setVisibility(View.GONE);
            LayoutParams params = (LayoutParams) ((View) (rightTx.getParent())).getLayoutParams();
            params.rightMargin = 0;
        }

        leftTx.setText(leftText);
        rightTx.setText(rightText);
        if (rightIcon != null) {
            rightTx.setText("");
            rightTx.setCompoundDrawables(rightIcon, null, null, null);
        }

        hasRightEdit = rightEdit;
        mRightTv = rightTx;
        mLeftTv = leftTx;
        mRightEdit = rightEv;
        mNextIv = nextIcon;
        typedArray.recycle();
    }

    @Override
    public void setOnClickListener(@Nullable View.OnClickListener l) {
        mOnClickListenerWrap.wrap(l);
    }

    /**
     * 添加点击后的下拉view，只能有一个view
     *
     * @param child
     */
    @SuppressLint("ResourceType")
    public void addViewToBottom(@NonNull View child) {
        child.setVisibility(GONE);
        if (getChildAt(getChildCount() - 1) == mBottomView) {
            removeView(mBottomView);
        }

        mBottomView = child;
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        child.setLayoutParams(params);
        params.topToBottom = R.id.cl_content;
        params.leftMargin = ActivityUtils.dp2px(getContext(), 8);
        params.rightMargin = ActivityUtils.dp2px(getContext(), 8);
        Drawable drawable = child.getBackground();
        if (drawable == null) {
            drawable = new ColorDrawable(Color.WHITE);
            child.setBackground(drawable);
        }
        child.setAlpha(0);
        child.animate().alpha(1).start();
        drawable.setColorFilter(getResources().getColor(Color.parseColor("#999999")), PorterDuff.Mode.SRC_ATOP);
        super.addView(child, params);
    }

    /**
     * 开关动画
     */
    public void toggle() {
        if (mNextIv.getVisibility() == GONE) return;
        if (isOpen) {
            close();
        } else {
            open();
        }
    }

    private void open() {
        if (isOpen) return;
        if (mBottomView != null) {
            mBottomView.setVisibility(View.VISIBLE);
        }
        mNextIv.animate().rotation(90f).start();
        isOpen = true;
    }

    private void close() {
        if (!isOpen) return;
        if (mBottomView != null) {
            mBottomView.setVisibility(View.GONE);
            if (mBottomView instanceof TextView) {
                String text = ((TextView) mBottomView).getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    setRightText(text);
                }
            } else if (mBottomView instanceof OnInfoUpdateListener) {
                String text = ((OnInfoUpdateListener) mBottomView).onUpdate();
                if (!TextUtils.isEmpty(text)) {
                    setRightText(text);
                }
            }
        }
        mNextIv.animate().rotation(0f).start();
        isOpen = false;
    }

    public void setRightText(String text) {
        mRightTv.setText(text);
    }

    public String getRightText() {
        TextView tv = mRightTv.getVisibility() == GONE ? mRightEdit : mRightTv;
        return tv.getText().toString();
    }

    public boolean isOpen() {
        return isOpen;
    }

    /**
     * 点击事件的包装，用于拦截点击事件，执行自身view必要的一些动画效果和视图切换等·
     */
    private class OnClickListenerWrap implements View.OnClickListener {
        private View.OnClickListener listener;

        public void wrap(View.OnClickListener l) {
            listener = l;
        }

        @Override
        public void onClick(View v) {
            toggle();
            if (hasRightEdit) {
                mRightTv.setVisibility(GONE);
                mRightEdit.setVisibility(VISIBLE);
                mRightEdit.requestFocus();
                final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mRightEdit, InputMethodManager.SHOW_IMPLICIT);

                if (keepText) {
                    mRightEdit.setText(mRightTv.getText());
                    mRightEdit.setSelection(mRightTv.getText().length());
                }
                mRightEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            if ("".equals(mRightEdit.getText().toString().trim())) {
                                mRightEdit.setVisibility(GONE);
                                mRightTv.setVisibility(VISIBLE);
                            }
                        }
                    }
                });
            }
            if (listener != null) {
                listener.onClick(v);
            }
        }
    }

    public void hideRightIcon() {
        mNextIv.setVisibility(GONE);
    }

    public void showRightIcon() {
        mNextIv.setVisibility(VISIBLE);
    }

    public interface OnInfoUpdateListener {
        String onUpdate();
    }

    public void addRightTextChangedListener(TextWatcher watcher) {
        mRightEdit.addTextChangedListener(watcher);
    }
}
