package com.leo618.librecorder;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewParent;

/**
 * function:RecorderSurface
 * <p>
 * 1.可以触摸悬浮移动
 *
 * <p>
 * Created by Leo on 2017/11/30.
 */
public class RecorderSurface extends SurfaceView {
    public RecorderSurface(Context context) {
        this(context, null);
    }

    public RecorderSurface(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecorderSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        post(new Runnable() {
            @Override
            public void run() {
                ViewParent parent = getParent();
                if (parent instanceof ViewGroup) {
                    mRootView = (ViewGroup) getParent();
                    checked = true;
                }
            }
        });
    }

    /**
     * 设置是否可以拖拽，默认可以的
     */
    public void setDragEnable(boolean enable) {
        dragEnable = enable;
    }

    private ViewGroup mRootView;
    private boolean checked = false;
    private float dx, dy;
    private boolean dragEnable = true;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!checked || !dragEnable) {
            return super.dispatchTouchEvent(event);
        } else {
            getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    dx = event.getRawX();
                    dy = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float diffX = event.getRawX() - dx;
                    float diffY = event.getRawY() - dy;
                    float l = getLeft() + diffX;
                    float b = getBottom() + diffY;
                    float r = getRight() + diffX;
                    float t = getTop() + diffY;
                    //超出屏幕判断
                    if (l < 0) {
                        l = 0;
                        r = l + getWidth();
                    }
                    if (t < 0) {
                        t = 0;
                        b = t + getHeight();
                    }
                    if (r > mRootView.getRight()) {
                        r = mRootView.getRight();
                        l = r - getWidth();
                    }
                    //FIXME need -view.height
                    if (b > mRootView.getBottom()) {
                        b = mRootView.getBottom();
                        t = b - getHeight();
                    }
                    layout((int) l, (int) t, (int) r, (int) b);
                    dx = event.getRawX();
                    dy = event.getRawY();
                    postInvalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        }
    }
}
