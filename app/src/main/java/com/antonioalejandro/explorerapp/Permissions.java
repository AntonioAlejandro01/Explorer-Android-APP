package com.antonioalejandro.explorerapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class Permissions {

    public final static  int REQUEST_CODE_PERMISSIONS = 225;

    private final static String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    public static void askPermission(Context context, Activity activity) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            ArrayList<String> permisos = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if(context.checkSelfPermission(permissions[i])!=PackageManager.PERMISSION_GRANTED){
                    permisos.add(permissions[i]);
                }
            }


            for (int i = 0; i < permisos.size(); i++) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,permisos.get(i))){
                    permisos.remove(i);
                }
            }
            if (permisos.size() > 0) {
                permisos.trimToSize();
                String[] tmp = new String[permisos.size()];
                for (int i = 0; i < permisos.size(); i++) {
                    tmp[i] = permisos.get(i);
                }
                ActivityCompat.requestPermissions(activity, tmp , REQUEST_CODE_PERMISSIONS);
            }

        }
    }

}
