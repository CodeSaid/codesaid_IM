package com.codesaid.lib_framework.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.codesaid.lib_framework.R;

/**
 * Created By codesaid
 * On :2019-12-28
 * Package Name: com.codesaid.lib_framework.view
 * desc : 图片拖动验证码 View
 */
public class TouchPictureView extends View {

    // 背景
    private Bitmap bgBitmap;

    // 背景画笔
    private Paint mbgPaint;

    // 空白块
    private Bitmap mNullBitmap;

    // 空白块画笔
    private Paint mNullPaint;

    // 移动方块
    private Bitmap mMoveBitmap;

    // 移动方块画笔
    private Paint mMovePaint;

    // View 的宽高
    private int mHeight;
    private int mWidth;

    // 方块的大小
    private int CARD_SIZE = 200;

    // 方块的坐标
    private int LINE_W, LINE_H = 0;

    // 移动方块横坐标
    private int moveX = 200;

    // 误差值
    private int errorValues = 10;

    private onViewResultListener mOnViewResultListener;

    public void setOnViewResultListener(onViewResultListener onViewResultListener) {
        mOnViewResultListener = onViewResultListener;
    }

    public TouchPictureView(Context context) {
        super(context);
        init();
    }

    public TouchPictureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mbgPaint = new Paint();
        mNullPaint = new Paint();
        mMovePaint = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBg(canvas);
        drawNullCard(canvas);
        drawMoveCard(canvas);
    }

    /**
     * 绘制背景
     *
     * @param canvas canvas
     */
    private void drawBg(Canvas canvas) {
        // 获取图片
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_bg);
        // 创建一个空的 Bitmap
        bgBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        // 将图片绘制到 空的 Bitmap 上
        Canvas bgCanvas = new Canvas(bgBitmap);
        bgCanvas.drawBitmap(bitmap, null, new Rect(0, 0, mWidth, mHeight), mbgPaint);
        // 将 bgBitmap 绘制到 View 上
        canvas.drawBitmap(bgBitmap, null, new Rect(0, 0, mWidth, mHeight), mbgPaint);
    }

    /**
     * 绘制空白块
     */
    private void drawNullCard(Canvas canvas) {
        // 获取图片
        mNullBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_null_card);
        // 计算值
        CARD_SIZE = mNullBitmap.getWidth();

        LINE_W = mWidth / 3 * 2;
        LINE_H = mHeight / 2 - (CARD_SIZE / 2);

        // 绘制
        canvas.drawBitmap(mNullBitmap, LINE_W, LINE_H, mNullPaint);
    }

    /**
     * 绘制移动方块
     */
    private void drawMoveCard(Canvas canvas) {
        // 截取空白块位置坐标的 bitmap 图像
        mMoveBitmap = Bitmap.createBitmap(bgBitmap, LINE_W, LINE_H, CARD_SIZE, CARD_SIZE);
        // 绘制在 View 上,不能直接使用 LINE_W 和 LINE_H ，那样会和空白块重叠
        canvas.drawBitmap(mMoveBitmap, moveX, LINE_H, mMovePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 判断点击的位置是否是 方块的 内部 坐标, 如果是，才能拖动方块

                break;
            case MotionEvent.ACTION_MOVE:
                // 防止越界
                if (event.getX() > 0 && event.getX() < (mWidth - CARD_SIZE)) {
                    moveX = (int) event.getX();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                // 验证
                if (moveX > (LINE_W - errorValues) && moveX < (LINE_W + errorValues)) {
                    if (mOnViewResultListener != null) {
                        mOnViewResultListener.onResult();
                        // 重置
                        moveX = 200;
                        invalidate();
                    }
                } else {
                    // 重置
                    moveX = 200;
                    invalidate();
                }
                break;
        }

        return true;
    }

    public interface onViewResultListener {
        void onResult();
    }
}
