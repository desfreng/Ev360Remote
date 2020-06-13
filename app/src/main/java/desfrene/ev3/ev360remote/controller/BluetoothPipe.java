package desfrene.ev3.ev360remote.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static desfrene.ev3.ev360remote.Constants.BLE_TAG;
import static desfrene.ev3.ev360remote.Constants.CONNECTED;
import static desfrene.ev3.ev360remote.Constants.CONNECTION_CLOSED;
import static desfrene.ev3.ev360remote.Constants.CONNECTION_FAILED;
import static desfrene.ev3.ev360remote.Constants.CONNECTION_LOST;
import static desfrene.ev3.ev360remote.Constants.DEVICE_NAME;

public class BluetoothPipe {
    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public BluetoothPipe(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;

        mConnectThread = null;
        mConnectedThread = null;
    }

    public synchronized void connect(BluetoothDevice device, int channel) {
        Log.d(BLE_TAG, "connect to: " + device + "@" + channel);

        stop();

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, channel);
        mConnectThread.start();
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        stop();
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(CONNECTED);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    public synchronized void stop() {
        Log.d(BLE_TAG, "stop");

        // Cancel any thread currently running a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mConnectedThread != null)
                r = mConnectedThread;
            else
                return;
        }

        r.write(out);
    }

    private void connectionFailed() {
        // Send a failure message back to the Activity
        mHandler.obtainMessage(CONNECTION_FAILED).sendToTarget();
    }

    private void connectionLost() {
        // Send a failure message back to the Activity
        mHandler.obtainMessage(CONNECTION_LOST).sendToTarget();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device, int channel) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                tmp = (BluetoothSocket) m.invoke(device, channel);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                Log.e(BLE_TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(BLE_TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(BLE_TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(BLE_TAG, "close() of connected socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean connected;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(BLE_TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            connected = true;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(BLE_TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(BLE_TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];

            // Keep listening to the InputStream while connected
            while (connected) {
                try {
                    // Read from the InputStream
                    int read = mmInStream.read(buffer);

                    String str = new String(buffer, StandardCharsets.US_ASCII);

                    if (read > 0 && str.contains("ST")) {
                        cancel();
                    } else {
                        Log.w(BLE_TAG, String.format("Passing over message : %s", str));
                    }
                } catch (IOException e) {
                    Log.e(BLE_TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(BLE_TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                write("ST".getBytes());
                mmSocket.close();
                mHandler.obtainMessage(CONNECTION_CLOSED).sendToTarget();
            } catch (IOException e) {
                Log.e(BLE_TAG, "close() of socket failed", e);
            } finally {
                connected = false;
            }
        }
    }
}
