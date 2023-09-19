package com.ftang.catmind.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;


public class BoundView extends View {
    private WeakReference<View> targetView;
    private int[] location = new int[2];

    private int statusBarHeight = 0;

    private Paint mPaint;

    {
        mPaint = new Paint();
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        statusBarHeight = getResources().getDimensionPixelSize(
                getResources().getIdentifier("status_bar_height", "dimen", "android"));
    }

    public void setTargetView(WeakReference<View> targetView) {
        this.targetView = targetView;
    }

    public BoundView(Context context) {
        super(context);
    }

    public BoundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (targetView.get() == null)
            return;
        targetView.get().getLocationOnScreen(location);
        location[1] -= statusBarHeight;

        canvas.drawRect(
                location[0],
                location[1],
                location[0] + targetView.get().getWidth(),
                location[1] + targetView.get().getHeight(),
                mPaint
        );
        //如果有子布局，绘制子布局的边界
        if (targetView.get() instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) targetView.get()).getChildCount(); i++) {
                int left = ((ViewGroup) targetView.get()).getChildAt(i).getLeft();
                int top = ((ViewGroup) targetView.get()).getChildAt(i).getTop();
                canvas.drawRect(
                        location[0] + left,
                        location[1] +top,
                        location[0] + left + ((ViewGroup) targetView.get()).getChildAt(i).getWidth(),
                        location[1] + top + ((ViewGroup) targetView.get()).getChildAt(i).getHeight(),
                        mPaint
                );
            }
        }
    }
}
