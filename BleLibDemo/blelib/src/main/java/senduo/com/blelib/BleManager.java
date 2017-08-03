package senduo.com.blelib;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import java.util.UUID;

import senduo.com.blelib.callback.BleCharacterCallback;
import senduo.com.blelib.callback.ConnectCallback;
import senduo.com.blelib.callback.MacScanCallback;
import senduo.com.blelib.callback.ScanDeviceCallback;
import senduo.com.blelib.utils.BleLog;
import senduo.com.blelib.utils.ConstConnectState;

public class BleManager {
	
	private Context mContext;
	private BluetoothAdapter bluetoothAdapter;
	private boolean mScanning;
    private Handler mHandler;
    private BluetoothGatt mBluetoothGatt;
    private String TAG = "BleManager";

    private static final int REQUEST_ENABLE_BT = 1;
    // 10秒后停止查找搜索.
    private static final long SCAN_PERIOD = 10000;
    
    private String uuid_notify = "";
    private String uuid_write = "";
    
    private ScanDeviceCallback mScanDeviceCallback;
    private BluetoothGattService service;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattDescriptor descriptor;
    private ConnectCallback mConnectCallback;
    private BleCharacterCallback mBleCharacterCallback;
    private static final String UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = "00002902-0000-1000-8000-00805f9b34fb";

    private BluetoothGattCallback mBluetoothGattCallback;
    
    private static BleManager mInstance = null;
	
	private BleManager(Context mContext){
		this.mContext = mContext.getApplicationContext();
		mHandler = new Handler();
		// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        final BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
        
	}
	
	/**
	 * 使用单例获取一个操作类
	 * @param mContext
	 * @return
	 */
	public static BleManager getInstance(Context mContext){
		if(mInstance == null){
			synchronized (BleManager.class) {
				if(mInstance == null){
					mInstance = new BleManager(mContext);
				}
			}
		}
		return mInstance;
	}
	
	/**
	 * 是否支持BLE4.0
	 * @return
	 */
	public boolean isSupportBLE(){
		if (mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			if(bluetoothAdapter != null){
				return true;
			}else{
				return false;
			}
        }else{
        	return false;
        }
	}
	
	public void enableBluetoothIfDisabled() {
        if (!isBlueEnable()) {
            enableBluetooth();
        }
    }
	
	public boolean isBlueEnable() {
        return bluetoothAdapter.isEnabled();
    }

    public void enableBluetooth() {
        bluetoothAdapter.enable();
    }

    public void disableBluetooth() {
        bluetoothAdapter.disable();
    }
    
    /*public boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                final boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
                BleLog.i("Refreshing result: " + success);
                return success;
            }
        } catch (Exception e) {
            BleLog.i("An exception occured while refreshing device", e);
        }
        return false;
    }*/
    
    public BluetoothGatt getBluetoothGatt() {
        return mBluetoothGatt;
    }
    
    public void closeBluetoothGatt() {
    	
    	mBluetoothGattCallback = null;
        if (mBluetoothGatt != null) {
        	mBluetoothGatt.disconnect();
        }

        /*if (mBluetoothGatt != null) {
            refreshDeviceCache();
        }*/

        if (mBluetoothGatt != null) {
        	mBluetoothGatt.close();
        }
    }
    
	
	/**
	 * 停止搜索
	 */
	public void stopScan(){
		mScanning = false;
        bluetoothAdapter.stopLeScan(mLeScanCallback);
	}
	
