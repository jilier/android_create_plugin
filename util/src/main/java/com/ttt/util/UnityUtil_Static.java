package com.ttt.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.Toast;

public class UnityUtil_Static {
    @SuppressLint("StaticFieldLeak")
    static Activity _unityActivity;

    static Activity getActivity(){
        if (_unityActivity == null){
            try {
                Class<?> classType = Class.forName("com.unity3d.player.UnityPlayer");
                _unityActivity  = (Activity) classType.getDeclaredField("currentActivity").get(classType);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        return _unityActivity;
    }

    public static void showToast(String info){
        Toast.makeText(_unityActivity, info, Toast.LENGTH_SHORT).show();
    }
}