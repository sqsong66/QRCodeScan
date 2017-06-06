package com.sqsong.qrcodelib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.sqsong.qrcodelib.R;
import com.sqsong.qrcodelib.camera.CameraManager;
import com.sqsong.qrcodelib.util.DensityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sqsong on 17-5-24.
 */

public class QRCodeScanView extends View {

    private static final int POINT_SIZE = 6;
    private static final int MAX_RESULT_POINTS = 20;
    private static final long ANIMATION_DELAY = 50L;
    private static final int CORNER_BORDER_WIDTH = 8;
    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int CENTER_LINE_MOVE_DISTANCE = 8;
    private static final int HALF_CORNER_BORDER_WIDTH = CORNER_BORDER_WIDTH / 2;

    private Path mPath;
    private Paint mPaint;
    private int mMuskColor;
    private Rect mScanRect;
    private int mBorderColor;
    private int mCornerHeight;
    private Paint mBorderPaint;
    private int mCenterLineTop;
    private int mCenterLineColor;
    private int mResultPointColor;
    private Rect mPreviewScanRect;
    private int mCenterLineHeight;
    private CameraManager mCameraManager;

    private List<ResultPoint> possibleResultPoints;
    private List<ResultPoint> lastPossibleResultPoints;

    public QRCodeScanView(Context context) {
        this(context, null);
    }

