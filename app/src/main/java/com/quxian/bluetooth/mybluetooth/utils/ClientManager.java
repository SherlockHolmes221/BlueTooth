package com.quxian.bluetooth.mybluetooth.utils;

import com.inuker.bluetooth.library.BluetoothClient;
import com.quxian.bluetooth.mybluetooth.activity.MyApplication;

public class ClientManager {

    private static BluetoothClient mClient;

    public static BluetoothClient getClient() {
        if (mClient == null) {
            synchronized (ClientManager.class) {
                if (mClient == null) {
                    mClient = new BluetoothClient(MyApplication.getInstance());
                }
            }
        }
        return mClient;
    }
}
