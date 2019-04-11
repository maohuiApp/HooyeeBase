/*
 * Copyright (c) 2018.
 * last modified : 18-9-19 上午1:01
 * author                             bug                             date                            comment
 * maohui----------------------------------------------------------2018-09-19--------------------------init
 */

package com.hooyee.base.ui.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * RecyclerView的分割线，用于悬浮标题栏的制作
 */
public abstract class NormalDecoration extends RecyclerView.ItemDecoration {
    protected String TAG = "QDX";
    private Paint mHeaderTxtPaint;
    private Paint mHeaderContentPaint;

    protected int headerHeight = 96;//头部高度
    private int textPaddingLeft = 0;//头部文字左边距
    private int textPaddingTop = 0;//头部文字上边距
    private int textPaddingRight = 0;//头部文字上边距
    private int textPaddingBottom = 0;//头部文字上边距
    private int textSize = 40;
    private int textColor = Color.parseColor("#999999");
    private int headerContentColor = 0x000000;
    private final float txtYAxis;
    private RecyclerView mRecyclerView;

    public NormalDecoration(int leftPx, int topPx, int rightPx, int bottomPx) {
        mHeaderTxtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHeaderTxtPaint.setColor(textColor);
//        textSize = ViewCommonUtils.spToPx(QDApplicationContext.getInstance(), 12);
        textSize = 36;
        mHeaderTxtPaint.setTextSize(textSize);
        mHeaderTxtPaint.setTextAlign(Paint.Align.LEFT);


        mHeaderContentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerContentColor = Color.WHITE;
        mHeaderContentPaint.setColor(headerContentColor);
        Paint.FontMetrics fontMetrics = mHeaderTxtPaint.getFontMetrics();
        txtYAxis = fontMetrics.descent - fontMetrics.ascent;
        textPaddingLeft = leftPx;
        textPaddingTop = topPx;

        headerHeight = (int) (txtYAxis + topPx + bottomPx);
    }
    private boolean isInitHeight = false;

    /**
     * 先调用getItemOffsets再调用onDraw
     */
    @Override
    public void getItemOffsets(Rect outRect, View itemView, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, itemView, parent, state);
        if (mRecyclerView == null) {
            mRecyclerView = parent;
        }

        if (headerDrawEvent != null && !isInitHeight) {
            View headerView = headerDrawEvent.getHeaderView(0);
            headerView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            headerHeight = headerView.getMeasuredHeight();
            isInitHeight = true;
        }

        /*我们为每个不同头部名称的第一个item设置头部高度*/
        int pos = parent.getChildAdapterPosition(itemView); //获取当前itemView的位置
        String curHeaderName = getHeaderName(pos);         //根据pos获取要悬浮的头部名

