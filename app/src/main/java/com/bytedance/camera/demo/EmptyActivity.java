package com.bytedance.camera.demo;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EmptyActivity extends AppCompatActivity {

    private final int IMAGE_CODE = 0;
    private String path = new String();
    private ImageView imageView;
    private File imgFile;
    private Button btn1,btn2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        btn1 = findViewById(R.id.button2);
        btn2 = findViewById(R.id.button3);
        imageView = findViewById(R.id.imageView2);
        Bundle extras = getIntent().getExtras();
        Log.d("AAAAAAAAAAA:","3");
        path = extras.getString("path");
        imgFile = new File(path);
        Log.d("AAAAAAAAAAA:",path);
        Log.d("AAAAAAAAAAA:","2");
        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        getAlbum.setType("image/*");
        System.out.println(path);
        startActivityForResult(getAlbum, IMAGE_CODE);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(EmptyActivity.this, CustomCameraActivity.class));
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgFile.delete();
                startActivity(new Intent(EmptyActivity.this, CustomCameraActivity.class));
            }
        });
    }
    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bm = null;
        ContentResolver resolver = getContentResolver();
        if (requestCode == IMAGE_CODE) {

            try {
                FileOutputStream outputStream = null;
                File imageFile = new File(path);
                Uri originalUri = Uri.fromFile(imageFile);
                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);
               imageView.setImageBitmap(bm);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
