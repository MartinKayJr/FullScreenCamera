package com.example.camerademo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class Cocos2dxActivity extends Activity {
    CameraViewHelper cameraViewHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cocos2dx);

        FrameLayout layout = (FrameLayout)findViewById(R.id.frame_layout);
        Button OpenCamera = (Button) findViewById(R.id.OpenCamera);
        Button Snap = (Button)findViewById(R.id.Snap);
        Button CloseCamera = (Button)findViewById(R.id.CloseCamera);
        Button SwitchCamera = (Button) findViewById(R.id.switchCamera);

        cameraViewHelper = new CameraViewHelper(this, layout);

        OpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraViewHelper.OpenCamera();
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

        SwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraViewHelper.SwitchCamera();
            }
        });
    }
}