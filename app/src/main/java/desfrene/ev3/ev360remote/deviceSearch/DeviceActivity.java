package desfrene.ev3.ev360remote.deviceSearch;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import desfrene.ev3.ev360remote.Constants;
import desfrene.ev3.ev360remote.R;

public class DeviceActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    public static final String TARGET_DEVICE = "ev360rotate.targetDevice";

    List<BluetoothDevice> deviceList = new ArrayList<>();
    List<String> macList = new ArrayList<>();
    List<String> doNotDiscover = new ArrayList<>();
    DeviceAdapter displayAdapter;

    BluetoothAdapter adapter;
    boolean bluetoothDiscaredByUser = false;

    Button but;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        but = findViewById(R.id.button);
        but.setText(R.string.device_loading);
        but.setEnabled(false);

        ListView listView = findViewById(R.id.listView);

        displayAdapter = new DeviceAdapter(deviceList);
        displayAdapter.notifyDataSetChanged();
        listView.setAdapter(displayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                confirmDevice(deviceList.get(position));
            }
        });
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter == null) {
            abortedByUser();
        }

        if (adapter.isEnabled()) {
            enableReceiver();
        } else {
            requestBluetooth();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    private void requestBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    private void enableReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);

        but.setEnabled(true);
        but.setText(R.string.device_scan);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.isDiscovering()) {
                    adapter.cancelDiscovery();
                }
                adapter.startDiscovery();
            }
        });
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            assert action != null;

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device1 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device1 == null)
                        throw new AssertionError("Device Found is 'null'");

                    if (!macList.contains(device1.getAddress())) {
                        deviceList.add(device1);
                        macList.add(device1.getAddress());
                        displayAdapter.notifyDataSetChanged();

                        device1.fetchUuidsWithSdp();
                    }

                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                    macList.clear();
                    deviceList.clear();
                    displayAdapter.notifyDataSetChanged();

                    but.setText(R.string.device_scanning);
                    but.setEnabled(false);
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    Toast.makeText(getApplicationContext(), R.string.toast_device_found, Toast.LENGTH_LONG).show();
                    but.setText(R.string.device_scan);
                    but.setEnabled(true);
                    break;
                case BluetoothDevice.ACTION_UUID:
                    BluetoothDevice device2 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Parcelable[] found_uuid_list = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);

                    if (device2 == null || found_uuid_list == null) {
                        return;
                    }

                    if (doNotDiscover.contains(device2.getAddress())) {
                        return;
                    }

                    for (Parcelable uuid : found_uuid_list) {
                        UUID uid = ((ParcelUuid) uuid).getUuid();

                        if (uid.equals(Constants.TARGET_UUID)) {
                            autoFoundDeviceDialog(device2);
                        }
                    }
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    enableReceiver();
                } else {
                    if (bluetoothDiscaredByUser) {
                        abortedByUser();
                    } else {
                        bluetoothDiscaredByUser = true;
                        requestBluetoothDialog();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void requestBluetoothDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.device_msg)
                .setPositiveButton(R.string.device_on, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestBluetooth();
                    }
                })
                .setNegativeButton(R.string.device_off, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        abortedByUser();
                    }
                })
                .create()
                .show();
    }

    private void autoFoundDeviceDialog(final BluetoothDevice dev) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.device_found, dev.getName()))
                .setPositiveButton(R.string.device_use, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        confirmDevice(dev);
                    }
                })
                .setNegativeButton(R.string.device_wait, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        doNotDiscover.add(dev.getAddress());
                    }
                })
                .create()
                .show();
    }

    private void abortedByUser() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void confirmDevice(BluetoothDevice dev) {
        Intent returnData = new Intent();
        returnData.putExtra(TARGET_DEVICE, dev.getAddress());
        setResult(RESULT_OK, returnData);
        finish();
    }
}