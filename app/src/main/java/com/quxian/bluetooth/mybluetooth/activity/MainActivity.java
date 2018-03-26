package com.quxian.bluetooth.mybluetooth.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.beacon.BeaconItem;
import com.inuker.bluetooth.library.beacon.BeaconParser;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.quxian.bluetooth.mybluetooth.R;
import com.quxian.bluetooth.mybluetooth.adapter.DeviceListAdapter;
import com.quxian.bluetooth.mybluetooth.utils.AppConstants;
import com.quxian.bluetooth.mybluetooth.view.PullRefreshListView;
import com.quxian.bluetooth.mybluetooth.view.PullToRefreshFrameLayout;

import java.util.ArrayList;
import java.util.List;

/*
显示蓝牙搜索的设备
 */
public class MainActivity extends AppCompatActivity {

   // private static final String MAC = "B0:D5:9D:6F:E7:A5";

    private PullToRefreshFrameLayout mRefreshLayout;
    private PullRefreshListView mListView;
    private DeviceListAdapter mAdapter;
    private TextView mTvTitle;

    private List<SearchResult> mDevices;//SearchResult : device rssi scanRecord name address rssi beacon
    private BluetoothClient mClient ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClient = new BluetoothClient(this);
        mDevices = new ArrayList<SearchResult>();

        mTvTitle = (TextView) findViewById(R.id.title);

        mRefreshLayout = (PullToRefreshFrameLayout) findViewById(R.id.pulllayout);

        mListView = mRefreshLayout.getPullToRefreshListView();
        mAdapter = new DeviceListAdapter(this);
        mListView.setAdapter(mAdapter);

        mListView.setOnRefreshListener(new PullRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO Auto-generated method stub
                searchDevice();
            }
        });

        //搜索蓝牙设备
        searchDevice();

        //监听蓝牙状态开关
        mClient.registerBluetoothStateListener(new BluetoothStateListener() {
            @Override
            public void onBluetoothStateChanged(boolean openOrClosed) {
                BluetoothLog.v(String.format("onBluetoothStateChanged %b", openOrClosed));
            }
        });
    }

    private void searchDevice() {
        //2 设备扫描
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();
//        SearchRequest request = new SearchRequest.Builder()
//                .searchBluetoothLeDevice(5000, 2).build();
        mClient.search(request, mSearchResponse);
    }

    //搜索返回的信息
    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            BluetoothLog.w("MainActivity.onSearchStarted");
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);//0
            mTvTitle.setText(R.string.string_refreshing);//扫描蓝牙中…
            mDevices.clear();
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                mAdapter.setDataList(mDevices);

                Log.e("device info :",device.toString());

                //解析获取的SearchResult中的scanRecords扫描信息
                Beacon beacon = new Beacon(device.scanRecord);
                BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));

                Log.e("beacon.mBytes:", String.valueOf(beacon.mBytes));
                Log.e("beacon.mItems:", beacon.mItems.toString());

                for(int i = 0;i<beacon.mItems.size();i++){
                    BeaconItem beaconItem = beacon.mItems.get(0);
                    BeaconParser beaconParser = new BeaconParser(beaconItem);
                    int firstByte = beaconParser.readByte(); // 读取第1个字节
                    int secondByte = beaconParser.readByte(); // 读取第2个字节
                    int productId = beaconParser.readShort(); // 读取第3,4个字节
                    boolean bit1 = beaconParser.getBit(firstByte, 0); // 获取第1字节的第1bit
                    boolean bit2 = beaconParser.getBit(firstByte, 1); // 获取第1字节的第2bit
                    beaconParser.setPosition(0); // 将读取起点设置到第1字节处
                }

            }

            if (mDevices.size() > 0) {
                mRefreshLayout.showState(AppConstants.LIST);
            }
        }

        @Override
        public void onSearchStopped() {
            BluetoothLog.w("MainActivity.onSearchStopped");
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);
            mTvTitle.setText(R.string.devices);//设备列表
        }

        @Override
        public void onSearchCanceled() {
            BluetoothLog.w("MainActivity.onSearchCanceled");
            mListView.onRefreshComplete(true);
            mRefreshLayout.showState(AppConstants.LIST);
            mTvTitle.setText(R.string.devices);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mClient.stopSearch();
    }
}
