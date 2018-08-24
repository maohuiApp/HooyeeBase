package com.hooyee.base.ui.adapter;

import android.view.View;

/**
 * @author maohui
 * @date Created on 2018/7/10.
 * @description
 */

public class HeaderViewHolder<D> extends BaseViewHolder<D> {
    public HeaderViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bindData(D data) {

    }

    public void setOnClickListener(View.OnClickListener listener) {
        itemView.setOnClickListener(listener);
    }

    public void onDestroy() {

    }
}
