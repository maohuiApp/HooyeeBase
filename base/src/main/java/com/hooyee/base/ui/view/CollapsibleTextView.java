package com.hooyee.base.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.hooyee.base.R;

/**
 * @author maohui
 * @date Created on 2018/12/20.
 * @description 可折叠的TextView，点击展开和折叠
 * @added
 */

public class CollapsibleTextView extends android.support.v7.widget.AppCompatTextView {
    private int foldRes;
    private int unfoldRes;
    private boolean folding = true; // 折叠状态
    private Drawable foldDrawable;
    private Drawable unfoldDrawable;
    private Drawable currentDrawable;
    private int originalHeight;  // 记录原本textView高度 用来做touch判定

    private String foldTip = "";
    private String unfoldTip = "";
    private String currentTip = "";

    private Rect foldTipRect;
    private Rect unfoldTipRect;
    private Rect currentTipRect;

    private Paint tipPaint;
    private float offset;

    private boolean showFold;
    private boolean hasEllipse;

    private int foldMarginTop;
    private int originMaxLines = Integer.MAX_VALUE;

    public CollapsibleTextView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public CollapsibleTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public CollapsibleTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {

        if (attrs != null) {
            tipPaint = new Paint();
            tipPaint.setDither(true);
            tipPaint.setAntiAlias(true);

            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CollapsibleTextView);
            foldRes = typedArray.getResourceId(R.styleable.CollapsibleTextView_custom_fold_icon, R.drawable.icon_middle_down);
            unfoldRes = typedArray.getResourceId(R.styleable.CollapsibleTextView_custom_unfold_icon, R.drawable.icon_middle_up);
            int foldDrawableWidth = typedArray.getDimensionPixelOffset(R.styleable.CollapsibleTextView_custom_fold_icon_width, -1);
            int foldDrawableHeight = typedArray.getDimensionPixelOffset(R.styleable.CollapsibleTextView_custom_fold_icon_height, -1);
            int unfoldDrawableWidth = typedArray.getDimensionPixelOffset(R.styleable.CollapsibleTextView_custom_unfold_icon_width, -1);
            int unfoldDrawableHeight = typedArray.getDimensionPixelOffset(R.styleable.CollapsibleTextView_custom_unfold_icon_height, -1);

            foldTip = typedArray.getString(R.styleable.CollapsibleTextView_custom_fold_tip);
            unfoldTip = typedArray.getString(R.styleable.CollapsibleTextView_custom_unfold_tip);
            float tipSize = typedArray.getDimensionPixelSize(R.styleable.CollapsibleTextView_custom_fold_tip_size, 36);
            int tipColor = typedArray.getColor(R.styleable.CollapsibleTextView_custom_tip_color, Color.GRAY);
            tipPaint.setTextSize(tipSize);
            tipPaint.setColor(tipColor);

            showFold = typedArray.getBoolean(R.styleable.CollapsibleTextView_custom_show_fold, true);
            foldMarginTop = typedArray.getDimensionPixelOffset(R.styleable.CollapsibleTextView_custom_fold_marginTop, 30);

            if (foldTip != null) {
                Rect bounds = new Rect();
                getPaint().getTextBounds(foldTip, 0, foldTip.length(), bounds);
                foldTipRect = bounds;
            }
            if (unfoldTip != null) {
                Rect bounds = new Rect();
                getPaint().getTextBounds(unfoldTip, 0, unfoldTip.length(), bounds);
                unfoldTipRect = bounds;
            }

            if (foldRes != 0) {
                foldDrawable = context.getResources().getDrawable(foldRes);
                foldDrawableWidth = foldDrawableWidth == -1 ? foldDrawable.getIntrinsicWidth() : foldDrawableWidth;
                foldDrawableHeight = foldDrawableHeight == -1 ? foldDrawable.getIntrinsicHeight() : foldDrawableHeight;
                foldDrawable.setBounds(0, 0, foldDrawableWidth, foldDrawableHeight);
            }
            if (unfoldRes != 0) {
                unfoldDrawable = context.getResources().getDrawable(unfoldRes);
                unfoldDrawableWidth = unfoldDrawableWidth == -1 ? unfoldDrawable.getIntrinsicWidth() : unfoldDrawableWidth;
                unfoldDrawableHeight = unfoldDrawableHeight == -1 ? unfoldDrawable.getIntrinsicHeight() : unfoldDrawableHeight;
                unfoldDrawable.setBounds(0, 0, unfoldDrawableWidth, unfoldDrawableHeight);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                originMaxLines = getMaxLines();
            }

            typedArray.recycle();
            statusChange(folding);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (event.getY() > originalHeight - getPaddingBottom()) {
                return true;
            }
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (event.getY() > originalHeight - getPaddingBottom()) {
                folding = !folding;
                statusChange(folding);
                return true;
            }
        }
        return result;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!showFold) return;

        Layout layout = getLayout();//拿到Layout
        int line = getLineCount();//获取文字行数
        if (line > 0) {
            int ellipsisCount = layout.getEllipsisCount(line - 1);
            //ellipsisCount > 0 时，说明省略生效
            if (ellipsisCount > 0 || line > originMaxLines) {
                hasEllipse = true;
            } else {
                hasEllipse = false;
            }
        }
        if (!hasEllipse) return;

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        String text = getText().toString();
        if (MeasureSpec.AT_MOST == heightMode || MeasureSpec.UNSPECIFIED == heightMode) {
            height = getLineHeight() * getLineCount() + foldMarginTop;
        }

        if (MeasureSpec.AT_MOST == widthMode) {
            Rect bounds = new Rect();
            getPaint().getTextBounds(text, 0, text.length(), bounds);
            width = bounds.width() + getPaddingLeft() + getPaddingRight();
        }
        originalHeight = height;
        offset = currentDrawable.getBounds().height() > getTipHeight() ? currentDrawable.getBounds().height() : getTipHeight();
        height += offset + getPaddingBottom();

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!showFold) {
            return;
        } else if (!hasEllipse) {
            return;
        }
        if (currentTip != null) {
            canvas.drawText(currentTip, (getWidth() - currentDrawable.getBounds().width() - getTipWidth()) / 2, originalHeight + offset - tipPaint.getFontMetrics().bottom - (offset - getTipHeight()) / 2, tipPaint);
        }
        canvas.translate((getWidth() - currentDrawable.getBounds().width() -getTipWidth()) / 2 + getTipWidth(), originalHeight + (offset - tipPaint.getFontMetrics().bottom - currentDrawable.getBounds().height()) / 2);
        currentDrawable.draw(canvas);
    }

    private int getTipWidth() {
        if (currentTipRect == null) return 0;
        return currentTipRect.width();
    }

    private int getTipHeight() {
        if (currentTipRect == null) return 0;
        return currentTipRect.height();
    }

    private void statusChange(boolean folding) {
        if (!showFold) return;
        if (folding) {
            currentDrawable = foldDrawable;
            currentTip = foldTip;
            currentTipRect = foldTipRect;
            setMaxLines(originMaxLines);
        } else {
            currentDrawable = unfoldDrawable;
            currentTip = unfoldTip;
            currentTipRect = unfoldTipRect;
            setMaxLines(Integer.MAX_VALUE);
        }
    }
}
