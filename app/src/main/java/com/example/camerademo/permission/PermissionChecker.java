package com.example.camerademo.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionChecker {

    // 检查存储权限
    public static boolean HasStoragePermission(Context context){
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M && targetSdkVersion>=23){
            boolean write = HasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            boolean read = HasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read && write;
        }
        return true;
    }

    /**
     * 是否有安装权限
     */
    public static boolean HasInstallPermission(Context context){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    /**
     * 悬浮窗权限
     */
    public static boolean HasWindowPermission(Context context){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    public static boolean HasPermission(Context context, String permission){
        return ActivityCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static List<String> CheckPermissions(Context context, String[] permissions){
        List<String> deniedList=new ArrayList<>();
        for (String permission : permissions){
            if(ActivityCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_DENIED){
                deniedList.add(permission);
            }
        }
        return deniedList;
    }

    // 权限被拒绝且不允许弹窗时
    public static boolean NeedShowPermissionRationale(Activity activity, String permission){
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
}
