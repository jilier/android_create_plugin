package com.ttt.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import static android.content.Context.WIFI_SERVICE;

public class UnityUtil /*extends Activity*/{

    private final int REQUEST_LOCATION = 0x0;
    /**
     * unity项目启动时的的上下文
     */
    private Activity _unityActivity;
    /**
     * 获取unity项目的上下文
     * @return
     */
    Activity getActivity(){
        if(null == _unityActivity) {
            try {
                Class<?> classtype = Class.forName("com.unity3d.player.UnityPlayer");
                _unityActivity = (Activity) classtype.getDeclaredField("currentActivity").get(classtype);
            } catch (ClassNotFoundException e) {

            } catch (IllegalAccessException e) {

            } catch (NoSuchFieldException e) {

            }
        }
        return _unityActivity;
    }

    /**
     * Toast显示unity发送过来的内容
     * @param info           消息的内容
     */
    public void showToast(String info){
        Toast.makeText(getActivity(),info,Toast.LENGTH_SHORT).show();
    }

    /**
     * 跳转到wifi设置界面
     */
    public void toWifiSetting(/*String methodName*/){
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
//        //都可以用
//        switch (methodName){
//            case "setAction_string":
//                intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
//                break;
//            case "setAction_class":
//                intent.setAction(Settings.ACTION_WIFI_SETTINGS);
//                break;
//            case "setAction_className":
//                if (Build.VERSION.SDK_INT >= 11){
//                    intent.setClassName("com.android.settings",
//                            "com.android.settings.Settings$WifiSettingsActivity");
//                }else{
//                    intent.setClassName("com.android.settings", "com.android.settings.wifi.WifiSettings");
//                }
//                break;
//        }

        getActivity().startActivity(intent);
    }

    /**
     * 获取当前连接的wifi的名称
     * @return
     */
    String getWifiName(){
        String[] needPermissions = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            needPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        if (needPermissions != null){
            if (!checkIsAllPermissionsGranted(needPermissions)){
                Toast.makeText(getActivity(), "需要获取相关权限", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(getActivity(), needPermissions, REQUEST_LOCATION);
                return "请允许获取权限";
            }
        }

        return getWifiNameInternal();
    }

    /**
     * 判断是否所有权限都允许该应用获取
     * @param permissions
     * @return
     */
    boolean checkIsAllPermissionsGranted(String[] permissions){
        boolean ret = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission :
                    permissions) {
                if (getActivity().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * 获取连接的wifi的名称
     * @return
     */
    String getWifiNameInternal(){
        String ssid = "";

        /**
         * android9.0以上需要申请定位权限
         *
         * android10.0需要申请新添加的隐私权限ACCESS_FINE_LOCATION
         */
        if (Build.VERSION.SDK_INT >= 29){
            WifiManager wifiMgr = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            ssid = wifiInfo.getSSID();
        }else if (Build.VERSION.SDK_INT >= 23){
            ConnectivityManager connectMgr = (ConnectivityManager) this.getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
            ssid = networkInfo.getExtraInfo();
        }else{
            WifiManager wifiMgr = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            ssid = wifiInfo.getSSID();
        }

        return ssid.replace("\"", "");
    }

    /**
     * 重启应用
     * @param delay 延时秒数
     */
    public void restartApp(int delay){
        Intent restartIntent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, restartIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + delay, pendingIntent);
        getActivity().finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
