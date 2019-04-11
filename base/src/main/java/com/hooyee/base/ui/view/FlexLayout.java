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
 * @description 一个简单的会自动换行的布局，子view直接写在布局中即可
 * @added
 */
public class FlexLayout extends ViewGroup {
    /**
     * 一行最多显示多少个子view
     */
    private int mMaxCount;
    private int horizontalSpace;
    private int verticalSpace;

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
        horizontalSpace = typedArray.getDimensionPixelSize(R.styleable.FlexLayout_custom_view_horizontal_space, 0);
        verticalSpace = typedArray.getDimensionPixelSize(R.styleable.FlexLayout_custom_view_vertical_space, 0);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.EXACTLY) {
            int widthSpec = MeasureSpec.makeMeasureSpec((widthSpecSize - horizontalSpace * (mMaxCount - 1)) / mMaxCount, widthSpecMode);
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

            if (line == 1) {
                height = heightTmp;
            }

            if (i + offset >= (mMaxCount * line)) {
                // 换行
                line++;
                height += heightTmp + verticalSpace;
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
                : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int line = 1;
        int count = getChildCount();
        int offset = 0;
        int maxH = 0;
        for (int j = 0; j < count; j++) {
            final View childView = getChildAt(j);
            if (childView.getVisibility() == GONE) {
                offset--;
                continue;
            }
            int w = childView.getMeasuredWidth();
            int h = childView.getMeasuredHeight();
            maxH = maxH > h ? maxH : h;
            if (j % mMaxCount != 0) {
                left += horizontalSpace;
            }
            childView.layout(left, top, left + w, top + h);

            // 控制子view换行
            if (j + offset >= (mMaxCount * line) - 1) {
                line++;
                left = getPaddingLeft();
                top += maxH + verticalSpace;
                maxH = 0;
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
