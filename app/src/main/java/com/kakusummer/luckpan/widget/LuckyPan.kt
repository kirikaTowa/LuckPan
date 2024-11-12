package com.kakusummer.luckpan.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.kakusummer.luckpan.R
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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
class LuckyPan @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {
    private var mPaintArc: Paint? = null //转盘扇形画笔
    private var mPaintItemStr: Paint? = null //转盘文字画笔
    private var mRadius = 0f //圆盘的半径
    private var rectFPan: RectF? = null //构建转盘的矩形
    private var rectFStr: RectF? = null //构建文字圆盘的矩形

    //转盘会根据传入数量动态调整
    private var mItemStrs =
        arrayOf("俯卧撑30个", "波比跳15个", "卷腹30个", "高抬腿30下", "深蹲30下", "开合跳30下")
    private val mArcPaths = ArrayList<Path>()
    private var mItemAnge = 0f
    private val mRepeatCount = 4 //转几圈
    private var mLuckNum = 2 //最终停止的位置
    private var mStartAngle = 0f //存储圆盘开始的位置
    private var mOffsetAngle = 0f //圆盘偏移角度（当Item数量为4的倍数的时候）
    private var mTextSize = 20f //文字大小
    private var objectAnimator: ObjectAnimator? = null

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
    var luckPanAnimEndCallBack: LuckyPanAnimEndCallBack? = null

    private var bitmap: Bitmap? = null
    private var mPaint: Paint? = null


    init {
        init()
    }

    private fun init() {
        mPaintArc = Paint(Paint.ANTI_ALIAS_FLAG)
        //弧形描边
        // mPaintArc?.style = Paint.Style.STROKE
        mPaintArc?.style = Paint.Style.FILL

        mPaintItemStr = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaintItemStr?.apply {
            color = Color.parseColor("#ED2F2F")
            strokeWidth = 3f
            //在弧形绘制区居中
            textAlign = Paint.Align.CENTER
        }


        val options = BitmapFactory.Options()
        //        options.inSampleSize = 2;
        bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_goods, options)

