package com.zrf.qq.view;

/**
 * Created by zrf on 2015/11/14.
 */
public class SwipeLayoutManager {
    //用来记录当前打开的滑块
    private SwipeLayout swipeLayout;
    //单例模式
    private static SwipeLayoutManager swipeManager = null;

    private SwipeLayoutManager() {
    }

    public static SwipeLayoutManager getInstance() {
        if (swipeManager == null) {
            synchronized (SwipeLayoutManager.class) {
                if (swipeManager == null) {
                    swipeManager = new SwipeLayoutManager();
                }
            }
        }
        return swipeManager;
    }

    //记录当前打开的滑块
    public void setSwipeLayout(SwipeLayout swipeLayout) {
        this.swipeLayout = swipeLayout;
    }

    //清除当前打开的滑块
    public void clearSwipeLayout() {
        this.swipeLayout = null;
    }

    //判断当前是否应该能够滑动滑块判断当前是否应该能够滑动，如果没有打开的，则可以滑动。
    // 如果有打开的，则判断打开的layout和当前按下的layout是否是同一个
    public boolean isShouldSwipe(SwipeLayout swipeLayout) {
        if (this.swipeLayout == null) {
            //表示当前没有打开的
            return true;
        } else {
            return this.swipeLayout == swipeLayout;
        }
    }

    public SwipeLayout getCurrentSwipeLayout() {
        return this.swipeLayout;
    }

}
