/*
 * Copyright (c) 2018.
 * last modified : 18-8-24 下午2:28
 * author                             bug                             date                            comment
 * maohui----------------------------------------------------------2018-07-10--------------------------init
 */

package com.hooyee.base.ui.adapter;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hooyee.base.ui.adapter.BaseViewHolder;
import com.hooyee.base.ui.adapter.HeaderViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maohui
 * @date Created on 2018/7/10.
 * @description
 */

public abstract class BaseAdapter<D> extends RecyclerView.Adapter<BaseViewHolder<D>> implements View.OnClickListener{
    private static final int BASE_ITEM_TYPE_HEADER = 10000;

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected List<D> mData;
    private SparseArrayCompat<HeaderViewHolder> mHeaderViews = new SparseArrayCompat<>();
    private OnItemClickListener<D> mOnItemClickListener;

    public BaseAdapter(Context context, List<D> data) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        if (data == null) data = new ArrayList<>();
        mData = data;
    }

    public void add(D d) {
        if (d == null) return;
        mData.add(d);
        notifyItemInserted(mData.size());
    }

    public void addAll(List<D> data) {
        if (data == null) return;
        mData.addAll(data);
        notifyDataSetChanged();
    }

    public void replaceAll(List<D> data) {
        if (data == null) return;
        mData.clear();
        addAll(data);
    }

    public void clear() {
        if (mData == null) return;
        mData.clear();
        notifyDataSetChanged();
    }

    public void delete(D d) {
        if (d == null) return;
        int index = mData.indexOf(d);
        if (index != -1) {
            mData.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void update(D d) {
        if (d == null) return;
        int index = mData.indexOf(d);
        if (index != -1) {
            mData.add(index, d);
            mData.remove(index + 1);
            notifyItemChanged(getHeadersCount() + index);
        }
    }

    public void setOnItemClickListener(OnItemClickListener<D> onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            int index = (Integer) v.getTag();
            mOnItemClickListener.onItemClick(mData.get(index), index);
        }
    }

    @Override
    public BaseViewHolder<D> onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            HeaderViewHolder holder = mHeaderViews.get(viewType);
            return holder;
        }
        BaseViewHolder viewHolder = OnCreateViewHolder(parent, viewType);
        viewHolder.itemView.setOnClickListener(this);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<D> holder, final int position) {
        if (isHeaderViewPos(position)) {
            return;
        }
        holder.bindData(mData.get(position - getHeadersCount()));
        holder.itemView.setTag(position - getHeadersCount());
    }

    public abstract BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position);
        }
        return super.getItemViewType(position - getHeadersCount());
    }

    @Override
    public int getItemCount() {
        int realSize = mData == null ? 0 : mData.size();
        return getHeadersCount() + realSize;
    }

    private boolean isHeaderViewPos(int position) {
        return position < getHeadersCount();
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public void addHeaderView(HeaderViewHolder view) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
    }

    public interface OnItemClickListener<D> {
        void onItemClick(D data, int position);
    }
}
