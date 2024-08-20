package com.itfitness.luckpan.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class LuckPan extends View {
    private Paint mPaintArc;//转盘扇形画笔
    private Paint mPaintItemStr;//转盘文字画笔
    private float mRadius;//圆盘的半径
    private RectF rectFPan;//构建转盘的矩形
    private RectF rectFStr;//构建文字圆盘的矩形
    private String[] mItemStrs = {"俯卧撑30个", "波比跳15个", "卷腹30个", "高抬腿30下", "深蹲30下", "开合跳30下"};
    private ArrayList<Path> mArcPaths = new ArrayList<>();
    private float mItemAnge;
    private int mRepeatCount = 4;//转几圈
    private int mLuckNum = 2;//最终停止的位置
    private float mStartAngle = 0;//存储圆盘开始的位置
    private float mOffsetAngle = 0;//圆盘偏移角度（当Item数量为4的倍数的时候）
    private float mTextSize = 20;//文字大小
    private ObjectAnimator objectAnimator;
    private LuckPanAnimEndCallBack luckPanAnimEndCallBack;

    public LuckPan(Context context) {
        this(context, null);
    }

    public LuckPan(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LuckPan(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaintArc = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintArc.setStyle(Paint.Style.FILL);

        mPaintItemStr = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintItemStr.setColor(Color.parseColor("#ED2F2F"));
        mPaintItemStr.setStrokeWidth(3);
        mPaintItemStr.setTextAlign(Paint.Align.CENTER);
    }

    public void setItems(String[] items) {
        mItemStrs = items;
        mArcPaths.clear(); // Clear previous paths
        mOffsetAngle = 0;
        mStartAngle = 0;
        mOffsetAngle = 360 / items.length / 2;
        invalidate();
    }

    public void setLuckNumber(int luckNumber) {
        mLuckNum = luckNumber;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRadius = Math.min(w, h) / 2 * 0.9f;
        rectFPan = new RectF(-mRadius, -mRadius, mRadius, mRadius);
        rectFStr = new RectF(-mRadius / 7 * 5, -mRadius / 7 * 5, mRadius / 7 * 5, mRadius / 7 * 5);
        mItemAnge = 360 / mItemStrs.length;
        mTextSize = mRadius / 9;
        mPaintItemStr.setTextSize(mTextSize);
        mOffsetAngle = mItemAnge / 2;
    }

    public void startAnim() {
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }

        float targetAngle = mItemAnge * mLuckNum + mStartAngle % 360;
        objectAnimator = ObjectAnimator.ofFloat(this, "rotation", mStartAngle, mStartAngle - mRepeatCount * 360 - targetAngle);
        objectAnimator.setDuration(4000);
        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (luckPanAnimEndCallBack != null) {
                    luckPanAnimEndCallBack.onAnimEnd(mItemStrs[mLuckNum]);
                }
            }
        });
        objectAnimator.start();
        mStartAngle -= mRepeatCount * 360 + targetAngle;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(-90 - mOffsetAngle);
        drawPanItem(canvas);
        drawText(canvas);
    }

    private void drawText(Canvas canvas) {
        for (int x = 0; x < mItemStrs.length; x++) {
            Path path = mArcPaths.get(x);
            canvas.drawTextOnPath(mItemStrs[x], path, 0, 0, mPaintItemStr);
        }
    }

    private void drawPanItem(Canvas canvas) {
        float startAng = 0;
        for (int x = 0; x < mItemStrs.length; x++) {
            mPaintArc.setColor(x % 2 == 0 ? Color.WHITE : Color.parseColor("#F8864A"));
            Path path = new Path();
            path.addArc(rectFStr, startAng, mItemAnge);
            mArcPaths.add(path);
            canvas.drawArc(rectFPan, startAng, mItemAnge, true, mPaintArc);
            startAng += mItemAnge;
        }
    }

    public LuckPanAnimEndCallBack getLuckPanAnimEndCallBack() {
        return luckPanAnimEndCallBack;
    }

    public void setLuckPanAnimEndCallBack(LuckPanAnimEndCallBack luckPanAnimEndCallBack) {
        this.luckPanAnimEndCallBack = luckPanAnimEndCallBack;
    }
}
