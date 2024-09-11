package com.itfitness.luckpan.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.itfitness.luckpan.R;

import java.util.ArrayList;
/**
 * @ProjectName: LuckPan
 * @Package: com.itfitness.luckpan.widget
 * @ClassName: LuckPan
 * @Description: java类作用描述 ：
 * @Author: 作者名：lml
 * @Author: 作者名：ywj
 * @CreateDate: 2019/3/12 15:50
 * @UpdateUser: 更新者：
 * @UpdateDate: 2019/3/12 15:50
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */



public class LuckPan extends View {
    private Paint mPaintArc;//转盘扇形画笔
    private Paint mPaintItemStr;//转盘文字画笔
    private float mRadius;//圆盘的半径
    private RectF rectFPan;//构建转盘的矩形
    private RectF rectFStr;//构建文字圆盘的矩形

    //转盘会根据传入数量动态调整
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

    private Bitmap bitmap;
    private Paint mPaint;


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
        //在弧形绘制区居中
        mPaintItemStr.setTextAlign(Paint.Align.CENTER);

        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 2;
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_goods, options);

//        Matrix matrix = new Matrix();
//        //让图片转到正确角度，如果切图是横着的，可以调这个来实现效果
//        matrix.postRotate(-90, bitmap.getWidth(), bitmap.getHeight());
//        Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        bitmap.recycle();
//        bitmap = rotateBitmap;
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

    // View 的尺寸发生变化时调用的回调函数。这使得你可以在视图大小发生变化时重新计算和设置一些关键参数
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //计算转盘的半径，取宽度 (w) 和高度 (h) 中的较小值的一半，并乘以 0.9，确保转盘在视图内有一定的边距
        mRadius = Math.min(w, h) / 2 * 0.9f;
        //创建一个 RectF 对象，用于定义转盘绘制的矩形区域。这个矩形以 (0, 0) 为中心，边界为 -mRadius 到 mRadius。
        rectFPan = new RectF(-mRadius, -mRadius, mRadius, mRadius);
        //创建一个用于绘制文本的矩形区域，其大小为转盘半径的 5/7，确保文本不溢出转盘边界
        //TODO 决定文字位置，当然后后面那种缩放法也有效，只是比较麻烦
        rectFStr = new RectF(-mRadius / 10 * 5, -mRadius / 10 * 5, mRadius / 10 * 5, mRadius / 10 * 5);
        //计算每个项目所占的角度。360 度除以项目的数量，确保每个项目有相同的角度分布
        mItemAnge = 360 / mItemStrs.length;
        //根据转盘半径设置文本大小，确保文本相对于转盘的大小适中。文本大小是半径的 1/9
        mTextSize = mRadius / 10;

        mPaintItemStr.setTextSize(mTextSize);
        //计算文本的偏移角度，将项目角度的一半赋值给 mOffsetAngle，用于文本绘制时的对齐，使文本在扇形的中间位置。
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
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setStrokeWidth(5);
            mPaint.setColor(Color.RED);
            mPaint.setAntiAlias(true);
        }
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(-90 - mOffsetAngle);
        drawPanItem(canvas);
        drawText(canvas);
        drawBitmap(canvas);
    }


    private void drawPanItem(Canvas canvas) {
        float startAng = 0;
        for (int x = 0; x < mItemStrs.length; x++) {
            //扇形的两种颜色
            mPaintArc.setColor(x % 2 == 0 ? Color.WHITE : Color.parseColor("#F8864A"));
            Path path = new Path();
            //addArc:传入矩形区域rectFStr画圆弧 ，起始角度 startAng 和扇形的角度 mItemAnge
            //TODO 这个矩形的区域就是整个圆盘
            canvas.drawArc(rectFPan, startAng, mItemAnge, true, mPaintArc);
            //TODO 顺道处理文字轨迹
            path.addArc(rectFStr, startAng, mItemAnge);
            mArcPaths.add(path);
            //下一个起始角度更新
            startAng += mItemAnge;
        }
    }

    private void drawText(Canvas canvas) {
        for (int x = 0; x < mItemStrs.length; x++) {
            Path path = mArcPaths.get(x);
            canvas.drawTextOnPath(mItemStrs[x], path, 0, 0, mPaintItemStr);
        }
    }

    private void drawBitmap(Canvas canvas) {
// 位图的宽度和高度的一半
        int bitmapWidthHalf = bitmap.getWidth() / 2;
        int bitmapHeightHalf = bitmap.getHeight() / 2;

        // 图片应该位于每个扇形的中心，距离圆心一个固定的距离
        float imageRadius = mRadius * 0.75f; // 位图距离圆心的距离，可以根据需要调整

        for (int x = 0; x < mItemStrs.length; x++) {
            // 计算每个扇形的中心角度
            float angle = mStartAngle + mItemAnge * x + mItemAnge / 2;

            // 将角度转换为弧度
            double radians = Math.toRadians(angle);

            // 计算位图的中心点坐标
            float centerX = (float) (imageRadius * Math.cos(radians));
            float centerY = (float) (imageRadius * Math.sin(radians));

            // 创建一个新的矩阵来处理位图的位置和旋转
            Matrix matrix = new Matrix();
            matrix.postTranslate(centerX - bitmapWidthHalf, centerY - bitmapHeightHalf);

            // 旋转位图使其水平居中显示
            matrix.postRotate(angle + 90, centerX, centerY); // 加90度以调整位图的初始旋转方向

            // 在计算出的位置绘制位图
            canvas.drawBitmap(bitmap, matrix, mPaint);
        }
    }

//    private void drawText(Canvas canvas) {
//        // 将 10dp 转换为像素,实现文字距离圆心前进后退
//        float offsetPx = 10 * getResources().getDisplayMetrics().density;
//
//        for (int x = 0; x < mItemStrs.length; x++) {
//            Path path = mArcPaths.get(x);
//
//            // 通过路径位移实现偏移
//            // 这里我们需要从原始路径中复制，增加路径的半径
//            Path outerPath = new Path(path);
//            outerPath.addArc(rectFStr, 0, mItemAnge);
//
//            // todo 通过缩小路径实现文本的外移效果
//            // 为了实际操作，我们需要拓展路径
//            float scale = (mRadius + offsetPx) / mRadius; // 计算新半径的比例
//            canvas.save();
//            canvas.scale(scale, scale); // 应用缩放变化
//            canvas.drawTextOnPath(mItemStrs[x], outerPath, 0, 0, mPaintItemStr);
//            canvas.restore(); // 恢复画布状态
//        }
//    }


    public LuckPanAnimEndCallBack getLuckPanAnimEndCallBack() {
        return luckPanAnimEndCallBack;
    }

    public void setLuckPanAnimEndCallBack(LuckPanAnimEndCallBack luckPanAnimEndCallBack) {
        this.luckPanAnimEndCallBack = luckPanAnimEndCallBack;
    }
}
