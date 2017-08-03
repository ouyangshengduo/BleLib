package senduo.com.blelibdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import senduo.com.blelib.BleManager;
import senduo.com.blelib.callback.BleCharacterCallback;
import senduo.com.blelib.callback.ConnectCallback;
import senduo.com.blelib.callback.ScanDeviceCallback;
import senduo.com.blelib.utils.BleLog;
import senduo.com.blelib.utils.ConstConnectState;

public class MainActivity extends AppCompatActivity {

    private Button btnScanDevice,btnOpenNotify,btnCloseNotify,btnSendData,btnClear,btnDisconnect;
    private ListView lvBluetooth;
    private TextView tvConnectInfo,tvShowDeviceData,tvSendResponse;
    private EditText etSendData;
    private List<BluetoothDevice> bluetoothDevices;

    // 下面的所有UUID及指令请根据实际设备替换
    private static final String UUID_SERVICE = "0000ff10-0000-1000-8000-00805f9b34fb";
    private static final String UUID_NOTIFY = "0000ff12-0000-1000-8000-00805f9b34fb";
    private static final String UUID_WRITE = "0000ff11-0000-1000-8000-00805f9b34fb";
    private static int send_cnt = 0;

    private static final int RECEIVE_DEVICE_DATA = 1000;//获取到设备数据
    private BleManager mBleManager;
    private IntentFilter intentFilter;
    private ConnectBroadcast connectBroadcast;

    private String TAG = "MainActivity";

    private BlueAdapter mBlueAdapter;


    private Handler mHandler = new Handler(){

        public void handleMessage(android.os.Message msg) {
            switch(msg.what){
                case ConstConnectState.CONNECTING:
                    tvConnectInfo.setText("连接中...");
                    tvConnectInfo.setTextColor(Color.BLUE);
                    break;
                case ConstConnectState.CONNECTED:
                    tvConnectInfo.setText("设备已连接");
                    tvConnectInfo.setTextColor(Color.GREEN);
                    break;
                case ConstConnectState.DISCONNECTED:
                    tvConnectInfo.setText("设备未连接");
                    tvConnectInfo.setTextColor(Color.RED);
                    break;
                case RECEIVE_DEVICE_DATA:
                    if(msg.obj != null){
                        if(tvShowDeviceData != null){
                            send_cnt ++;
                            if(send_cnt > 100000000){
                                send_cnt = 1;
                            }
                            String str = tvShowDeviceData.getText().toString() + "\n";
                            str = str + send_cnt + ":" + (String)msg.obj;
                            tvShowDeviceData.setText(str);
                        }

                    }
                    break;
            }
        };

    };

    /**
     * 连接广播接受
     */
    class ConnectBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    tvConnectInfo.setText("设备未连接");
                    tvConnectInfo.setTextColor(Color.RED);
                    break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBleManager = BleManager.getInstance(this);
        initView();
        connectBroadcast = new ConnectBroadcast();
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(connectBroadcast,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectBroadcast);
    }

    private void initView(){

        btnScanDevice = (Button) findViewById(R.id.btnScanDevice);
        btnOpenNotify = (Button) findViewById(R.id.btnOpenNotify);
        btnCloseNotify = (Button) findViewById(R.id.btnCloseNotify);
        btnDisconnect = (Button) findViewById(R.id.btnDisconnect);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnSendData = (Button) findViewById(R.id.btnSendData);
        etSendData = (EditText) findViewById(R.id.etSendData);
        lvBluetooth = (ListView) findViewById(R.id.lvbluetooth);
        tvConnectInfo = (TextView) findViewById(R.id.tvConnectInfo);
        tvShowDeviceData = (TextView) findViewById(R.id.tvShowDeviceData);
        tvSendResponse = (TextView) findViewById(R.id.tvSendResponse);
        tvShowDeviceData.setMovementMethod(ScrollingMovementMethod.getInstance());
        bluetoothDevices = new ArrayList<BluetoothDevice>();
        mBlueAdapter = new BlueAdapter(MainActivity.this, bluetoothDevices);
        lvBluetooth.setAdapter(mBlueAdapter);
        tvSendResponse.setText("");
        lvBluetooth.setVisibility(View.GONE);

        lvBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (!bluetoothDevices.get(position).getAddress().toString()
                        .equals("")) {
                    if (mBleManager != null) {
                        lvBluetooth.setVisibility(View.GONE);
                        mBleManager.connectDevice(bluetoothDevices.get(position),
                                new ConnectCallback() {

                                    @Override
                                    public void onConnectState(int state) {
                                        // TODO Auto-generated method stub
                                        mHandler.sendEmptyMessage(state);
                                    }

                                    @Override
                                    public void onServicesFound(
                                            BluetoothGatt gatt, int status) {
                                        // TODO Auto-generated method stub
                                        Log.e(TAG,"发现服务");
                                    }
                                });
                    }
                }

            }
        });


        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBleManager != null){
                    mBleManager.closeBluetoothGatt();
                }
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvShowDeviceData.setText("");
            }
        });
        send_cnt = 0;
        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputData = etSendData.getText().toString().trim();
                if(!inputData.equals("")){
                    boolean response = mBleManager.writeDevice(UUID_SERVICE,UUID_WRITE,CommonTool.hexStringToBytes(inputData));
                    send_cnt ++;
                    if(send_cnt > 100000000){
                        send_cnt = 1;
                    }
                    if(response){
                        tvSendResponse.setText(send_cnt + ":下发成功");
                    }else{
                        tvSendResponse.setText(send_cnt + ":下发失败");
                    }
                }else{
                    Toast.makeText(MainActivity.this,"不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnScanDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                bluetoothDevices.clear();
                mBlueAdapter.notifyDataSetChanged();
                lvBluetooth.setVisibility(View.VISIBLE);
                mBleManager.scanDevice(new ScanDeviceCallback() {

                    @Override
                    public void onDeviceFound(BluetoothDevice device) {
                        // TODO Auto-generated method stub
                        if(!bluetoothDevices.contains(device)){
                            bluetoothDevices.add(device);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mBlueAdapter.setDeviceList(bluetoothDevices);
                                }
                            });
                        }
                    }
                });
            }
        });

        btnOpenNotify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mBleManager != null) {
                    boolean response = mBleManager.enableNotify(UUID_SERVICE, UUID_NOTIFY, new BleCharacterCallback() {

                        @Override
                        public void onSuccess(BluetoothGattCharacteristic characteristic) {
                            // TODO Auto-generated method stub
                            Message msg = mHandler.obtainMessage();
                            msg.what = RECEIVE_DEVICE_DATA;
                            msg.obj = CommonTool.bytesToHex(characteristic.getValue(), characteristic.getValue().length);
                            mHandler.sendMessage(msg);
                        }
                    });
                    if(response){
                        BleLog.e(TAG, "通知服务开启成功");
                    }
                }
            }
        });

        btnCloseNotify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mBleManager != null) {
                    boolean response = mBleManager.disableNotify(UUID_SERVICE, UUID_NOTIFY);
                    if(response){
                        BleLog.e(TAG, "通知服务开启成功");
                    }
                }
            }
        });

    }
}
