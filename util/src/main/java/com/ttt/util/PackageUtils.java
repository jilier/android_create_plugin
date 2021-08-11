package com.ttt.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
//import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;

import androidx.core.content.FileProvider;

class PackageUtils {

    /*
     * 普通安装
     */
    public static boolean installNormal(Context context, String apkFullPath) {
        try {
            //Toast.makeText(context, "install apk normal", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            File apkFile = new File(apkFullPath);
            Uri uri = null;
            if (Build.VERSION.SDK_INT >= 24) {
                String path = context.getPackageName() + ".fileProvider";
                uri = FileProvider.getUriForFile(context, path, apkFile);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(apkFile);
            }
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            //解决安卓8.0安装界面不弹出
            //查询所有符合 intent 跳转目标应用类型的应用，注意此方法必须放置在 setDataAndType 方法之后
            List<ResolveInfo> resolveLists = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            // 然后全部授权
            for (ResolveInfo resolveInfo : resolveLists) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            context.startActivity(intent);

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(context, "error " + e.getMessage(), Toast.LENGTH_LONG).show();

            return false;
        }
    }

    public static boolean clientInstall(Context context, String apkPath){
        PrintWriter PrintWriter = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            PrintWriter = new PrintWriter(process.getOutputStream());
            PrintWriter.println("chmod 777 "+apkPath);
            PrintWriter.println("export LD_LIBRARY_PATH=/vendor/lib:/system/lib");
            PrintWriter.println("pm install -r "+apkPath);
//          PrintWriter.println("exit");
            PrintWriter.flush();
            PrintWriter.close();
            int value = process.waitFor();
            return returnResult(value);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }finally{
            if(process!=null){
                process.destroy();
            }
        }
        return false;
    }

    private static boolean returnResult(int value){
        // 代表成功
        if (value == 0) {
            return true;
        } else if (value == 1) { // 失败
            return false;
        } else { // 未知情况
            return false;
        }
    }

    /*
     * 静默安装
     */
    public static void installSilent(Context context, String apkPath){
        int ret = -1;
        DataOutputStream out = null;
        try {
            //申请su权限
            Process process = Runtime.getRuntime().exec("su");
            out = new DataOutputStream(process.getOutputStream());
            //执行pm install命令
            String command = "pm install -r " + apkPath + "\n";
            out.write(command.getBytes(Charset.forName("utf-8")));
            out.flush();
            out.writeBytes("exit\n");
            out.flush();

            out.close();
        } catch (IOException e) {
            e.printStackTrace();

            Toast.makeText(context, ret + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static boolean installSlient(Context context, String path) {
        //Toast.makeText(context, "install apk silent", Toast.LENGTH_LONG).show();
//        String cmd = "pm install -r " + apk.getPath();
        String cmd = "pm install -r " + path;
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        try {
            //静默安装需要root权限
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.write(cmd.getBytes());
            os.writeBytes("\n");
            os.writeBytes("exit\n");
            os.flush();

            //定时器
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            PendingIntent restartIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {// 6.0及以上
                mgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, restartIntent);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {// 4.4及以上
                mgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, restartIntent);
            }

            //执行命令
            process.waitFor();

            //获取返回结果
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();

            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }

            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();

            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();

            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        Log.e(TAG, "=================== successMsg: " + successMsg.toString() + ", errorMsg: " + errorMsg.toString());
//
//        //安装成功
//        if ("Success".equals(successMsg.toString())) {
//
//            Log.e(TAG, "======= apk install success");
//
//        }
    }














    public static void restartApp(Context context, String packageName){
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void reStart(Context context){
        context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }
}
