package com.sqsong.qrcodelib.camera;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

/**
 * Created by 青松 on 2017/5/27.
 */

public interface QRCodeDecodeCallback {

    void cameraManagerInitFinish(CameraManager cameraManager);

    void foundPossibleResultPoint(ResultPoint point);

    void onDecodeSuccess(Result result);

}
