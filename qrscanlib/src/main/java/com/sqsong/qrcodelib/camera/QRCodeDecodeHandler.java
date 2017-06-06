/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sqsong.qrcodelib.camera;

import android.os.Handler;
import android.os.Message;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.sqsong.qrcodelib.util.Constant;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QRCodeDecodeHandler extends Handler implements ResultPointCallback {

    private static final String TAG = QRCodeDecodeHandler.class.getSimpleName();

    private State state;
    private final DecodeThread decodeThread;
    private final CameraManager cameraManager;
    private QRCodeDecodeCallback mDecodeCallback;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    @Override
    public void foundPossibleResultPoint(ResultPoint resultPoint) {
        if (mDecodeCallback != null) {
            mDecodeCallback.foundPossibleResultPoint(resultPoint);
        }
    }

    public QRCodeDecodeHandler(CameraManager cameraManager, QRCodeDecodeCallback l) {
        this.decodeThread = new DecodeThread(this, cameraManager, this);
        this.mDecodeCallback = l;
        decodeThread.start();
        state = State.SUCCESS;

        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case Constant.MESSAGE_WHAT_RESTART_PREVIEW:
                restartPreviewAndDecode();
                break;
            case Constant.MESSAGE_WHAT_DECODE_SUCCEEDED:
                state = State.SUCCESS;
                if (mDecodeCallback != null) {
                    mDecodeCallback.onDecodeSuccess((Result) message.obj);
                }
                break;
            case Constant.MESSAGE_WHAT_DECODE_FAILED:
                // We're decoding as fast as possible, so when one decode fails, start another.
                state = State.PREVIEW;
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), Constant.MESSAGE_WHAT_DECODE);
                break;
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), Constant.MESSAGE_WHAT_QUIT);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(Constant.MESSAGE_WHAT_DECODE_SUCCEEDED);
        removeMessages(Constant.MESSAGE_WHAT_DECODE_FAILED);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), Constant.MESSAGE_WHAT_DECODE);
        }
    }

}
