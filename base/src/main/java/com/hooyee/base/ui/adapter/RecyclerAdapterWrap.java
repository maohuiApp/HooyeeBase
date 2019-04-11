package com.hooyee.base.ui.adapter;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author maohui
 * @date Created on 2018/11/13.
 * @description 一个用于给RecyclerAdapter包装成带有添加头尾，固定的loadmore等功能的wrap
 * @added
 */

public class RecyclerAdapterWrap extends RecyclerView.Adapter {
    private static final int BASE_ITEM_TYPE_HEADER = 10000;
    private static final int BASE_ITEM_TYPE_FOOTER = 20000;
    private static final int BASE_ITEM_TYPE_LOAD_MORE = -1001;
    private static final int BASE_ITEM_TYPE_HAD_COMPLETED = -2001;
    private static final int BASE_ITEM_TYPE_LOAD_ERROR = -3001;

    public static final int STATUS_INIT = 0x100;
    public static final int STATUS_LOADING = 0x101;
    public static final int STATUS_IDLE = 0x102;
    public static final int STATUS_LOAD_ERROR = 0x103;
    public static final int STATUS_LOAD_ALL_COMPLETE = 0x104;

    private @Status
    int status = STATUS_INIT;

    protected SparseArrayCompat<RecyclerView.ViewHolder> mHeaderViews = new SparseArrayCompat<>();
    protected SparseArrayCompat<RecyclerView.ViewHolder> mFooterViews = new SparseArrayCompat<>();
    private RecyclerView.ViewHolder mLoadMoreView;
    private RecyclerView.ViewHolder mHadCompletedView;
    private RecyclerView.ViewHolder mLoadErrorView;

    private RecyclerView.Adapter mAdapter;

    private OnLoadMoreListener onLoadMoreListener;
    private RecyclerView.AdapterDataObserver innerAdapterObserver;

    public RecyclerAdapterWrap(@NonNull RecyclerView.Adapter adapter) {
        this.mAdapter = adapter;
        innerAdapterObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                notifyDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                notifyItemRangeChanged(getHeadersCount() + positionStart, itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                notifyItemRangeChanged(getHeadersCount() + positionStart, itemCount, payload);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                notifyItemRangeInserted(getHeadersCount() + positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                notifyItemRangeRemoved(getHeadersCount() + positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                notifyItemMoved(getHeadersCount() + fromPosition, getHeadersCount() + toPosition);
            }
        };
    }

    public void addHeaderView(RecyclerView.ViewHolder view) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
    }

