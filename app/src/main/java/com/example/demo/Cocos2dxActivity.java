package com.example.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
    CameraViewHelper cameraViewHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout layout = (FrameLayout)findViewById(R.id.frame_layout);
        Button OpenCamera = (Button) findViewById(R.id.OpenCamera);
        Button Snap = (Button)findViewById(R.id.Snap);
        Button CloseCamera = (Button)findViewById(R.id.CloseCamera);

        cameraViewHelper = new CameraViewHelper(this, layout);

        OpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(CameraViewHelper.HasCamera()){
                CameraViewHelper.OpenCamera();
                }
                else {
                    ShowNoCameraDialog();
                }
            }
        });

        Snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraViewHelper.TakePic();
            }
        });

        CloseCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraViewHelper.DestoryCamera();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryReceiver);
    }

    // region util
    private void ShowNoCameraDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("该设备无相机硬件或权限被禁用，是否检查权限");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CameraViewHelper.OpenAppSetting();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }
    // endregion util

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 100);
            int status = intent.getIntExtra("status", 0);
            //batteryInfo.setText(level * 100/scale+"% :"+status);
        }
    };
}
