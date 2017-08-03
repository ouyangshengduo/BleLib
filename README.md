# BleLib
自己整理的一个关于BLE4.0的简易操作库


# 对象的初始化
```
mBleManager = BleManager.getInstance(this);
```

# 设备的搜索

```

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
```

# 设备的连接

```
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
```

# 设备通知服务的绑定

```
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
```

# 设备通知服务的解除绑定

```

if (mBleManager != null) {
    boolean response = mBleManager.disableNotify(UUID_SERVICE, UUID_NOTIFY);
    if(response){
        BleLog.e(TAG, "通知服务开启成功");
    }
}
```

# 指令的下发
```

boolean response = mBleManager.writeDevice(UUID_SERVICE,UUID_WRITE,CommonTool.hexStringToBytes(inputData));
                   
if(response){
    tvSendResponse.setText("下发成功");
}else{
    tvSendResponse.setText("下发失败");
}
```
