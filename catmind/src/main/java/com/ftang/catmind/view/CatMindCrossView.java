package com.ftang.catmind.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.ftang.catmind.R;

public class CatMindCrossView extends View {

    private Paint mCirclePaint;
    private Paint mCrossPaint;
    private int mCircleRadius;
    private int mCrossLength;
    public CatMindCrossView(Context context) {
        super(context);
        init(null, 0);
        // 将背景设置为透明
        setBackgroundColor(Color.TRANSPARENT);
    }

    public CatMindCrossView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        // 将背景设置为透明
        setBackgroundColor(Color.TRANSPARENT);
    }

    public CatMindCrossView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, 0);
        // 将背景设置为透明
        setBackgroundColor(Color.TRANSPARENT);
    }

    public CatMindCrossView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, 0);
        // 将背景设置为透明
        setBackgroundColor(Color.TRANSPARENT);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // 初始化圆和瞄准镜的画笔
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.BLACK);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(1);

        mCrossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCrossPaint.setColor(Color.RED);
        mCrossPaint.setStrokeWidth(2);

        // 获取圆的半径和瞄准镜的长度
        mCircleRadius = getResources().getDimensionPixelSize(R.dimen.cross_circle_radius);
        mCrossLength = getResources().getDimensionPixelSize(R.dimen.cross_cross_length);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 获取视图的宽度和高度
        int width = getWidth();
        int height = getHeight();

        // 绘制圆
        canvas.drawCircle(width / 2, height / 2, mCircleRadius, mCirclePaint);

        // 绘制瞄准镜
        canvas.drawLine(width / 2 - mCrossLength, height / 2, width / 2 + mCrossLength, height / 2, mCrossPaint);
        canvas.drawLine(width / 2, height / 2 - mCrossLength, width / 2, height / 2 + mCrossLength, mCrossPaint);
    }

}
