package com.example.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Layout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/// 外部调用接口
public class CameraViewHelper {

    //private Camera camera = null;
    private static Cocos2dxActivity activity;
    private static FrameLayout layout;

    private static CameraHelper cameraHelper;
    private static CameraSurfaceView surfaceView;

    public CameraViewHelper(Cocos2dxActivity activity, FrameLayout layout){
        this.activity = activity;
        this.layout =layout;

        cameraHelper = new CameraHelper(activity);
        surfaceView = new CameraSurfaceView(activity, cameraHelper);
    }

    // 是否有相机
    public static boolean HasCamera(){
        return cameraHelper.HasCamera();
    }

    // 打开相机
    public static void OpenCamera(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(HasCamera()){
                    layout.addView(surfaceView);
                    cameraHelper.OpenCamera(surfaceView.getHolder());
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("提示");
                    builder.setMessage("该设备无相机硬件或权限被禁用，请检查权限");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            OpenAppSetting();
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();
                }
            }
        });
    }

    // 切换相机
    public static void SwitchCamera(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraHelper.SwitchCamera(surfaceView.getHolder());
            }
        });
    }

    // 销毁相机
    public static void DestoryCamera(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cameraHelper.DestoryCamera();
                layout.removeView(surfaceView);
            }
        });
    }

    // 打开APP的设置
    public static void OpenAppSetting(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package",activity.getPackageName(),null));
        activity.startActivity(intent);
    }

    // 拍照
    public static void TakePic(){
        cameraHelper.TakePicture(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                Bitmap bmp = getBmpPicData(data, camera);
                saveBitmap(bmp);
            }
        });
    }

    // 保存图片到相册
    public static boolean SaveToGallery(String filePath){
        try{
            File file=new File(filePath);
            if(!file.exists()){
                return false;
            }
            MediaStore.Images.Media.insertImage(activity.getContentResolver(), filePath, "DJLW_AR","DJLW_AR");
            Uri uri = Uri.fromFile(file);
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            return true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    // 打开相册(单纯打开)
    public static void OpenGallery(){
        Intent intent =new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activity.startActivity(intent);
    }

    // Yuv data 转bmp
    private static Bitmap getBmpPicData(byte[] data, Camera camera){
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

    // 保存图片(/Android/data/packname/caches/)目录下
    private static void saveBitmap(Bitmap bmp) {
        String path = activity.getExternalCacheDir() +"/" + System.currentTimeMillis()+".jpg";
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
            Toast.makeText(activity,"图片保存成功", Toast.LENGTH_SHORT).show();

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
