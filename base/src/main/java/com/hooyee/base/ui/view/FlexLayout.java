package com.hooyee.base.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.hooyee.base.R;

/**
 * @author maohui
 * @date Created on 2018/11/19.
 * @description 一个会自动换行的布局
 * @added
 */
public class FlexLayout extends ViewGroup {
    private int mMaxCount;

    public FlexLayout(Context context) {
        super(context);
        initView(context, null);
    }

    public FlexLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public FlexLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FlexLayout);
        mMaxCount = typedArray.getInteger(R.styleable.FlexLayout_custom_max_count, 5);
        typedArray.recycle();
    }

    /**
     * 计算控件及子控件所占区域
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY) {
            int widthSpec = MeasureSpec.makeMeasureSpec(widthSpecSize / mMaxCount, widthSpecMode);
            int heightSpec = MeasureSpec.makeMeasureSpec(heightSpecSize, heightSpecMode);
            measureChildren(widthSpec, heightSpec);
        } else {
            measureChildren(widthMeasureSpec, heightMeasureSpec);
        }

        int height = 0;
        int width = 0;
        int heightTmp = 0;
        int widthTmp = 0;
        int offset = 0;
        int line = 1;
        // 进1
        int lines = (getVisibleCount() + mMaxCount - 1) / mMaxCount;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childHeight = getChildAt(i).getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
                heightTmp = heightTmp > childHeight ? heightTmp : childHeight;
                widthTmp += getChildAt(i).getMeasuredWidth() + getPaddingLeft() + getPaddingRight();
            } else {
                offset--;
            }

            if (i + offset >= (mMaxCount * line)) {
                // 换行
                line++;
                height += heightTmp;
                heightTmp = 0;
                width = width > widthTmp ? width : widthTmp;
                widthTmp = 0;
            }
        }

        if (getVisibleCount() <= mMaxCount) {
            width = widthTmp;
            height = heightTmp;
        }

        setMeasuredDimension((widthSpecMode == MeasureSpec.EXACTLY) ? widthSpecSize
                : width, (heightSpecMode == MeasureSpec.EXACTLY) ? heightSpecSize
                : height * lines);
    }

    /**
     * 控制子控件的换行
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = 0;
        int top = 0;
        int line = 1;
        int count = getChildCount();
        int offset = 0;
        for (int j = 0; j < count; j++) {
            final View childView = getChildAt(j);
            if (childView.getVisibility() == GONE) {
                offset--;
                continue;
            }
            int w = childView.getMeasuredWidth();
            int h = childView.getMeasuredHeight();

            childView.layout(left, top, left + w, top + h);

            if (j + offset >= (mMaxCount * line) - 1) {
                line++;
                left = 0;
                top += h;
            } else {
                left += w;
            }
        }
    }

    private int getVisibleCount() {
        int count = getChildCount();
        int result = 0;
        for (int i = 0; i < count; i++) {
            View v = getChildAt(i);
            if (v.getVisibility() != GONE) {
                result++;
            }
        }
        return result;
    }

}
