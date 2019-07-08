package com.bytedance.camera.demo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.CamcorderProfile;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class Main2Activity extends AppCompatActivity {

    private ImageView cameraIv;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 0 && requestCode == 100)
        {
            String path = data.getStringExtra("path");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            ImageSize imageSize = getImageViewSize(cameraIv);
            options.inSampleSize = caculateInSampleSize(options,
                    imageSize.width, imageSize.height);

            // 使用获得到的InSampleSize再次解析图片
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            cameraIv.setImageBitmap(bitmap);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Button openButton = (Button) findViewById(R.id.button);
        cameraIv = (ImageView) findViewById(R.id.imageView);



        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main2Activity.this, CameraActivity.class);
                startActivityForResult(intent,100);
            }
        });
    }

    /* 根据需求的宽和高以及图片实际的宽和高计算SampleSize
     *
     * @param options
     * @param width
     * @param height
     * @return
     */
    public static int caculateInSampleSize(BitmapFactory.Options options, int reqWidth,
                                           int reqHeight)
    {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;

        if (width > reqWidth || height > reqHeight)
        {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio);
        }

        return inSampleSize;
    }

    public static ImageSize getImageViewSize(ImageView imageView)
    {

        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources()
                .getDisplayMetrics();

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)imageView.getLayoutParams();

        int width = imageView.getWidth();// 获取imageview的实际宽度
        if (width <= 0)
        {
            width = lp.width;// 获取imageview在layout中声明的宽度
        }

        if (width <= 0)
        {
            width = displayMetrics.widthPixels;
        }

        int height = imageView.getHeight();// 获取imageview的实际高度
        if (height <= 0)
        {
            height = lp.height;// 获取imageview在layout中声明的宽度
        }

        if (height <= 0)
        {
            height = displayMetrics.heightPixels;
        }
        imageSize.width = width;
        imageSize.height = height;

        return imageSize;
    }

    public static class ImageSize
    {
        int width;
        int height;
    }
}
