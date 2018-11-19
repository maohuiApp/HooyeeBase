package com.hooyee.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hooyee.base.helper.MessageContentObserver;
import com.hooyee.base.ui.view.MovableView;

public class MainActivity extends AppCompatActivity {
    public static final int MSG_RECEIVE_CODE = 0x101; //收到短信的验证码
    private MessageContentObserver messageContentObserver;    //回调接口
    TextView mCodeTv;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_RECEIVE_CODE) {
                //设置读取到的内容
                mCodeTv.setText("验证码：" + msg.obj);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCodeTv = findViewById(R.id.tv_code);
        MovableView view = findViewById(R.id.move_view);
        view.addData("test1");
        view.addData("test2");
        view.addData("test3");
        view.addData("test5");
        TimeStamp2Date(10000);

        messageContentObserver = new MessageContentObserver(MainActivity.this, handler);
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, messageContentObserver);


        findViewById(R.id.tv_test1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), ScrollTestActivity.class));
            }
        });

    }

    public String TimeStamp2Date(long timestampString) {
        long timestamp = timestampString;
        String date = new java.text.SimpleDateFormat("HH:mm").format(timestamp);
        Toast.makeText(this, date, Toast.LENGTH_SHORT).show();
        return date;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(messageContentObserver);
    }
}
