package senduo.com.blelib.utils;

public class ConstConnectState {
	
	public final static int CONNECTED = 20000; //设备已连接
	public final static int CONNECTING = 20001;//设备连接中
	public final static int DISCONNECTING = 20002;//断开中
	public final static int DISCONNECTED = 20003;//设备未连接
	public final static int END_FIND = 20006;//结束查询
	public final static int BLE_SIGNAL_POOR = 20007;//蓝牙信号差
}
