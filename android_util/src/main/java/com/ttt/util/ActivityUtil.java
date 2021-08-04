package com.ttt.util;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class ActivityUtil {
    Activity _unityActivity;

    Activity getActivity(){
        if (_unityActivity == null){
            try {
                Class<?> classType = Class.forName("com.unity3d.player.UnityPlayer");
                Activity activity = (Activity) classType.getDeclaredField("currentActivity").get(classType);
                _unityActivity = activity;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return _unityActivity;
    }

    public void showToast(String info){
        Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT).show();
    }
}