    public void removeHeadView(RecyclerView.ViewHolder view) {
        for (int i = 0; i < mHeaderViews.size(); i++) {
            RecyclerView.ViewHolder holder = mHeaderViews.valueAt(i);
            if (holder == view) {
                mHeaderViews.removeAt(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void addFooterView(RecyclerView.ViewHolder view) {
        mFooterViews.put(mFooterViews.size() + BASE_ITEM_TYPE_FOOTER, view);
    }

    public void setLoadMoreView(RecyclerView.ViewHolder view) {
        mLoadMoreView = view;
    }

    public void setHadCompletedView(RecyclerView.ViewHolder view) {
        mHadCompletedView = view;
    }


    public RecyclerView.ViewHolder getLoadErrorView() {
        return mLoadErrorView;
    }

    public void setLoadErrorView(RecyclerView.ViewHolder mLoadErrorView) {
        this.mLoadErrorView = mLoadErrorView;
    }

    public OnLoadMoreListener getOnLoadMoreListener() {
        return onLoadMoreListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    public @Status
    int getStatus() {
        return status;
    }

    public void setStatus(@Status int status) {
        this.status = status;
        notifyLast();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            RecyclerView.ViewHolder holder = mHeaderViews.get(viewType);
            return holder;
        }
        if (mFooterViews.get(viewType) != null) {
            RecyclerView.ViewHolder holder = mFooterViews.get(viewType);
            return holder;
        }
        if (viewType == BASE_ITEM_TYPE_LOAD_MORE) {
            return mLoadMoreView;
        }
        if (viewType == BASE_ITEM_TYPE_HAD_COMPLETED) {
            return mHadCompletedView;
        }
        if (viewType == BASE_ITEM_TYPE_LOAD_ERROR) {
            return mLoadErrorView;
        }
        return mAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (isHeaderViewPos(position)) {
            return;
        }
        if (isFooterViewPos(position)) {
            return;
        }
        if (isLoadMoreItem(position)) {
            if (onLoadMoreListener != null && status != STATUS_LOADING) {
                onLoadMoreListener.onLoadMore();
                status = STATUS_LOADING;
            }
            return;
        }
        if (isLoadErrorItem(position)) {
            return;
        }
        if (isHadCompletedItem(position)) {
            return;
        }
        mAdapter.onBindViewHolder(viewHolder, position - mHeaderViews.size());
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public int getFooterCount() {
        return mFooterViews.size();
    }

    public boolean isCanLoadMore() {
        if (status == STATUS_IDLE || status == STATUS_INIT || status == STATUS_LOAD_ERROR) {
            return true;
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position);
        }
        if (isFooterViewPos(position)) {
            return mFooterViews.keyAt(position - getHeadersCount() - mAdapter.getItemCount());
        }
        if (isLoadMoreItem(position)) {
            return BASE_ITEM_TYPE_LOAD_MORE;
        }
        if (isHadCompletedItem(position)) {
            return BASE_ITEM_TYPE_HAD_COMPLETED;
        }
        if (isLoadErrorItem(position)) {
            return BASE_ITEM_TYPE_LOAD_ERROR;
        }
        return mAdapter.getItemViewType(position - getHeadersCount());
    }

    @Override
    public int getItemCount() {
        if ((status == STATUS_IDLE || status == STATUS_LOADING) && mLoadMoreView != null)
            return getHeadersCount() + mAdapter.getItemCount() + getFooterCount() + 1;
        if (status == STATUS_LOAD_ALL_COMPLETE && mHadCompletedView != null)
            return getHeadersCount() + mAdapter.getItemCount() + getFooterCount() + 1;
        if (status == STATUS_LOAD_ERROR && mLoadErrorView != null)
            return getHeadersCount() + mAdapter.getItemCount() + getFooterCount() + 1;
        return getHeadersCount() + mAdapter.getItemCount() + getFooterCount();
    }

    private boolean isLoadMoreItem(int position) {
        if ((status == STATUS_IDLE || status == STATUS_LOADING)
                && position - getHeadersCount() - getFooterCount() == mAdapter.getItemCount()
                && mLoadMoreView != null) {
            return true;
        }
        return false;
    }

    private boolean isLoadErrorItem(int position) {
        if (status == STATUS_LOAD_ERROR
                && position - getHeadersCount() - getFooterCount() == mAdapter.getItemCount()
                && mLoadErrorView != null) {
            return true;
        }
        return false;
    }

    private boolean isHadCompletedItem(int position) {
        if (status == STATUS_LOAD_ALL_COMPLETE && position - getHeadersCount() - getFooterCount() == mAdapter.getItemCount() && mHadCompletedView != null) {
            return true;
        }
        return false;
    }

    private boolean isHeaderViewPos(int position) {
        return position < getHeadersCount();
    }

    private boolean isFooterViewPos(int position) {
        int count = getHeadersCount() + mAdapter.getItemCount();
        if ((status == STATUS_IDLE || status == STATUS_LOADING) && mLoadMoreView != null) {
            count++;
        }
        if (status == STATUS_LOAD_ALL_COMPLETE && mHadCompletedView != null) {
            count++;
        }
        if (status == STATUS_LOAD_ERROR && mLoadErrorView != null) {
            count++;
        }
        if (position >= count) {
            return true;
        }
        return false;
    }

    private void notifyLast() {
        if (getItemCount() < 1) return;

        try {
            if ((status == STATUS_LOAD_ALL_COMPLETE && mHadCompletedView == null && mLoadMoreView != null)
                    || (status == STATUS_LOAD_ERROR && mLoadErrorView == null && mLoadMoreView != null)) {
                notifyItemChanged(getItemCount());
            } else {
                notifyItemChanged(getItemCount() - 1);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isFooterViewPos(position) || isHeaderViewPos(position) || fullSpan(position) || isLoadMoreItem(position) || isHadCompletedItem(position)) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (null != lp && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);//占满一行
            }
        } else {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (null != lp && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(false);//占满一行
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mAdapter.registerAdapterDataObserver(innerAdapterObserver);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    // 如果当前是footer的位置，那么该item占据gridManager.getSpanCount()（整行）单元格，正常情况下占据1个单元格
                    return (isFooterViewPos(position) || isHeaderViewPos(position) || fullSpan(position) || isLoadMoreItem(position) || isHadCompletedItem(position)) ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mAdapter.unregisterAdapterDataObserver(innerAdapterObserver);
    }

    private boolean fullSpan(int position) {
        if (mAdapter instanceof IShowRuleWhenGridManager) {
            IShowRuleWhenGridManager rule = (IShowRuleWhenGridManager) mAdapter;
            if (rule.isNormal(position - getHeadersCount())) {
                return true;
            }
        }
        return false;
    }

    public interface IShowRuleWhenGridManager {
        /**
         * @return 当布局为grid的时候，viewholder是占一行还是按照grid的spancount分布;true占一行
         */
        boolean isNormal(int position);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    @IntDef({STATUS_INIT, STATUS_LOADING, STATUS_IDLE, STATUS_LOAD_ERROR, STATUS_LOAD_ALL_COMPLETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {

    }

}
