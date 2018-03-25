package com.quxian.bluetooth.mybluetooth.utils;

import android.widget.Toast;

import com.quxian.bluetooth.mybluetooth.activity.MyApplication;

public class CommonUtils {

    public static void toast(String text) {
        Toast.makeText(MyApplication.getInstance(), text, Toast.LENGTH_SHORT).show();
    }
}