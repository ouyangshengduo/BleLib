package senduo.com.blelib.callback;



import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

public abstract class MacScanCallback implements LeScanCallback {
	
	private String mac;
	private boolean hasFound = false;
	public MacScanCallback(String mac) {
        this.mac = mac;
        hasFound = false;
        if (mac == null) {
            throw new IllegalArgumentException("start scan, mac can not be null!");
        }
    }

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		// TODO Auto-generated method stub
		if (device == null)
            return;
        if (TextUtils.isEmpty(device.getAddress())) {
            return;
        }
        if (!hasFound) {
            if (mac.equalsIgnoreCase(device.getAddress())) {
            	hasFound = true;
                onDeviceFound(device);
            }
        }
	}
	
	public abstract void onDeviceFound(BluetoothDevice device);
	
	

}
