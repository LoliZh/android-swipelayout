package com.zrf.qq.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by zrf on 2015/11/8.
 */
public class SwipeLayout extends FrameLayout {
    //内容区域
    private View contentView;
    //删除区域
    private View deleteView;
    private ViewDragHelper dragHelper;
    //删除区域的宽度
    private int delWidth;
    //内容区域的宽度
    private int contentWidth;
    //内容区域的高度
    private int contentHeight;
    private int delHeight;
    //用来记录当前状态
    private DragState  mCurrentState = DragState.CLOSE;
    private float downX;
    private float downY;
    private SwipeLayoutManager swipeLayoutManager;

    public SwipeLayout(Context context) {
        super(context);
        init();
    }
    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        dragHelper = ViewDragHelper.create(this, callback);
        swipeLayoutManager = SwipeLayoutManager.getInstance();
    }



    @Override
    protected void onFinishInflate() {
        contentView = getChildAt(0);
        deleteView = getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        delWidth = deleteView.getMeasuredWidth();
        delHeight = deleteView.getMeasuredHeight();
        contentWidth = contentView.getMeasuredWidth();
        contentHeight = contentView.getMeasuredHeight();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        contentView.layout(0,0,contentWidth,contentHeight);
        deleteView.layout(contentWidth,0,contentWidth+delWidth,delHeight);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //先判断当前是否可以滑动
        if(!swipeLayoutManager.isShouldSwipe(SwipeLayout.this)){
            //不可以滑动
            requestDisallowInterceptTouchEvent(true);
            //将滑块关闭
            swipeLayoutManager.getCurrentSwipeLayout().close();
            return true;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                float disX = moveX-downX;
                float disY = moveY-downY;
                if(Math.abs(disX)>Math.abs(disY)){
                    //当前偏向于水平移动，请求listView不要拦截事件
                    requestDisallowInterceptTouchEvent(true);
                }
                downX = moveX;
                downY = moveY;
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        dragHelper.processTouchEvent(event);
        return true;
    }


    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child==contentView || child==deleteView;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return delWidth;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(child == contentView){
                if(left<-delWidth){
                    left =-delWidth;
                }else if(left>0){
                    left = 0;
                }
            }else if(child == deleteView){
                if(left<contentWidth-delWidth){
                    left = contentWidth-delWidth;
                }else if(left>contentWidth){
                    left = contentWidth;
                }
            }
            return left;
        }

        /**
         * 用来做伴随移动
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if(changedView == contentView){
                //表示当前移动的是contentView，手动移动deleteView
                deleteView.layout(deleteView.getLeft()+dx,top,deleteView.getRight()+dx,deleteView.getBottom()+dy);
            }else if(changedView == deleteView){
                //手动移动contentView
                contentView.layout(contentView.getLeft()+dx,top,contentView.getRight()+dx,contentView.getBottom()+dy);
            }

            //判断打开和关闭的逻辑
            if(contentView.getLeft()==0&&mCurrentState!=DragState.CLOSE){
                mCurrentState = DragState.CLOSE;
                if(swipeStateChangeListener!=null){
                    swipeStateChangeListener.onClose();
                }
                //清除已经记录的打开的滑块
                swipeLayoutManager.clearSwipeLayout();
            }else if(contentView.getLeft()==-delWidth&&mCurrentState!=DragState.OPEN){
                mCurrentState = DragState.OPEN;
                if(swipeStateChangeListener!=null){
                    swipeStateChangeListener.onOpen();
                }
                //记录该滑块为当前已经打开的
                swipeLayoutManager.setSwipeLayout(SwipeLayout.this);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if(contentView.getLeft()<-delWidth/2){
                //打开删除区域
                open();
            }else{
                //关闭删除区域
                close();
            }
            //处理用户的稍微滑动效果
            //Log.e("tag","xvel:"+xvel);
            if(xvel>200 && mCurrentState!=DragState.CLOSE){
                //关闭
                close();
            }else if(xvel<-200 && mCurrentState!=DragState.OPEN){
                //打开
                open();
            }
        }
    };

    public enum DragState{
        OPEN,CLOSE;
    }
    public void open() {
        dragHelper.smoothSlideViewTo(contentView,-delWidth,contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
    }

    public void close(){
        dragHelper.smoothSlideViewTo(contentView,0,contentView.getTop());
        ViewCompat.postInvalidateOnAnimation(SwipeLayout.this);
    }

    @Override
    public void computeScroll() {
       if(dragHelper.continueSettling(true)){
           ViewCompat.postInvalidateOnAnimation(this);
       }
    }
    //接口回调
    private OnSwipeStateChangeListener swipeStateChangeListener;
    public void setOnSwipeStateChangeListener(OnSwipeStateChangeListener onSwipeStateChangeListener){
        this.swipeStateChangeListener = onSwipeStateChangeListener;
    }
    public interface  OnSwipeStateChangeListener{
        void onOpen();
        void onClose();
    }
}
