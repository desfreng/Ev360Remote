package desfrene.ev3.ev360remote.deviceSearch;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import desfrene.ev3.ev360remote.R;


public class DeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> _data;

    DeviceAdapter(List<BluetoothDevice> data) {
        _data = data;
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return _data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_row_item, parent, false);

        }
        BluetoothDevice device = getItem(position);

        TextView address = convertView.findViewById(R.id.addressView);
        address.setText(device.getAddress());

        TextView name = convertView.findViewById(R.id.nameView);
        if (device.getName() == null) {
            name.setText(R.string.device_unknown);
        } else {
            name.setText(device.getName());
        }
        return convertView;
    }
}