package com.example.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;

public class Cocos2dxActivity extends Activity {
    private CameraSurfaceView cameraSurfaceView;
    private CameraHelper cameraHelper=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout layout = (FrameLayout)findViewById(R.id.frame_layout);
        Button OpenCamera = (Button) findViewById(R.id.OpenCamera);
        Button Snap = (Button)findViewById(R.id.Snap);
        Button CloseCamera = (Button)findViewById(R.id.CloseCamera);

        cameraHelper = new CameraHelper(this);

        OpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 有相机
                if(cameraHelper.HasCamera()){
                    if (cameraSurfaceView==null){
                        cameraSurfaceView = new CameraSurfaceView(Cocos2dxActivity.this, cameraHelper);
                    }
                    OpenCamera.setEnabled(false);
                    layout.addView(cameraSurfaceView, 0);
                    cameraHelper.OpenCamera(cameraSurfaceView.getHolder());
                }else {
                    ShowNoCameraDialog();
                }
            }
        });

        Snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper.TakePicture(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        Bitmap bmp = getBmpPicData(data, camera);
                        saveBitmap(bmp);
                    }
                });
            }
        });

        CloseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraHelper.DestoryCamera();
                layout.removeView(cameraSurfaceView);
                OpenCamera.setEnabled(true);
            }
        });
    }

    // Yuv data 转bmp
    private Bitmap getBmpPicData(byte[] data, Camera camera){
        Camera.Size size = camera.getParameters().getPreviewSize();
        YuvImage image = new YuvImage(data, ImageFormat.NV21,size.width, size.height,null);
        if(image != null){
            Bitmap bmp = null;
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compressToJpeg(new Rect(0,0, size.width, size.height),80, stream);
                bmp = BitmapFactory.decodeByteArray(stream.toByteArray(),0, stream.size());
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bmp;
        }
        return null;
    }

    // 保存图片
    public void saveBitmap(Bitmap bmp) {
        String path = Environment.getExternalStorageDirectory() +"/" + System.currentTimeMillis()+".jpg";
        try{
            File img = new File(path);
            if(!img.exists()){
                img.getParentFile().mkdir();
                img.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(img);
            // 图片压缩
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, fs);
            fs.flush();
            fs.close();
            Toast.makeText(this,"图片保存成功",Toast.LENGTH_SHORT).show();

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }


    // region util
    private void ShowNoCameraDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("该设备无相机硬件或权限被禁用，是否检查权限");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package",getPackageName(),null));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    // endregion util
}
