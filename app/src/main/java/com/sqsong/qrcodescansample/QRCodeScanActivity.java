package com.sqsong.qrcodescansample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.sqsong.qrcodelib.camera.CameraManager;
import com.sqsong.qrcodelib.camera.QRCodeDecodeCallback;
import com.sqsong.qrcodelib.camera.QRCodeManager;
import com.sqsong.qrcodelib.util.DensityUtil;
import com.sqsong.qrcodelib.view.QRCodeScanView;

public class QRCodeScanActivity extends AppCompatActivity implements QRCodeDecodeCallback {

    private SurfaceView mSurfaceView;
    private QRCodeScanView mQRCodeScanView;
    private SurfaceHolder mSurfaceHolder;
    private QRCodeManager mQRCodeManager;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scan);

        initView();
        initEvent();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mQRCodeScanView = (QRCodeScanView) findViewById(R.id.qrscan_view);
    }

    private void initEvent() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mToolbar.getLayoutParams();
        layoutParams.topMargin = DensityUtil.getStatusBarHeight(this);

        mSurfaceHolder = mSurfaceView.getHolder();
        mQRCodeManager = new QRCodeManager(getApplicationContext(), this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mQRCodeManager.onResume(mSurfaceHolder);
    }

    @Override
    protected void onPause() {
        mQRCodeManager.onPause(mSurfaceHolder);
        super.onPause();
    }

    @Override
    public void cameraManagerInitFinish(CameraManager cameraManager) {
        if (mQRCodeScanView != null && cameraManager != null) {
            mQRCodeScanView.setCameraManager(cameraManager);
        }
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint point) {
        if (mQRCodeScanView != null) {
            mQRCodeScanView.addPossibleResultPoint(point);
        }
    }

    @Override
    public void onDecodeSuccess(Result result) {
        Intent intent = new Intent();
        intent.putExtra("scan_result", result.getText());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("sqsong", "Activity onDestroy.");
    }
}