    public QRCodeScanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QRCodeScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initParams(context, attrs);
        init();
    }

    private void initParams(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.QRCodeScanView);
        mMuskColor = ta.getColor(R.styleable.QRCodeScanView_muskColor, getResources().getColor(R.color.colorMusk));
        mBorderColor = ta.getColor(R.styleable.QRCodeScanView_cornerBorderColor, getResources().getColor(R.color.colorAccent));
        mCenterLineColor = ta.getColor(R.styleable.QRCodeScanView_centerLineColor, getResources().getColor(R.color.colorAccent));
        mResultPointColor = ta.getColor(R.styleable.QRCodeScanView_resultPointColor, getResources().getColor(R.color.colorPossiblePoints));
        mCornerHeight = (int) ta.getDimension(R.styleable.QRCodeScanView_borderHeight, 0);
        mCenterLineHeight = (int) ta.getDimension(R.styleable.QRCodeScanView_centerLineHeight, DensityUtil.dip2px(1));
        ta.recycle();
    }

    private void init() {
        possibleResultPoints = new ArrayList<>(5);
        lastPossibleResultPoints = null;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mPath = new Path();
        mScanRect = new Rect();
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.mCameraManager = cameraManager;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mCameraManager == null) {
            return;
        }

        mScanRect = mCameraManager.getFramingRect();
        mPreviewScanRect = mCameraManager.getFramingRectInPreview();
        if (mScanRect == null || mPreviewScanRect == null) {
            return;
        }

        if (mCornerHeight == 0) {
            mCornerHeight = (int) ((mScanRect.right - mScanRect.left) * 1.0f / 10);
        }
        if (mCenterLineTop == 0) {
            mCenterLineTop = mScanRect.top + HALF_CORNER_BORDER_WIDTH;
        }

        drawMusk(canvas);
        drawFourCorner(canvas);
        drawCenterLine(canvas);
        drawPossiblePoints(canvas);
    }

    /**
     * draw the possible qrcode points.
     * @param canvas
     */
    private void drawPossiblePoints(Canvas canvas) {
        float scaleX = mScanRect.width() / (float) mPreviewScanRect.width();
        float scaleY = mScanRect.height() / (float) mPreviewScanRect.height();

        List<ResultPoint> currentPossible = possibleResultPoints;
        List<ResultPoint> currentLast = lastPossibleResultPoints;
        int frameLeft = mScanRect.left;
        int frameTop = mScanRect.top;

        if (currentPossible.isEmpty()) {
            lastPossibleResultPoints = null;
        } else {
            possibleResultPoints = new ArrayList<>(5);
            lastPossibleResultPoints = currentPossible;
            mPaint.setAlpha(CURRENT_POINT_OPACITY);
            mPaint.setColor(mResultPointColor);
            synchronized (currentPossible) {
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY), POINT_SIZE, mPaint);
                }
            }
        }
        if (currentLast != null) {
            mPaint.setAlpha(CURRENT_POINT_OPACITY / 2);
            mPaint.setColor(mResultPointColor);
            synchronized (currentLast) {
                float radius = POINT_SIZE / 2.0f;
                for (ResultPoint point : currentLast) {
                    canvas.drawCircle(frameLeft + (int) (point.getX() * scaleX),
                            frameTop + (int) (point.getY() * scaleY), radius, mPaint);
                }
            }
        }

        // 定时绘制中间的扫描框部分
        postInvalidateDelayed(ANIMATION_DELAY, mScanRect.left, mScanRect.top,
                mScanRect.right, mScanRect.bottom);
    }

    /**
     * draw the center scanning line.
     * @param canvas
     */
    private void drawCenterLine(Canvas canvas) {
        mCenterLineTop += CENTER_LINE_MOVE_DISTANCE;
        if (mCenterLineTop > mScanRect.bottom - HALF_CORNER_BORDER_WIDTH) {
            mCenterLineTop = mScanRect.top + HALF_CORNER_BORDER_WIDTH;
        }
        mPaint.setColor(mCenterLineColor);
        canvas.drawRect(mScanRect.left + HALF_CORNER_BORDER_WIDTH, mCenterLineTop,
                mScanRect.right - HALF_CORNER_BORDER_WIDTH, mCenterLineTop + mCenterLineHeight,
                mPaint);
    }

    /**
     * draw the four corner border.
     * @param canvas
     */
    private void drawFourCorner(Canvas canvas) {
        mPath.reset();

        mPath.moveTo(mScanRect.left - HALF_CORNER_BORDER_WIDTH, mScanRect.top + mCornerHeight);
        mPath.lineTo(mScanRect.left - HALF_CORNER_BORDER_WIDTH, mScanRect.top - HALF_CORNER_BORDER_WIDTH);
        mPath.lineTo(mScanRect.left + mCornerHeight, mScanRect.top - HALF_CORNER_BORDER_WIDTH);

        mPath.moveTo(mScanRect.right - mCornerHeight, mScanRect.top - HALF_CORNER_BORDER_WIDTH);
        mPath.lineTo(mScanRect.right + HALF_CORNER_BORDER_WIDTH, mScanRect.top - HALF_CORNER_BORDER_WIDTH);
        mPath.lineTo(mScanRect.right + HALF_CORNER_BORDER_WIDTH, mScanRect.top + mCornerHeight);

        mPath.moveTo(mScanRect.right + HALF_CORNER_BORDER_WIDTH, mScanRect.bottom - mCornerHeight);
        mPath.lineTo(mScanRect.right + HALF_CORNER_BORDER_WIDTH, mScanRect.bottom + HALF_CORNER_BORDER_WIDTH);
        mPath.lineTo(mScanRect.right - mCornerHeight, mScanRect.bottom + HALF_CORNER_BORDER_WIDTH);

        mPath.moveTo(mScanRect.left + mCornerHeight, mScanRect.bottom + HALF_CORNER_BORDER_WIDTH);
        mPath.lineTo(mScanRect.left - HALF_CORNER_BORDER_WIDTH, mScanRect.bottom + HALF_CORNER_BORDER_WIDTH);
        mPath.lineTo(mScanRect.left - HALF_CORNER_BORDER_WIDTH, mScanRect.bottom - mCornerHeight);

        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(CORNER_BORDER_WIDTH);
        canvas.drawPath(mPath, mBorderPaint);
    }

    /**
     * draw the musk.
     * @param canvas
     */
    private void drawMusk(Canvas canvas) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        mPaint.setColor(mMuskColor);
        canvas.drawRect(0, 0, width, mScanRect.top, mPaint);
        canvas.drawRect(0, mScanRect.top, mScanRect.left, mScanRect.bottom, mPaint);
        canvas.drawRect(mScanRect.right, mScanRect.top, width, mScanRect.bottom, mPaint);
        canvas.drawRect(0, mScanRect.bottom, width, height, mPaint);
    }

    /**
     * add the possible qrcode point.
     * @param point qrcode point.
     */
    public void addPossibleResultPoint(ResultPoint point) {
        List<ResultPoint> points = possibleResultPoints;
        synchronized (points) {
            points.add(point);
            int size = points.size();
            if (size > MAX_RESULT_POINTS) {
                // trim it
                points.subList(0, size - MAX_RESULT_POINTS / 2).clear();
            }
        }
    }
}
