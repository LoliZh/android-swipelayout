package com.zrf.qq;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.zrf.qq.view.MyLinearLayout;
import com.zrf.qq.view.SlideMenu;
import com.zrf.qq.view.SwipeLayout;
import com.zrf.qq.view.SwipeLayoutManager;

import java.util.List;
import java.util.Random;


public class MainActivity extends Activity {
    private SlideMenu mSlideMenu;
    private ListView mainList;
    private ListView menuList;
    private ImageView iv_head;
    private MyLinearLayout my_layout;
    private String[] menuContent;
    private String[] mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
        initData();
    }

    //填充数据
    private void initData() {
        menuContent = getResources().getStringArray(R.array.menu_content);
        mainContent = getResources().getStringArray(R.array.main_content);
        mainList.setAdapter(new MainListAdapter());
        menuList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuContent) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = null;
                if (convertView == null) {
                    textView = (TextView) super.getView(position, convertView, parent);
                } else {
                    textView = (TextView) convertView;
                }
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });
        mSlideMenu.setStateChangedListener(new SlideMenu.OnStateChangedListener() {
            @Override
            public void onOpen() {
                //侧边栏展开时走此方法
            }

            @Override
            public void onClose() {
                ViewPropertyAnimator.animate(iv_head).translationXBy(15).setInterpolator(new CycleInterpolator(4)).setDuration(350).start();
            }

            @Override
            public void onDraging(float fraction) {
                ViewHelper.setAlpha(iv_head, 1 - fraction);

            }
        });
        my_layout.setSlideMenu(mSlideMenu);
    }

    @Override
    public void onBackPressed() {
        SwipeLayoutManager swipeLayoutManager = SwipeLayoutManager.getInstance();
        SwipeLayout swipeLayout = swipeLayoutManager.getCurrentSwipeLayout();
        if(swipeLayout!=null){
            swipeLayout.close();
            swipeLayoutManager.clearSwipeLayout();
        }
        super.onBackPressed();
    }

    //初始化控件
    private void assignViews() {
        mSlideMenu = (SlideMenu) findViewById(R.id.slideMenu);
        mainList = (ListView) findViewById(R.id.main_listview);
        menuList = (ListView) findViewById(R.id.menu_listview);
        iv_head = (ImageView) findViewById(R.id.iv_head);
        my_layout = (MyLinearLayout) findViewById(R.id.my_layout);
    }

    private class MainListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mainContent.length;
        }

        @Override
        public String getItem(int position) {
            return mainContent[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getApplicationContext(), R.layout.list_item, null);
            }
            ViewHolder holder = ViewHolder.getViewHolder(convertView);
            holder.tv_name.setText(mainContent[position]);
           // holder.swipeLayout.setTag(position);
            //设置swipelayout的状态改变监听
            holder.swipeLayout.setOnSwipeStateChangeListener(new SwipeLayout.OnSwipeStateChangeListener() {
                @Override
                public void onOpen() {

                }

                @Override
                public void onClose() {

                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        TextView tv_top, tv_delete,tv_name;
        SwipeLayout swipeLayout;
        public ViewHolder(View convertView) {
            tv_top = (TextView) convertView.findViewById(R.id.tv_top);
            tv_delete = (TextView) convertView.findViewById(R.id.tv_delete);
            tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            swipeLayout = (SwipeLayout) convertView.findViewById(R.id.sl_swipe);
        }
        public static ViewHolder getViewHolder(View convertView){
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            if(viewHolder==null){
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            return viewHolder;
        }

    }

}
