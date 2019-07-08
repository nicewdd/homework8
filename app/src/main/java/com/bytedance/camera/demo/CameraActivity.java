package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.camera.demo.utils.Utils.getOutputMediaFile;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private SurfaceView sv;
    private Camera mCamera;
    private SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        sv = (SurfaceView)findViewById(R.id.sv);
        holder = sv.getHolder();
        holder.addCallback(this);

        findViewById(R.id.btn_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取当前相机参数
                android.hardware.Camera.Parameters parameters = mCamera.getParameters();
                // 设置相片格式
                parameters.setPictureFormat(ImageFormat.JPEG);
                // 设置预览大小
                parameters.setPreviewSize(800, 480);
                // 设置对焦方式，这里设置自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera.autoFocus(new Camera.AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        // 判断是否对焦成功
                        if (success) {
                            // 拍照 第三个参数为拍照回调
                            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    // data为完整数据
                                    File file = new File("/sdcard/photo.png");
                                    // 使用流进行读写
                                    try {
                                        FileOutputStream fos = new FileOutputStream(file);
                                        try {
                                            fos.write(data);
                                            // 关闭流
                                            fos.close();
                                            // 查看图片
                                            Intent intent = new Intent();
                                            // 传递路径
                                            intent.putExtra("path", file.getAbsolutePath());
                                            setResult(0,intent);
                                            finish();
                                        } catch (IOException e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    } catch (FileNotFoundException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera == null) {
            mCamera = getCamera();
            if (holder != null) {
                showCameraView(mCamera, holder);
            }
        }
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        // activity暂停时我们释放相机内存
        clearCamera();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 释放相机的内存
     */
    private void clearCamera() {

        // 释放hold资源
        if (mCamera != null) {
            // 停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            // 释放相机资源
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera getCamera() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            camera = null;
        }
        return camera;
    }

    private void showCameraView(Camera camera,SurfaceHolder holder)
    {
        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        }catch (Exception e){
            e.printStackTrace();
        };

    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        showCameraView(mCamera, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        showCameraView(mCamera, holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        clearCamera();
    }
}
