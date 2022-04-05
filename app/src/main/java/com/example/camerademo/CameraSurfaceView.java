package com.example.camerademo;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private CameraHelper cameraHelper;
    private SurfaceHolder surfaceHolder;

    public CameraSurfaceView(Activity activity, CameraHelper cameraHelper) {
        super(activity);
        this.cameraHelper = cameraHelper;
        this.surfaceHolder = this.getHolder();
        surfaceHolder.addCallback(this);
    }

    // region SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(cameraHelper != null){
            cameraHelper.StartPreview(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(holder.getSurface() == null)
            return;

        if(cameraHelper!=null){
            cameraHelper.StopPreview();
            cameraHelper.StartPreview(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(cameraHelper!=null)
            cameraHelper.DestoryCamera();
    }
    // end region
}
