package senduo.com.blelib.callback;


import android.bluetooth.BluetoothGattCharacteristic;

public abstract class BleCharacterCallback {
	
	public abstract void onSuccess(BluetoothGattCharacteristic characteristic);

}
