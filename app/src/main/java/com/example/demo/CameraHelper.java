package com.example.demo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

public class CameraHelper {
    private static String TAG = "CameraHelper";

    private Activity activity;
    private Camera mCamera;
    private Camera.Parameters parameters;

    // 相机ID(默认后置相机)
    private int cameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

    // 旋转监听
    private OrientationEventListener orientationEventListener;

    public CameraHelper(Activity activity){
        this.activity = activity;

        orientationEventListener = new OrientationEventListener(activity) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.d(TAG, "onOrientationChanged: " + orientation);
                setPictureRotate(orientation);
            }
        };
    }

    // 0 后摄  1 前摄
    public boolean HasCamera(){
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    // 打开相机
    public void OpenCamera(){
        try{
            Log.d(TAG,"打开相机: " + cameraID);
            mCamera = GetCamera();
            Camera.getCameraInfo(cameraID, cameraInfo);
            initCamera();
            setDispalyRotation();
            orientationEventListener.enable();
        }
        catch (Exception ex){
            //ex.printStackTrace();
        }
    }

    // 获取相机
    public Camera GetCamera(){
        Camera camera;
       try{
            camera = Camera.open(cameraID);
       }catch (Exception ex){
           camera = null;
           ex.printStackTrace();
       }
       return camera;
    }

    // 切换前后相机
    public void SwitchCamera(){
        DestoryCamera();
        cameraID = cameraID == 0 ? 1 : 0;
        OpenCamera();
    }

    // 销毁相机
    public void DestoryCamera(){
        if(mCamera != null){
            Log.d(TAG,"销毁相机");
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
        orientationEventListener.disable();
    }

    // 开始预览
    public void StartPreview(SurfaceHolder holder){
        if(mCamera != null){
            try{
                Log.d(TAG, "开始预览");
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }catch (IOException ex){
                ex.printStackTrace();
            }
            Log.d(TAG, parameters.getPictureSize().width+" <-:-> "+parameters.getPictureSize().height);
        }
    }

    // 预览手动放大缩小
    public void HandleZoom(boolean zoomIn){
        if(parameters.isZoomSupported()){
            int maxZoom = parameters.getMaxZoom();
            int curZoom = parameters.getZoom();

            // 放大
            if(zoomIn){
                if(curZoom < maxZoom) ++ curZoom;
            }else {
                // 缩小
                if(curZoom > 0) --curZoom;
            }
            parameters.setZoom(curZoom);
            mCamera.setParameters(parameters);
        }
    }

    // 结束预览
    public void StopPreview(){
        if(mCamera != null){
            Log.d(TAG, "StopPreview");
            mCamera.stopPreview();
        }
    }

    // 是否是前置相机
    public boolean IsFrontCamera(){
        return cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    // 拍照
    public void TakePicture(Camera.PreviewCallback callback){
       //  mCamera.takePicture(null,null, callback);
        if(mCamera!=null){
            mCamera.setOneShotPreviewCallback(callback);
        }
    }




    // 初始化相机
    private void initCamera(){
        try {
            parameters = mCamera.getParameters();

            // 设置自动聚焦
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            if(parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            //
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setPreviewFormat(ImageFormat.NV21);
            parameters.setExposureCompensation(0); // 设置曝光强度

            // 拿到屏幕尺寸
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            // 设置预览大小
            Point  previousSize = findBestPreviewSizeValue(parameters.getSupportedPreviewSizes(), metrics.widthPixels, metrics.heightPixels);
            parameters.setPreviewSize(previousSize.y, previousSize.x);


            mCamera.setParameters(parameters);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    // 设置屏幕旋转
    // 横屏就设置0
    private void setDispalyRotation() {
        int roation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (roation) {
            case Surface.ROTATION_0:
                roation = 0;
            case Surface.ROTATION_90:
                roation = 90;
            case Surface.ROTATION_180:
                roation = 180;
            case Surface.ROTATION_270:
                roation = 270;
            default:
                roation = 0;
        }
        if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
            roation = (cameraInfo.orientation + roation) % 360;
            roation = (360 - roation) % 360;
        }else {
            roation = (cameraInfo.orientation - roation + 360) % 360;
        }
        mCamera.setDisplayOrientation(0);
    }
    // 设置图片旋转
    private void setPictureRotate(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return;
        orientation = (orientation + 45) / 90 * 90;
        int rotation = 0;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (cameraInfo.orientation - orientation + 360) % 360;
        } else {
            rotation = (cameraInfo.orientation + orientation) % 360;
        }
        Log.d(TAG, "setPictureRotate: " + rotation);
    }

    // 找到一个最合适的预览大小
    private static Point findBestPreviewSizeValue(List<Camera.Size> sizes, int screenWidth, int screenHeight) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (Camera.Size size : sizes) {
            int dimPosition = size.width;
            if (dimPosition < 0) {
                continue;
            }

            int newDiff = Math.abs(size.width - screenWidth) + Math.abs(size.height - screenHeight);
            if (newDiff == 0) {
                bestX = size.width;
                bestY = size.height;
                break;
            } else if (newDiff < diff) {
                bestX = size.width;
                bestY = size.height;
                diff = newDiff;
            }
        }
        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }
}
