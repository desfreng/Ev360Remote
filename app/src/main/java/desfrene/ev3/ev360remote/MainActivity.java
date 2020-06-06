package desfrene.ev3.ev360remote;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import desfrene.ev3.ev360remote.controller.ControllerActivity;
import desfrene.ev3.ev360remote.deviceSearch.DeviceActivity;

import static desfrene.ev3.ev360remote.Constants.FCT_TAG;

public class MainActivity extends AppCompatActivity {

    private static final int DEVICE_ADDRESS = 1;
    private static final int TEST = 2;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private static final int REQUEST_BLUETOOTH_ADMIN_PERMISSION = 2;
    private static final int REQUEST_ACCESS_FINE_LOCATION_PERMISSION = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        checkPermissions();

        startActivityForResult(new Intent(this, ControllerActivity.class), TEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case DEVICE_ADDRESS:
                if (resultCode == RESULT_OK) {
                    String address = Objects.requireNonNull(data).getStringExtra(DeviceActivity.TARGET_DEVICE);
                    Toast.makeText(this, String.format("Target Device %s", address), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(this, R.string.toast_device_abort, Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            case TEST:
                finish();
                break;
            default:
                break;
        }
    }

    private void checkPermissions() {
        boolean perms_ok = true;

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.i(FCT_TAG, "Bluetooth Permission not accessible. Send Request.");
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
            perms_ok = false;
        } else {
            Log.i(FCT_TAG, "Bluetooth Permission granted");
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            Log.i(FCT_TAG, "Bluetooth Admin Permission not accessible. Send Request.");
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_ADMIN_PERMISSION);
            perms_ok = false;
        } else {
            Log.i(FCT_TAG, "Bluetooth Admin Permission granted");
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(FCT_TAG, "Access Fine Location Permission not accessible. Send Request.");
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION_PERMISSION);
            perms_ok = false;
        } else {
            Log.i(FCT_TAG, "Access Fine Location Permission granted");
        }

        if (perms_ok) {
            startActivityForResult(new Intent(this, DeviceActivity.class), DEVICE_ADDRESS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> perm_array = Arrays.asList(permissions);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSION)
            checkForSinglePermission(Manifest.permission.BLUETOOTH, perm_array, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_ADMIN_PERMISSION)
            checkForSinglePermission(Manifest.permission.BLUETOOTH_ADMIN, perm_array, grantResults);

        if (requestCode == REQUEST_ACCESS_FINE_LOCATION_PERMISSION)
            checkForSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION, perm_array, grantResults);


        checkPermissions();
    }

    private void checkForSinglePermission(String permission, List<String> perm_array, @NonNull int[] grantResults) {
        if (perm_array.contains(permission)) {
            if (grantResults[perm_array.indexOf(permission)] == PackageManager.PERMISSION_GRANTED) {
                Log.i(FCT_TAG, String.format("%s permission granted", permission));
            } else {
                Log.i(FCT_TAG, String.format("%s permission denied", permission));
                if (shouldShowRequestPermissionRationale(permission)) {
                    showMessageOKCancel(permission);
                } else
                    permissionFailure();
            }
        }
    }

    private void showMessageOKCancel(final String permission) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_msg)
                .setPositiveButton(R.string.permission_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch (permission) {
                            case Manifest.permission.BLUETOOTH:
                                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
                                break;
                            case Manifest.permission.BLUETOOTH_ADMIN:
                                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_ADMIN_PERMISSION);
                                break;
                            case Manifest.permission.ACCESS_FINE_LOCATION:
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION_PERMISSION);
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.permission_ko, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        permissionFailure();
                    }
                })
                .create()
                .show();
    }

    private void permissionFailure() {
        Toast.makeText(this, R.string.toast_permission_abort, Toast.LENGTH_LONG).show();
        finish();
    }
}



