package com.example.demo;

import android.hardware.Camera;
import android.widget.FrameLayout;

public class CameraViewHelper {

    private Camera camera = null;
    private static Cocos2dxActivity activity;
    private static FrameLayout layout;

    public CameraViewHelper(Cocos2dxActivity activity, FrameLayout layout){
        this.activity = activity;
        this.layout =layout;
    }

    public static void CreateCameraView(){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}
