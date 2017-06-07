# QRCode Scanning Library
A qr code scanning library based on ZXing(ZXing core:3.3.0).

## ScreenShot
| Image Preview:                                                    | Gif Preview:                                                      |
| ----------------------------------------------------------------- |:-----------------------------------------------------------------:|
| <img src="screenshot/preview.png" width="270" height="480" />     | <img src="screenshot/preview_gif.gif" width="270" height="480" /> |

## How to use?
1. Add the follow dependency to your project build.gradle file:
```
compile 'com.github.songmao123:qrscanlib:1.0.0'
```

</br>

2. Add `SurfaceView` and `QRCodeScanView`to your Activity's root layout:
```xml
...
<SurfaceView
    android:id="@+id/surfaceView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />

<com.sqsong.qrcodelib.view.QRCodeScanView
    android:id="@+id/qrscan_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
...
```
The `QRCodeScanView` has some customeize attributes you can use it:
```xml
<declare-styleable name="QRCodeScanView">
    <attr name="muskColor" format="color" /> <!-- 遮罩颜色 -->
    <attr name="cornerBorderColor" format="color" /> <!-- 边角边框颜色 -->
    <attr name="centerLineColor" format="color" /> <!-- 中间扫描颜色 -->
    <attr name="resultPointColor" format="color" /> <!-- 二维码关键点颜色 -->
    <attr name="centerLineHeight" format="dimension" /> <!-- 中间扫描线高度 -->
    <attr name="borderHeight" format="dimension" /> <!-- 边角边框高度颜色 -->
</declare-styleable>
```

</br>

3. On the activity `OnCreate` method, init the `SurfaceHolder` and `QRCodeManager`.
```java
mSurfaceHolder = mSurfaceView.getHolder();
mQRCodeManager = new QRCodeManager(getApplicationContext(), this);
```

then, in the `onResume` and `onPause` method:

```java
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
```

</br>

4. Implete the interface `QRCodeDecodeCallback` method:
```java
/**
 * Set the {@link CameraManager} for the {@link QRCodeScanView}.
 * Must be set, because the {@link QRCodeScanView}'s preview frame depend on the
 * {@link CameraManager}'s params.
 * @param cameraManager
 */
@Override
public void cameraManagerInitFinish(CameraManager cameraManager) {
    if (mQRCodeScanView != null && cameraManager != null) {
        mQRCodeScanView.setCameraManager(cameraManager);
    }
}

/**
 * The ZXing library find the possible qrcode point, and {@link QRCodeScanView}
 * draw the point to the preview frame.
 * @param point possible point.
 */
@Override
public void foundPossibleResultPoint(ResultPoint point) {
    if (mQRCodeScanView != null) {
        mQRCodeScanView.addPossibleResultPoint(point);
    }
}

/**
 * Resolve the qrcode success, and return the result.
 * @param result qrcode result.
 */
@Override
public void onDecodeSuccess(Result result) {
    Intent intent = new Intent();
    intent.putExtra("scan_result", result.getText());
    setResult(RESULT_OK, intent);
    finish();
}
```
That's All.

If you have any question, you can make a issue or contact me.

## License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
