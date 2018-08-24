package com.hooyee.base.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author maohui
 * @date Created on 2018/7/10.
 * @description
 */

public abstract class BaseViewHolder<D> extends RecyclerView.ViewHolder {
    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindData(D data);

}
