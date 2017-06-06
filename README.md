# QRCode Scanning Library
A qr code scanning library based on ZXing.

## ScreenShot
| Image Preview:                                                    | Gif Preview:                                                      |
| ----------------------------------------------------------------- |:-----------------------------------------------------------------:|
| <img src="screenshot/preview.png" width="270" height="480" />     | <img src="screenshot/preview_gif.gif" width="270" height="480" /> |

## How to use?
1. To the root build.gradle file add:
```
repositories {
    ...
    maven {
        url 'https://dl.bintray.com/sqsong/maven'
    }
    ...
}
```
</br>
2. Add the follow dependency to your project build.gradle file:
```
compile 'com.github.songmao123:qrscanlib:1.0.0'
```
</br>
3. Add `SurfaceView` and `QRCodeScanView`to your Activity's root layout:
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
```
...
</br>
4. On the activity `OnCreate` method, init the `SurfaceHolder` and `QRCodeManager`.
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
5. Implete the interface `QRCodeDecodeCallback` method:
```java
/**
* Set the {@link CameraManager} for the {@link QRCodeScanView}.
* @param cameraManager
*/
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
```
