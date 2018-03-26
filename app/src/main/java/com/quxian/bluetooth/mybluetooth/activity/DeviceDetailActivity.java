package com.quxian.bluetooth.mybluetooth.activity;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.BluetoothUtils;
import com.quxian.bluetooth.mybluetooth.R;
import com.quxian.bluetooth.mybluetooth.adapter.DeviceDetailAdapter;
import com.quxian.bluetooth.mybluetooth.bean.DetailItem;

import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;

public class DeviceDetailActivity extends Activity {

    private TextView mTvTitle;
    private ProgressBar mPbar;

    private ListView mListView;
    private DeviceDetailAdapter mAdapter;

    private SearchResult mResult;

    private BluetoothDevice mDevice;

    private boolean mConnected;
    private BluetoothClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail_activity);

        mClient = new BluetoothClient(this);
        Intent intent = getIntent();
        String mac = intent.getStringExtra("mac");
        Log.e("mac",mac);
       // mResult = intent.getParcelableExtra("device");

        mDevice = BluetoothUtils.getRemoteDevice(mac);

        mTvTitle = (TextView) findViewById(R.id.title);
        mTvTitle.setText(mDevice.getAddress());

        mPbar = (ProgressBar) findViewById(R.id.pbar);

        mListView = (ListView) findViewById(R.id.listview);
        mAdapter = new DeviceDetailAdapter(this, mDevice);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mConnected) {
                    return;
                }
                DetailItem item = (DetailItem) mAdapter.getItem(position);
                if (item.type == DetailItem.TYPE_CHARACTER) {
                    BluetoothLog.v(String.format("click service = %s, character = %s", item.service, item.uuid));
                    startCharacterActivity(item.service, item.uuid);
                }
            }
        });

        mClient.registerConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);

        connectDeviceIfNeeded();
    }

    private final BleConnectStatusListener mConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            BluetoothLog.v(String.format("DeviceDetailActivity onConnectStatusChanged %d in %s",
                    status, Thread.currentThread().getName()));

            mConnected = (status == STATUS_CONNECTED);
            connectDeviceIfNeeded();
        }
    };

    private void startCharacterActivity(UUID service, UUID character) {
        Intent intent = new Intent(this, CharacterActivity.class);
        intent.putExtra("mac", mDevice.getAddress());
        intent.putExtra("service", service);
        intent.putExtra("character", character);
        startActivity(intent);
    }

    //连接设备并且显示
    private void connectDevice() {
        mTvTitle.setText(String.format("%s%s", getString(R.string.connecting), mDevice.getAddress()));
        mPbar.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);

        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)
                .setConnectTimeout(20000)
                .setServiceDiscoverRetry(3)
                .setServiceDiscoverTimeout(10000)
                .build();

        mClient.connect(mDevice.getAddress(), options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile profile) {
                BluetoothLog.v(String.format("profile:\n%s", profile));
                mTvTitle.setText(String.format("%s", mDevice.getAddress()));
                mPbar.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);

                //在adapter添加数据
                if (code == REQUEST_SUCCESS) {
                    mAdapter.setGattProfile(profile);
                }
            }
        });
    }

    private void connectDeviceIfNeeded() {
        if (!mConnected) {
            connectDevice();
        }
    }

    @Override
    protected void onDestroy() {
        mClient.disconnect(mDevice.getAddress());
        mClient.unregisterConnectStatusListener(mDevice.getAddress(), mConnectStatusListener);
        super.onDestroy();
    }
}
