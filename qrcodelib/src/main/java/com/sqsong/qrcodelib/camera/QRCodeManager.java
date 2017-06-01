package com.sqsong.qrcodelib.camera;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;

import com.sqsong.qrcodelib.util.Constant;

import java.io.IOException;

/**
 * Created by 青松 on 2017/6/1.
 */

public class QRCodeManager implements SurfaceHolder.Callback {

    private static final String TAG = QRCodeManager.class.getSimpleName();

    private Context mContext;
    // surface create flag.
    private boolean hasSurface;
    private CameraManager mCameraManager;
    private QRCodeDecodeCallback mCallback;
    private QRCodeDecodeHandler mDecodeHandler;

    public QRCodeManager(Context context, QRCodeDecodeCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    /**
     * init some params
     * @param surfaceHolder {@link android.view.SurfaceView}'s holder.
     */
    public void onResume(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalArgumentException("The surface holder must not be null.");
        }
        mCameraManager = new CameraManager(mContext);
        if (mCallback != null) {
            mCallback.cameraManagerInitFinish(mCameraManager);
        }
        mDecodeHandler = null;
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }
    }

    /**
     * destroy some config.
     * @param surfaceHolder {@link android.view.SurfaceView}'s holder.
     */
    public void onPause(SurfaceHolder surfaceHolder) {
        if (mDecodeHandler != null) {
            mDecodeHandler.quitSynchronously();
            mDecodeHandler = null;
        }
        mCameraManager.closeDriver();
        if (!hasSurface) {
            surfaceHolder.removeCallback(this);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalArgumentException("The surface holder must not be null.");
        }

        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            // Creating the mDecodeHandler starts the preview, which can also throw a RuntimeException.
            if (mDecodeHandler == null) {
                mDecodeHandler = new QRCodeDecodeHandler(mCameraManager, mCallback);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) return;

        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    /**
     * restart scanning mode.
     */
    public void startScanning() {
        if (mDecodeHandler != null) {
            mDecodeHandler.sendEmptyMessageDelayed(Constant.MESSAGE_WHAT_RESTART_PREVIEW, 0);
        }
    }

    /**
     * open the flash light.
     * @param open true: open  false: close.
     */
    public void openFlash(boolean open) {
        if (mCameraManager != null) {
            mCameraManager.setTorch(open);
        }
    }
}
