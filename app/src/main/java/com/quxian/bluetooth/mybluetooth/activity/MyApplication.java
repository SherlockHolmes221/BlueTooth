package com.quxian.bluetooth.mybluetooth.activity;

import android.app.Application;

import com.inuker.bluetooth.library.BluetoothContext;

public class MyApplication extends Application {

    private static MyApplication instance;

    public static Application getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        BluetoothContext.set(this);

    }
}