        //        Matrix matrix = new Matrix();
//        //让图片转到正确角度，如果切图是横着的，可以调这个来实现效果
//        matrix.postRotate(-90, bitmap.getWidth(), bitmap.getHeight());
//        Bitmap rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//        bitmap.recycle();
//        bitmap = rotateBitmap;
    }

    fun setItems(items: Array<String>) {
        mItemStrs = items
        mArcPaths.clear() // Clear previous paths
        mOffsetAngle = 0f
        mStartAngle = 0f

        //支持传入item重新绘制 计算新的偏移角度
        if (items.isNotEmpty()) {
            mOffsetAngle = (360f / items.size / 2).toFloat()
            mItemAnge = (360f / items.size).toFloat() // 更新每个项的角度
        }else{
            mOffsetAngle = (360f / items.size / 2).toFloat()
        }

        mOffsetAngle = (360f / items.size / 2).toFloat()
        invalidate()
    }

    fun setLuckNumber(luckNumber: Int) {
        mLuckNum = luckNumber
    }

    // View 的尺寸发生变化时调用的回调函数。这使得你可以在视图大小发生变化时重新计算和设置一些关键参数
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //计算转盘的半径，取宽度 (w) 和高度 (h) 中的较小值的一半，并乘以 0.9，确保转盘在视图内有一定的边距
        mRadius = (min(w.toDouble(), h.toDouble()) / 2 * 0.9f).toFloat()
        //创建一个 RectF 对象，用于定义转盘绘制的矩形区域。这个矩形以 (0, 0) 为中心，边界为 -mRadius 到 mRadius。
        rectFPan = RectF(-mRadius, -mRadius, mRadius, mRadius)
        //创建一个用于绘制文本的矩形区域，其大小为转盘半径的 5/7，确保文本不溢出转盘边界
        //TODO 决定文字位置，当然后后面那种缩放法也有效，只是比较麻烦
        rectFStr = RectF(-mRadius / 10 * 5, -mRadius / 10 * 5, mRadius / 10 * 5, mRadius / 10 * 5)
        //计算每个项目所占的角度。360 度除以项目的数量，确保每个项目有相同的角度分布
        mItemAnge = (360f / mItemStrs.size).toFloat()
        //根据转盘半径设置文本大小，确保文本相对于转盘的大小适中。文本大小是半径的 1/9
        mTextSize = mRadius / 10f

        mPaintItemStr?.textSize = mTextSize
        //计算文本的偏移角度，将项目角度的一半赋值给 mOffsetAngle，用于文本绘制时的对齐，使文本在扇形的中间位置。
        mOffsetAngle = mItemAnge / 2f
    }

    fun startAnim() {
        objectAnimator?.cancel()


        val targetAngle = mItemAnge * mLuckNum + mStartAngle % 360f
        objectAnimator = ObjectAnimator.ofFloat(
            this,
            "rotation",
            mStartAngle,
            mStartAngle - mRepeatCount * 360f - targetAngle
        )
        objectAnimator?.setDuration(4000)
        objectAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

                luckPanAnimEndCallBack?.onAnimEnd(mItemStrs[mLuckNum])

            }
        })
        objectAnimator?.start()
        mStartAngle -= mRepeatCount * 360f + targetAngle
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mPaint == null) {
            mPaint = Paint()
            mPaint?.apply {
                strokeWidth = 5f
                color = Color.RED
                isAntiAlias = true
            }

        }
        canvas.translate((width / 2).toFloat(), (height / 2).toFloat())
        canvas.rotate(-90 - mOffsetAngle)
        drawPanItem(canvas)
        drawText(canvas)
        drawBitmap(canvas)
    }


    private fun drawPanItem(canvas: Canvas) {
        var startAng = 0f
        for (x in mItemStrs.indices) {
            //扇形的两种颜色
            mPaintArc?.let {
                it.color = if (x % 2 == 0) Color.WHITE else Color.parseColor("#F8864A")
                //addArc:传入矩形区域rectFStr画圆弧 ，起始角度 startAng 和扇形的角度 mItemAnge
                //TODO 这个矩形的区域就是整个圆盘
                canvas.drawArc(rectFPan!!, startAng, mItemAnge, true, it)
            }

            //TODO 顺道处理文字轨迹
            rectFStr?.let {
                val path = Path()
                path.addArc(rectFStr!!, startAng, mItemAnge)
                mArcPaths.add(path)
            }

            //下一个起始角度更新
            startAng += mItemAnge
        }
    }

    private fun drawText(canvas: Canvas) {
        for (x in mItemStrs.indices) {
            val path = mArcPaths[x]
            mPaintItemStr?.let {
                canvas.drawTextOnPath(mItemStrs[x], path, 0f, 0f, it)
            }
        }
    }

    private fun drawBitmap(canvas: Canvas) {
        bitmap?.let {
            // 位图的宽度和高度的一半
            val bitmapWidthHalf = it.width / 2
            val bitmapHeightHalf = it.height / 2

            // 图片应该位于每个扇形的中心，距离圆心一个固定的距离
            val imageRadius = mRadius * 0.75f // 位图距离圆心的距离，可以根据需要调整

            for (x in mItemStrs.indices) {
                // 计算每个扇形的中心角度
                val angle = mStartAngle + mItemAnge * x + mItemAnge / 2

                // 将角度转换为弧度
                val radians = Math.toRadians(angle.toDouble())

                // 计算位图的中心点坐标
                val centerX = (imageRadius * cos(radians)).toFloat()
                val centerY = (imageRadius * sin(radians)).toFloat()

                // 创建一个新的矩阵来处理位图的位置和旋转
                val matrix = Matrix()
                matrix.postTranslate(centerX - bitmapWidthHalf, centerY - bitmapHeightHalf)

                // 旋转位图使其水平居中显示
                matrix.postRotate(angle + 90, centerX, centerY) // 加90度以调整位图的初始旋转方向

                // 在计算出的位置绘制位图
                canvas.drawBitmap(it, matrix, mPaint)
            }
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
}