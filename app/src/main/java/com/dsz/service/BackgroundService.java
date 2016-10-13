package com.dsz.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dsz.activity.MainActivity;
import com.dsz.activity.R;

import java.io.IOException;

public class BackgroundService extends Service {
    public final String TAG = "BackgroundService";

    private LinearLayout mOverlay = null;   //线性布局
    private SurfaceView mSurfaceView;       //surfaceView控件

    private Camera mCamera;                 //相机
    private MjpegServer mMjpegServer;       //MjpegServer？

    private String mPort;       //端口号


    public BackgroundService() {
    }

    @Override
    public void onCreate() {

        SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                Log.v(TAG, "surfaceCreated()");

                int cameraId;           //相机的ID
                int previewWidth;       //预览宽度
                int previewHeight;      //预览高度
                int rangeMin;           //预览帧的最小值
                int rangeMax;           //预览帧的最大值
                int quality;            //图片质量
                int port;               //端口号

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BackgroundService.this);
                String cameraIdString = preferences.getString("settings_camera", null);
                String previewSizeString = preferences.getString("settings_size", null);
                String rangeString = preferences.getString("settings_range", null);
                String qualityString = preferences.getString("settings_quality", "50");
                String portString = preferences.getString("settings_port", "8080");

                // 如果失败了,那就意味着设置坏了。
                assert (cameraIdString != null && previewSizeString != null && rangeString != null);

                int xIndex = previewSizeString.indexOf("x");
                int tildeIndex = rangeString.indexOf("~");

                // 】如果失败了,那就意味着设置坏了。
                assert (xIndex > 0 && tildeIndex > 0);

                try {
                    cameraId = Integer.parseInt(cameraIdString);

                    previewWidth = Integer.parseInt(previewSizeString.substring(0, xIndex - 1));
                    previewHeight = Integer.parseInt(previewSizeString.substring(xIndex + 2));

                    rangeMin = Integer.parseInt(rangeString.substring(0, tildeIndex - 1));
                    rangeMax = Integer.parseInt(rangeString.substring(tildeIndex + 2));

                    quality = Integer.parseInt(qualityString);
                    port = Integer.parseInt(portString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Settings is broken");
                    Toast.makeText(BackgroundService.this, "Settings is broken", Toast.LENGTH_SHORT).show();

                    stopSelf();
                    return;
                }

                mCamera = Camera.open(cameraId);
                if (mCamera == null) {
                    Log.v(TAG, "Can't open camera" + cameraId);

                    Toast.makeText(BackgroundService.this, getString(R.string.can_not_open_camera),
                            Toast.LENGTH_SHORT).show();
                    stopSelf();

                    return;
                }

                try {
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    Log.v(TAG, "SurfaceHolder is not available");

                    Toast.makeText(BackgroundService.this, "SurfaceHolder is not available",
                            Toast.LENGTH_SHORT).show();
                    stopSelf();

                    return;
                }

                Parameters parameters = mCamera.getParameters();
                parameters.setPreviewSize(previewWidth, previewHeight); //设置预览照片的尺寸。
                parameters.setPreviewFpsRange(rangeMin, rangeMax);      //最小和最大帧设置预览。
                mCamera.setParameters(parameters);                      //使相机参数生效。
                mCamera.startPreview();                                 //开始预览

                JpegFactory jpegFactory = new JpegFactory(previewWidth,
                        previewHeight, quality);
                mCamera.setPreviewCallback(jpegFactory);    //使用此方法注册一个Camera. PreviewCallback，这将确保在屏幕上显示一个新的预览帧时调用onPreviewFrame方法。

                mMjpegServer = new MjpegServer(jpegFactory);
                try {
                    mMjpegServer.start(port);
                } catch (IOException e) {
                    String message = "Port: " + port + " is not available";
                    Log.v(TAG, message);

                    Toast.makeText(BackgroundService.this, message, Toast.LENGTH_SHORT).show();
                    stopSelf();
                }

                Toast.makeText(BackgroundService.this, "Port: " + port, Toast.LENGTH_SHORT).show();
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                Log.v(TAG, "surfaceChanged()");
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.v(TAG, "surfaceDestroyed()");
            }
        };


        createOverlay();
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(callback);

        mPort = PreferenceManager.getDefaultSharedPreferences(this).getString("settings_port", "8080");

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want BackgroundService.this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        // mNM.cancel(NOTIFICATION);

        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }

        destroyOverlay();

        if (mMjpegServer != null) {
            mMjpegServer.close();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showNotification() {
        // In BackgroundService.this sample, we'll use the same text for the ticker and the expanded notification
        // CharSequence text = getText(R.string.service_started);
        CharSequence text = "View webcam at " + getIpAddr() + ":" + mPort;

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.mipmap.ic_stat_webcam, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects BackgroundService.this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
//        notification.setLatestEventInfo(this, getText(R.string.app_name),
//                       text, contentIntent);

        // Send the notification.
        startForeground(R.string.service_started, notification);
    }

    /**
     * 获取ip地址
     *
     * @return
     */
    public String getIpAddr() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipString = String.format(
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));

        return ipString;
    }

    /**
     * 创建一个表面视图相机预览的表面覆盖。
     */
    private void createOverlay() {
        assert (mOverlay == null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.BOTTOM;

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mOverlay = (LinearLayout) inflater.inflate(R.layout.background, null);
        mSurfaceView = (SurfaceView) mOverlay.findViewById(R.id.backgroundSurfaceview);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(mOverlay, params);
    }

    private void destroyOverlay() {
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.removeView(mOverlay);
    }
}
