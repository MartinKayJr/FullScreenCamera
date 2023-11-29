package com.example.camerademo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.camerademo.permission.PermissionChecker;
import com.example.camerademo.permission.RequestCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/// 外部调用接口
public class CameraViewHelper {

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
                    if(HasCameraPermission()){
                        layout.addView(surfaceView, 0);
                        cameraHelper.OpenCamera();
                    }else{
                        RequestCameraPermission();
                    }
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
        cameraHelper.SwitchCamera();
        cameraHelper.StartPreview(surfaceView.getHolder());
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
        if(!HasStoragePermission()){
            RequestStoragePermission();
            return;
        }
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
            MediaStore.Images.Media.insertImage(activity.getContentResolver(), filePath, file.getName(),"DJLW_AR");
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
            SaveToGallery(path);

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    // region permission
    // 判断相机权限
    public static boolean HasCameraPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && activity.getApplicationInfo().targetSdkVersion>=23){
            return PermissionChecker.HasPermission(activity.getApplicationContext(),Manifest.permission.CAMERA);
        }
        return true;
    }

    // 申请相机权限
    public static void RequestCameraPermission(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SweetDialog dialog = new SweetDialog(activity);
                dialog.setTitle("想要访问你的相机")
                      .setContent("如果不允许，您将无法拍摄照片，也无法正常使用御伴功能。")
                      .setPositiveButton("确定", new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              if(PermissionChecker.NeedShowPermissionRationale(activity, Manifest.permission.CAMERA)){
                                  // 拒绝权限并且点了不允许弹出
                                  OpenAppSetting();
                              }else {
                                  ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, RequestCode.Camera);
                              }
                              dialog.dismiss();
                          }
                      })
                      .setNegativeutton("不允许", new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              dialog.dismiss();
                          }
                      }).show();
            }
        });
    }

    public static boolean HasStoragePermission(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M &&
                activity.getApplicationInfo().targetSdkVersion >= 23){
            boolean write = PermissionChecker.HasPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            boolean read = PermissionChecker.HasPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
            return read && write;
        }
        return true;
    }
    //请求存储权限
    public static void RequestStoragePermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && activity.getApplicationInfo().targetSdkVersion>=23) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                SweetDialog dialog = new SweetDialog(activity);
                dialog.setTitle("想要访问你的内部存储")
                      .setContent("如果不允许，拍摄照片将无法保存到相册，也无法正常使用御伴功能。")
                      .setPositiveButton("确定", new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              if(PermissionChecker.NeedShowPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                                      || PermissionChecker.NeedShowPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                                  // 拒绝权限并且点了不允许弹出
                                  OpenAppSetting();
                              }else {
                                  ActivityCompat.requestPermissions(activity,
                                          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                          RequestCode.Storage);
                              }
                              dialog.dismiss();
                          }
                      })
                      .setNegativeutton("不允许", new View.OnClickListener() {
                          @Override
                          public void onClick(View view) {
                              dialog.dismiss();
                          }
                      })
                      .show();
                }
            });
        }
    }
    // endregion
}