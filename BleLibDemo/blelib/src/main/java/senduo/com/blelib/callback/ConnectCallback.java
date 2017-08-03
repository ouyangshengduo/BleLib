package senduo.com.blelib.callback;


import android.bluetooth.BluetoothGatt;

public abstract class ConnectCallback {
	
	public abstract void onConnectState(int state);
	public abstract void onServicesFound(BluetoothGatt gatt, int status);

}
