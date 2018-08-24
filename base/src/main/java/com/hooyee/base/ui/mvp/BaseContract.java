package com.hooyee.base.ui.mvp;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * @author maohui
 * @date Created on 2018/7/6.
 * @description
 */

public interface BaseContract {
    interface BaseView {

    }

    abstract class BasePresenter<V extends BaseView> {
        protected WeakReference<V> mWeakReference;
        protected WeakReference<Context> mContextRef;

        public BasePresenter(Context context) {
            mContextRef = new WeakReference(context);
        }

        public void attach(V v) {
            mWeakReference = new WeakReference(v);
        }

        public void deAttach() {
            if (mWeakReference != null) {
                mWeakReference.clear();
                mWeakReference = null;
            }
        }

        public V getView() {
            return mWeakReference != null ? mWeakReference.get() : null;
        }

        public Context getContext() {
            return mContextRef.get();
        }
    }
}