        if (curHeaderName == null) {
            return;
        }
        if (pos == 0 || !curHeaderName.equals(getHeaderName(pos - 1))) {//如果当前位置为0，或者与上一个item头部名不同的，都腾出头部空间
            outRect.top = headerHeight;        //设置itemView PaddingTop的距离
        }
    }

    public abstract String getHeaderName(int pos);

    private SparseArray<Integer> stickyHeaderPosArray = new SparseArray<>();//记录每个头部和悬浮头部的坐标信息【用于点击事件】
    private GestureDetector gestureDetector;


    @Override
    public void onDrawOver(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        super.onDrawOver(canvas, recyclerView, state);
        if (mRecyclerView == null) {
            mRecyclerView = recyclerView;
        }
        if (gestureDetector == null) {
            gestureDetector = new GestureDetector(recyclerView.getContext(), gestureListener);
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }

        stickyHeaderPosArray.clear();

        int childCount = recyclerView.getChildCount();//获取屏幕上可见的item数量
        int left = recyclerView.getLeft() + recyclerView.getPaddingLeft();
        int right = recyclerView.getRight() - recyclerView.getPaddingRight();

        String firstHeaderName = null;
        int firstPos = 0;
        int translateTop = 0;//绘制悬浮头部的偏移量
        /*for循环里面绘制每个分组的头部*/
        for (int i = 0; i < childCount; i++) {
            View childView = recyclerView.getChildAt(i);
            int pos = recyclerView.getChildAdapterPosition(childView); //获取当前view在Adapter里的pos
            String curHeaderName = getHeaderName(pos);                 //根据pos获取要悬浮的头部名
            if (i == 0) {
                firstHeaderName = curHeaderName;
                firstPos = pos;
            }
            if (curHeaderName == null)
                continue;//如果headerName为空，跳过此次循环
            int viewTop = childView.getTop() + recyclerView.getPaddingTop();
            if (pos == 0 || !curHeaderName.equals(getHeaderName(pos - 1))) {//如果当前位置为0，或者与上一个item头部名不同的，都腾出头部空间
                // 绘制每个组头
                if (headerDrawEvent != null) {
                    View headerView;
                    if (headViewMap.get(pos) == null) {
                        headerView = headerDrawEvent.getHeaderView(pos);
                        headerView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                        headerView.setDrawingCacheEnabled(true);
                        headerView.layout(0, 0, right, headerHeight);//布局layout
                        headViewMap.put(pos, headerView);
                        canvas.drawBitmap(headerView.getDrawingCache(), left, viewTop - headerHeight, null);

                    } else {
                        headerView = headViewMap.get(pos);
                        canvas.drawBitmap(headerView.getDrawingCache(), left, viewTop - headerHeight, null);
                    }
                } else {
                    // 绘制背景颜色
                    canvas.drawRect(0, viewTop - headerHeight, right, viewTop, mHeaderContentPaint);
                    // -10 为了给右侧留出边距
                    drawStringSingleLine(canvas, curHeaderName, textPaddingLeft,  viewTop - headerHeight + txtYAxis + textPaddingTop, mHeaderTxtPaint, right - left - 10);
                }
                if (headerHeight < viewTop && viewTop <= 2 * headerHeight) { //此判断是刚好2个头部碰撞，悬浮头部就要偏移
                    translateTop = viewTop - 2 * headerHeight;
                }
                stickyHeaderPosArray.put(pos, viewTop);//将头部信息放进array
                Log.i(TAG, "绘制各个头部" + pos);
            }
        }
        if (firstHeaderName == null)
            return;


        canvas.save();
        canvas.translate(0, translateTop);
        if (headerDrawEvent != null) {//inflater
            View headerView;
            if (headViewMap.get(firstPos) == null) {
                headerView = headerDrawEvent.getHeaderView(firstPos);
                headerView.measure(
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                headerView.setDrawingCacheEnabled(true);
                headerView.layout(0, 0, right, headerHeight);//布局layout
                headViewMap.put(firstPos, headerView);
                canvas.drawBitmap(headerView.getDrawingCache(), textPaddingLeft, 0, null);
            } else {
                headerView = headViewMap.get(firstPos);
                canvas.drawBitmap(headerView.getDrawingCache(), textPaddingLeft, 0, null);
            }
        } else {
            if ( recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int p = layoutManager.findFirstVisibleItemPosition();
                firstHeaderName = getHeaderName(p);
            }
            /*绘制悬浮的头部*/
            canvas.drawRect(0, 0, right, headerHeight, mHeaderContentPaint);
            // -10 为了给右侧留出边距
            drawStringSingleLine(canvas, firstHeaderName, textPaddingLeft,txtYAxis + textPaddingTop, mHeaderTxtPaint, right - left - 10);
//           canvas.drawLine(0, headerHeight / 2, right, headerHeight / 2, mHeaderTxtPaint);//画条线看看文字居中不
        }
        canvas.restore();
        Log.i(TAG, "绘制悬浮头部");
    }

    private Map<Integer, View> headViewMap = new HashMap<>();

    private void drawStringSingleLine(Canvas canvas, String text, float x, float y, Paint paint, float maxLength) {
        String tail = " ------";
        String tmp = text + tail;
        int count = tmp.length();
        if (paint.measureText(tmp, 0, count) > maxLength) {
            tmp = text;
            count = tmp.length();
        } else {
        }
        text = tmp;
        while (paint.measureText(text, 0, count) > maxLength) {
            count--;
        }
        if (count < text.length()) {
            text = text.substring(0, count - 2);
            canvas.drawText(text + "...", x, y, paint);
        } else {
            canvas.drawText(text, x, y, paint);
        }
    }

    private void drawString(Canvas canvas, String text, float x, float y, Paint paint, float maxLength) {
        int count = text.length();
        while (paint.measureText(text, 0, count) > maxLength) {
            count--;
        }
        // 第一行
        canvas.drawText(text, 0, count, x, y, paint);
        // 第二行
        if (count < text.length()) {
            int length = text.length();
            while (paint.measureText(text, count - 1, length) > maxLength) {
                length--;
            }
            // 超出两行
            if (length < text.length()) {
                text = text.substring(count - 1, length - 2);
                float offsetY = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
                canvas.drawText(text + "...", x, y + offsetY, paint);
            } else {
                float offsetY = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
                canvas.drawText(text, count, length, x, y + offsetY, paint);
            }
        }
    }

    public interface OnHeaderClickListener {
        void headerClick(int pos);
    }

    private OnHeaderClickListener headerClickEvent;

    public void setOnHeaderClickListener(OnHeaderClickListener headerClickListener) {
        this.headerClickEvent = headerClickListener;
    }

    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            for (int i = 0; i < stickyHeaderPosArray.size(); i++) {
                int value = stickyHeaderPosArray.valueAt(i);
                float y = e.getY();
                if (value - headerHeight <= y && y <= value) {//如果点击到分组头
                    if (headerClickEvent != null) {
                        headerClickEvent.headerClick(stickyHeaderPosArray.keyAt(i));
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    };

    private OnDecorationHeadDraw headerDrawEvent;

    public interface OnDecorationHeadDraw {
        /**
         *
         * @param pos adapter中的位置
         * @return 自定义头部
         */
        View getHeaderView(int pos);
    }

    /**
     * 自定义头部
     * 注意，不要用这个view去做点击事件处理
     */
    public void setOnDecorationHeadDraw(OnDecorationHeadDraw decorationHeadDraw) {
        this.headerDrawEvent = decorationHeadDraw;
    }

    private Map<String, Drawable> imgDrawableMap = new HashMap<>();

    private Drawable getImg(String url) {
        return imgDrawableMap.get(url);
    }

    public void onDestory() {
        headViewMap.clear();
        imgDrawableMap.clear();
        stickyHeaderPosArray.clear();
        mRecyclerView = null;
        setOnHeaderClickListener(null);
        setOnDecorationHeadDraw(null);
    }


    public void setHeaderHeight(int headerHeight) {
        this.headerHeight = headerHeight;
    }

    public int getHeaderHeight() {
        return headerHeight;
    }

    public void setTextPaddingLeft(int textPaddingLeft) {
        this.textPaddingLeft = textPaddingLeft;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        this.mHeaderTxtPaint.setTextSize(textSize);
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        this.mHeaderTxtPaint.setColor(textColor);
    }

    public void setHeaderContentColor(int headerContentColor) {
        this.headerContentColor = headerContentColor;
        this.mHeaderContentPaint.setColor(headerContentColor);
    }
}