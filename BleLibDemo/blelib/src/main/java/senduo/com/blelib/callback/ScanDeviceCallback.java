package senduo.com.blelib.callback;


import android.bluetooth.BluetoothDevice;

public abstract class ScanDeviceCallback {
	
	public abstract void onDeviceFound(BluetoothDevice device);

}
