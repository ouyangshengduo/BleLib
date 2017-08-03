package senduo.com.blelibdemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


public class BlueAdapter extends BaseAdapter{
	private int viewId;
	private Context mContext;
	private List<BluetoothDevice> blueToothList;
	public BlueAdapter(Context context,List<BluetoothDevice> deviceList) {
		this.mContext = context;
		this.blueToothList = deviceList;
	}
	
	public BlueAdapter setDeviceList(List<BluetoothDevice> deviceList) {
        this.blueToothList = deviceList;
        notifyDataSetChanged();
        return this;
    }

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return blueToothList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return blueToothList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item_layout, null);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.deviceMac = (TextView) convertView.findViewById(R.id.address);
            convertView.setTag(viewHolder);
        } else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(blueToothList != null && blueToothList.get(position) != null){
            String deviceName = blueToothList.get(position).getName();
            if(deviceName != null && !deviceName.isEmpty()){
                viewHolder.deviceName.setText(deviceName);
            } else{
                viewHolder.deviceName.setText("未知设备");
            }
            viewHolder.deviceMac.setText(blueToothList.get(position).getAddress());
        }
        
        return convertView;
	}
	
	
	class ViewHolder{
        TextView deviceName;
        TextView deviceMac;
    }

}
