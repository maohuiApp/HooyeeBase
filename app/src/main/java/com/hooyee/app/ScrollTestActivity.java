package com.hooyee.app;

import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ScrollView;

public class ScrollTestActivity extends AppCompatActivity {

    ImageView mBottomIv;
    ScrollView scrollView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_test);
        scrollView = findViewById(R.id.sv_scroll);

        mBottomIv = findViewById(R.id.iv_bottom);
        mBottomIv.post(new Runnable() {
            @Override
            public void run() {
                Rect r = new Rect();
                mBottomIv.getHeight();
                int[] position = new int[2];
                mBottomIv.getLocationInWindow(position);
                mBottomIv.getLocalVisibleRect(r);
                scrollView.scrollTo(position[0], position[1]);
            }
        });

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fl_bottom);
        if (null == fragment) {
            fragment = new MyFragment();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.add(R.id.fl_bottom, fragment);
            transaction.commit();
        }
    }
}
