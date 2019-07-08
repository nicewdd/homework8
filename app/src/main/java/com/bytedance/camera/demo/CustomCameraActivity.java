package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.camera.demo.utils.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;
    private int cameraId = 0;
    private Timer timer;
    private String path = new String();
    private Camera.AutoFocusCallback myAutoFocusCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);
        mSurfaceView = findViewById(R.id.img);
        //todo 给SurfaceHolder添加Callback
//        mCamera = getCamera(cameraId);
        mCamera = getCamera(cameraId);
        mCamera.setDisplayOrientation(getCameraDisplayOrientation(cameraId));


        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mCamera.startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        });

        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            //todo 拍一张照片
            openFlash();
            mCamera.takePicture(null,null,mPicture);
            closeFlash();
            Toast.makeText(CustomCameraActivity.this, "已保存至手机", Toast.LENGTH_LONG).show();

        });

        findViewById(R.id.btn_record).setOnClickListener(v -> {
            //todo 录制，第一次点击是start，第二次点击是stop
            Log.d("AAAAAAAA","record");
            if (isRecording) {
                //todo 停止录制
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                mCamera.lock();
                Toast.makeText(CustomCameraActivity.this, "结束录制", Toast.LENGTH_LONG).show();
                isRecording = false;
            } else {
                //todo 录制
                Toast.makeText(CustomCameraActivity.this, "开始录制", Toast.LENGTH_LONG).show();
                mMediaRecorder = new MediaRecorder();
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);

                mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

                mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

                mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
                mMediaRecorder.setOrientationHint(rotationDegree);

                try{
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                }catch (Exception e){
                    releaseMediaRecorder();
                    e.printStackTrace();
                }
                isRecording = true;
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
            //todo 切换前后摄像头
            if(cameraId == 0){
                cameraId = 1;
                mCamera.stopPreview();//停掉原来摄像头的预览
                mCamera.release();//释放资源
                mCamera = null;//取消原来摄像头
                mCamera = getCamera(cameraId);
                mCamera.setDisplayOrientation(getCameraDisplayOrientation(cameraId));
                SurfaceHolder holder = mSurfaceView.getHolder();
                startPreview(holder);
            }
            else{
                cameraId = 0;
                mCamera.stopPreview();//停掉原来摄像头的预览
                mCamera.release();//释放资源
                mCamera = null;//取消原来摄像头
                mCamera = getCamera(cameraId);
                mCamera.setDisplayOrientation(getCameraDisplayOrientation(cameraId));
                SurfaceHolder holder = mSurfaceView.getHolder();
                startPreview(holder);
            }
        });

        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
            //todo 调焦，需要判断手机是否支持

            if(mCamera.getParameters().isZoomSupported()){
                Toast.makeText(CustomCameraActivity.this, "开始调焦", Toast.LENGTH_LONG).show();
                mCamera.autoFocus(myAutoFocusCallback);
            }
            else{
                Toast.makeText(CustomCameraActivity.this, "您的手机不支持变焦", Toast.LENGTH_LONG).show();
            }
        });
        myAutoFocusCallback = new Camera.AutoFocusCallback()
        {
            public void onAutoFocus(boolean success, Camera
                    camera) {
                if(success)//success表示对焦成功
                {
                    Log.i("Warning", "myAutoFocusCallback: success...");
                }
                else
                {
                    Log.i("warning", "myAutoFocusCallback: 失败了...");
                }
            }
        };
        timer = new Timer();
        timer.schedule(new startFocus(),0,800);
    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等

        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        mCamera.stopPreview();
        mCamera.release();
    }

    Camera.Size size;

    private void startPreview(SurfaceHolder holder) {
//        int width = mSurfaceView.getWidth();
//        int height = mSurfaceView.getHeight();
//        Camera.Parameters parameters = mCamera.getParameters();
//        Camera.Size size = getOptimalPreviewSize(parameters.getSupportedPictureSizes(), width, height);
//        parameters.setPictureSize(size.width, size.height);
        setStartPreview(mCamera,holder);
        //todo 开始预览
    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder

        return true;
    }


    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        path = pictureFile.getAbsolutePath();
        System.out.println(pictureFile);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }
        Intent intent = new Intent(CustomCameraActivity.this, EmptyActivity.class);
        intent.putExtra("path",path);
        startActivity(intent);
        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    private void setStartPreview(Camera camera,SurfaceHolder holder){
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d("error", "Error starting camera preview: " + e.getMessage());
        }
    }

    class startFocus extends TimerTask{
        public void run(){
            if(cameraId == 0){
                mCamera.autoFocus(myAutoFocusCallback);
            }
        }
    }
    private void openFlash(){
        try{
            Camera.Parameters mParameters;
            mParameters = mCamera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
           mCamera.setParameters(mParameters);
        } catch(Exception ex){
            Toast.makeText(CustomCameraActivity.this, "开启闪光灯失败", Toast.LENGTH_LONG).show();
        }
    }
    private void closeFlash(){
        try{
            Camera.Parameters mParameters;
            mParameters = mCamera.getParameters();
            mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(mParameters);
        } catch(Exception ex) {
            Toast.makeText(CustomCameraActivity.this, "关闭闪光灯失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        timer.cancel();
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        timer.cancel();
    }

}
