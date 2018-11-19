package com.hooyee.app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author maohui
 * @date Created on 2018/11/9.
 * @description
 * @added
 */

public class MyFragment extends Fragment {


    // 初始化一个List
    private List<HashMap<String, Object>> getData() {
        // 新建一个集合类，用于存放多条数据
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> map = null;
        for (int i = 1; i <= 40; i++) {
            map = new HashMap<String, Object>();
            map.put("title", "人物" + i);
            map.put("time", "9月20日");
            map.put("info", "我通过了你的好友验证请求，现在我们可以开始对话啦");
            map.put("img", R.drawable.ic_next);
            list.add(map);
        }
        return list;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my, container, false);
        ListView listView = v.findViewById(R.id.lv_list);



        String[] strings = {"img","title","info","time"};//Map的key集合数组
        int[] ids = {R.id.img,R.id.title,R.id.info,R.id.time};//对应布局文件的id
        SimpleAdapter simpleAdapter = new SimpleAdapter(getContext(),
                getData(), R.layout.activity_weixin_item, strings, ids);
        listView.setAdapter(simpleAdapter);//绑

        return v;
    }
}
