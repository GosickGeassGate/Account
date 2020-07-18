package com.bigyao.account;


import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class ClassifyAdapter extends SimpleAdapter {
    private ArrayList<Map<String, Object>> list;    // 数据
    private Context mContext;   // 上下文

    public static int LABEL = -1;

    public ClassifyAdapter(Context context, ArrayList<Map<String, Object>> data, int resource, String[] from, int[] to){
        super(context, data, resource, from, to);

        list = data;
        mContext = context;
    }

    @Override
    public int getCount(){
        return list.size();
    }

    @Override
    public Object getItem(int position){
        return list.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = super.getView(position, convertView, parent);

        // 为了解决复用的问题需要先reset
        view.findViewById(R.id.title).setVisibility(View.VISIBLE);
        view.findViewById(R.id.detail).setVisibility(View.VISIBLE);
        view.findViewById(R.id.income).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.date)).setTextColor(mContext.getResources().getColor(R.color.colorAccent));
        view.setBackgroundColor(Color.WHITE);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        if(((TextView)view.findViewById(R.id.id)).getText().equals(String.valueOf(LABEL))){  // 目录项
            view.findViewById(R.id.title).setVisibility(View.GONE);
            view.findViewById(R.id.detail).setVisibility(View.GONE);
            view.findViewById(R.id.income).setVisibility(View.GONE);

            TextView typeView = (TextView)view.findViewById(R.id.date);
            typeView.setTextColor(Color.GRAY);
            view.setBackgroundColor(Color.rgb(238, 238, 238));
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        return view;
    }
}

