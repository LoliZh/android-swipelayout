package com.zrf.qq.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;

import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.animation.FloatEvaluator;
import com.nineoldandroids.animation.IntEvaluator;
import com.nineoldandroids.view.ViewHelper;
import com.zrf.qq.utils.ColorUtil;

/**
 * Created by zrf on 2015/11/7.
 */
public class SlideMenu extends FrameLayout {

    private View mainView;
    private View menuView;
    //子控件的拖拽范围
    private float dragRange;
    //SlideMenu的宽度
    private int width;
    private ViewDragHelper dragHelper;
    //float的计算器
    private FloatEvaluator floatEvaluator;
    //int的计算器
    private IntEvaluator intEvaluator;
    //用来记录当前状态
    private DragState mCurrentState = DragState.CLOSE;
    private OnStateChangedListener stateChangedListener;

    //用枚举来表示当前拖拽的状态
    public enum  DragState{
        OPEN,CLOSE;
    }
    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SlideMenu(Context context) {
        super(context);
        init();
    }
    //做些初始化的操作
    private void init() {
        dragHelper = ViewDragHelper.create(this,callback);
        floatEvaluator = new FloatEvaluator();
        intEvaluator = new IntEvaluator();
    }
    /*
    当xml布局的结束标签被读取完成会执行该方法，此时会知道自己有几个子View了
    一般用来初始化子View的引用
     */
    @Override
    protected void onFinishInflate() {
        if (getChildCount() != 2) {
            throw new IllegalArgumentException("SlideMenu can only have 2 childs");
        }
        menuView = getChildAt(0);
        mainView = getChildAt(1);

    }

    /**
     * 该方法在onMeasure执行完之后执行，那么可以在该方法中初始化自己和子View的宽高
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = getMeasuredWidth();
        dragRange = width * 0.6f;
    }
    //由draghelper来判断是否拦截当前事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return dragHelper.shouldInterceptTouchEvent(ev);
    }

    //需要将事件交给draghelper来处理
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dragHelper.processTouchEvent(event);
        return true;
    }

    /**
     * 主要封装了对View的触摸位置，触摸速度，移动距离等的检测和Scroller，通过接口回调的方式告诉我们
     */
    public ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

        /**
         * 用于判断是否捕获当前child的触摸事件 child: 当前触摸的子View return: true:就捕获并解析 false：不处理
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mainView || child == menuView;
        }

        /**
         * 当View被开始捕获和解析的回调 capturedChild：当前被捕获的view
         */
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
            //Log.e("tag", "onViewCaptured");
        }

        /**
         * 获取View水平方向的拖拽范围，但是目前不能限制边界
         * @param child
         * @return 返回的值目前用在手指抬起的时候view缓慢移动的动画世界的计算上面; 最好不要返回0
         */
        @Override
        public int getViewHorizontalDragRange(View child) {
            return (int) dragRange;
        }

        /**
         * 控制子View在水平方向的移动
         * @param child
         * @param left 表示ViewDragHelper认为你想让当前child的left改变的值,left=child.getLeft()+dx
         * @param dx   本次child水平方向移动的距离
         * @return 表示你真正想让child的left变成的值
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if(child==mainView){
                if(left<0){
                    left = 0;
                }else if(left>dragRange){
                    left = (int) dragRange;
                }
            }
            return left;
        }

        /**
         * 当View位置改变时回调此方法，一般用来做其他子View的伴随移动
         * @param changedView  位置改变的child
         * @param left  child当前最新的left
         * @param top   child当前最新的top
         * @param dx   本次水平移动的距离
         * @param dy   本次垂直移动的距离
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if(changedView==menuView){
                left = left-dx;
                //固定住menuView
                menuView.layout(0,0,menuView.getMeasuredWidth(),menuView.getMeasuredHeight());
                int newLeft = mainView.getLeft()+dx;
                if(newLeft<0){
                    newLeft = 0;
                }else if(newLeft>dragRange){
                    newLeft = (int) dragRange;
                }
                mainView.layout(newLeft,mainView.getTop()+dy,newLeft+mainView.getMeasuredWidth(),mainView.getBottom()+dy);
            }
            //计算滑动的百分比
            float fraction = mainView.getLeft() / dragRange;
            //根据滑动的百分比执行动画
            executeAnim(fraction);
            //TODO  接口回调
            if(fraction==0 && mCurrentState == DragState.OPEN){
                //表示当前关闭
                mCurrentState = DragState.CLOSE;
                if(stateChangedListener!=null)
                    stateChangedListener.onClose();
            }else if(fraction==1 && mCurrentState == DragState.CLOSE){
                //打开
                mCurrentState = DragState.OPEN;
                if(stateChangedListener!=null)
                    stateChangedListener.onOpen();
            }else{
                //拖拽中
                if(stateChangedListener!=null)
                    stateChangedListener.onDraging(fraction);
            }

        }

        /**
         * 当释放View后回调此方法
         * @param releasedChild  当前抬起的view
         * @param xvel   x方向的移动的速度  正：向右移动， 负：向左移动
         * @param yvel   y方向移动的速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if(mainView.getLeft()<dragRange/2){
                //在左边，需要关闭
                close();
            }else{
                //需要打开menuview
                open();
            }
            //处理用户的稍微滑动效果
            if(xvel>200 && mCurrentState!=DragState.OPEN){
                //打开
                open();
            }else if(xvel<-200 && mCurrentState!=DragState.CLOSE){
                //关闭
                close();
            }
        }
    };

    /**
     * 执行缩放动画
     * @param fraction 当前滑动的百分比
     */
    private void executeAnim(float fraction) {
        //缩小mainView  fraction:0-1
        ViewHelper.setScaleX(mainView,floatEvaluator.evaluate(fraction,1f,0.8f));
        ViewHelper.setScaleY(mainView,floatEvaluator.evaluate(fraction,1f,0.8f));
        //menuView的动画
        ViewHelper.setTranslationX(menuView,intEvaluator.evaluate(fraction,-menuView.getMeasuredWidth()/2,0));
        ViewHelper.setScaleX(menuView,floatEvaluator.evaluate(fraction,0.8f,1f));
        ViewHelper.setScaleY(menuView,floatEvaluator.evaluate(fraction,0.8f,1f));
        ViewHelper.setAlpha(menuView,floatEvaluator.evaluate(fraction,0.3f,1f));
        //给SlideMenu的背景添加黑色遮罩效果
        getBackground().setColorFilter((Integer) ColorUtil.evaluateColor(fraction, Color.BLACK,Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    //关闭menuView
    public void close(){
        dragHelper.smoothSlideViewTo(mainView,0,mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }
    //打开menuView
    public void open(){
        dragHelper.smoothSlideViewTo(mainView,(int)dragRange,mainView.getTop());
        ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
    }

    @Override
    public void computeScroll() {
        if(dragHelper.continueSettling(true)){
            ViewCompat.postInvalidateOnAnimation(SlideMenu.this);
        }
    }

    public void setStateChangedListener(OnStateChangedListener stateChangedListener){
        this.stateChangedListener = stateChangedListener;
    }

    //定义回调接口
    public interface OnStateChangedListener{
        //menu菜单打开回调该方法
        public void onOpen();
        //menu菜单关闭回调该方法
        public void onClose();
        //拖拽中回调该方法
        public void onDraging(float fraction);
    }

    /**
     * 提供当前SlideMenu的状态
     * @return
     */
    public DragState getCurrentState(){
        return mCurrentState;
    }
}
