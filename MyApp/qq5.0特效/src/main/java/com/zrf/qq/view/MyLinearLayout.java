package com.zrf.qq.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by zrf on 2015/11/8.
 */
public class MyLinearLayout extends LinearLayout {

    private SlideMenu slideMenu;

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MyLinearLayout(Context context) {
        super(context);
    }

    public void setSlideMenu(SlideMenu slideMenu){
        this.slideMenu = slideMenu;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(slideMenu!=null){
            if(slideMenu.getCurrentState()== SlideMenu.DragState.OPEN){
                //拦截当前事件
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(slideMenu!=null){
            if(slideMenu.getCurrentState()== SlideMenu.DragState.OPEN){
                //消费掉当前事件
                slideMenu.close();
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}