	public void scanDevice(ScanDeviceCallback scanDeviceCallback){
		
		this.mScanDeviceCallback = scanDeviceCallback;
		if(mScanning){
			stopScan();
		}
		scanLeDevice(true);
	}
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
        	if(mScanDeviceCallback != null){
        		mScanDeviceCallback.onDeviceFound(device);
        	}
        }
    };
	
    /**
     * 开始搜索设备
     * @param enable
     */
	private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                	stopScan();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
        	stopScan();
        }
    }
	
	/**
	 * 连接设备
	 * @param deviceMac
	 * @param connectCallback
	 */
	public void connectDevice(String deviceMac,ConnectCallback connectCallback){
		this.mConnectCallback = connectCallback;
		
		bluetoothAdapter.startLeScan(new MacScanCallback(deviceMac){

			@Override
			public void onDeviceFound(BluetoothDevice device) {
				// TODO Auto-generated method stub
				bluetoothAdapter.stopLeScan(this);
				if(mBluetoothGattCallback == null){
					initGattCallback();
				}
				mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
			}
			
		});
	}
	
	private synchronized void initGattCallback(){
		if(mBluetoothGattCallback == null){
			mBluetoothGattCallback = new BluetoothGattCallback() {
				
				 @Override
			        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			            String intentAction;
			            if (newState == BluetoothProfile.STATE_CONNECTED) {
			            	mConnectCallback.onConnectState(ConstConnectState.CONNECTED);
			            	gatt.discoverServices();

			            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
			            	mConnectCallback.onConnectState(ConstConnectState.DISCONNECTED);
			            }
			        }

			        @Override
			        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			            if (status == BluetoothGatt.GATT_SUCCESS) {
			                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
			            	mConnectCallback.onServicesFound(gatt, status);
			            } else {
			                Log.w(TAG, "onServicesDiscovered received: " + status);
			            }
			        }

			        @Override
			        public void onCharacteristicRead(BluetoothGatt gatt,
			                                         BluetoothGattCharacteristic characteristic,
			                                         int status) {
			            if (status == BluetoothGatt.GATT_SUCCESS) {
			               // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			            }
			        }

			        @Override
			        public void onCharacteristicChanged(BluetoothGatt gatt,
			                                            BluetoothGattCharacteristic characteristic) {
			            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
			        	if(characteristic.getUuid().equals(UUID.fromString(uuid_notify))){
			        		BleLog.e(TAG, "=========源头收到数据===============");
			        		mBleCharacterCallback.onSuccess(characteristic);
			        	}
			        }
			};
		}
	}

	
	/**
	 * 连接设备
	 * @param device
	 * @param connectCallback
	 */
	public void connectDevice(BluetoothDevice device,ConnectCallback connectCallback){
		this.mConnectCallback = connectCallback;
		if(device != null){
			mConnectCallback.onConnectState(ConstConnectState.CONNECTING);
			if(mBluetoothGattCallback == null){
				initGattCallback();
			}
			mBluetoothGatt = device.connectGatt(mContext, false, mBluetoothGattCallback);
		}
		
	}
	
	
	private UUID formUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }
	
	/**
     * notify setting
     */
    private boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                                  boolean enable) {
        if (mBluetoothGatt == null || characteristic == null) {
            BleLog.e(TAG, "gatt or characteristic equal null");
            return false;
        }

        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
            BleLog.e(TAG, "Check characteristic property: false");
            return false;
        }

        boolean success = mBluetoothGatt.setCharacteristicNotification(characteristic, enable);
        BleLog.d(TAG, "setCharacteristicNotification: " + enable
                + "\nsuccess: " + success
                + "\ncharacteristic.getUuid(): " + characteristic.getUuid());

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                formUUID(UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR));
        if (descriptor != null) {
            descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE :
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            return mBluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }
    
	
    /**
     * 
     * @param uuid_service
     * @param uuid_notify
     * @param callback
     * @return
     */
	public boolean enableNotify(String uuid_service,String uuid_notify,BleCharacterCallback callback){
		this.uuid_notify = uuid_notify;
		this.mBleCharacterCallback = callback;
		UUID serviceUUID = formUUID(uuid_service);
		if (serviceUUID != null && mBluetoothGatt != null) {
            service = mBluetoothGatt.getService(serviceUUID);
        }else{
        	return false;
        }

		UUID charactUUID = formUUID(uuid_notify);
        if (service != null && charactUUID != null) {
            characteristic = service.getCharacteristic(charactUUID);
            return setCharacteristicNotification(characteristic, true);
        }else{
        	return false;
        }
        
	}
	
	public boolean disableNotify(String uuid_service,String uudi_notify){
		
		UUID serviceUUID = formUUID(uuid_service);
		if (serviceUUID != null && mBluetoothGatt != null) {
            service = mBluetoothGatt.getService(serviceUUID);
        }else{
        	return false;
        }

		UUID charactUUID = formUUID(uudi_notify);
		
		if (service != null && charactUUID != null) {
            characteristic = service.getCharacteristic(charactUUID);
            return setCharacteristicNotification(characteristic, false);
        }else{
        	return false;
        }
	}
	
	public boolean writeDevice(String uuid_service,String uuid_write,byte[] data){
		if (data == null)
            return false;
		this.uuid_write = uuid_write;
		
		UUID serviceUUID = formUUID(uuid_service);
		if (serviceUUID != null && mBluetoothGatt != null) {
            service = mBluetoothGatt.getService(serviceUUID);
        }else{
        	return false;
        }

		UUID charactUUID = formUUID(uuid_write);
        if (service != null && charactUUID != null) {
            characteristic = service.getCharacteristic(charactUUID);
            characteristic.setValue(data);
            return mBluetoothGatt.writeCharacteristic(characteristic);
        }else{
        	return false;
        }
		
	}

}
