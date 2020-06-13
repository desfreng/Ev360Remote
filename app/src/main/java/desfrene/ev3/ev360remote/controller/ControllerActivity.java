package desfrene.ev3.ev360remote.controller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import desfrene.ev3.ev360remote.Constants;
import desfrene.ev3.ev360remote.R;
import desfrene.ev3.ev360remote.controller.customsViews.BasicTouchView;
import desfrene.ev3.ev360remote.controller.customsViews.JoystickView;
import desfrene.ev3.ev360remote.controller.customsViews.PowerView;
import desfrene.ev3.ev360remote.controller.customsViews.TouchButtonView;

import static desfrene.ev3.ev360remote.Constants.CONNECTION_CLOSED;
import static desfrene.ev3.ev360remote.Constants.CONNECTION_FAILED;
import static desfrene.ev3.ev360remote.Constants.CONNECTION_LOST;
import static desfrene.ev3.ev360remote.Constants.TARGET_CHANNEL;
import static desfrene.ev3.ev360remote.Constants.TARGET_DEVICE;

public class ControllerActivity extends AppCompatActivity {

    JoystickView joystick;
    PowerView powerView;
    TouchButtonView turnRight;
    TouchButtonView turnLeft;

    BluetoothPipe pipe;

    private GestureDetectorCompat mDetector;
    BluetoothDevice dev;
    boolean connect;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        String address = getIntent().getStringExtra(TARGET_DEVICE);
        int channel = getIntent().getIntExtra(TARGET_CHANNEL, 1);
        dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);

        pipe = new BluetoothPipe(mHandler);
        pipe.connect(dev, channel);

        mDetector = new GestureDetectorCompat(this, new MyGestureListener());
        hideSystemUI();

        joystick = findViewById(R.id.joystickTest);
        powerView = findViewById(R.id.powerView);

        turnRight = findViewById(R.id.turn_right);
        turnLeft = findViewById(R.id.turn_left);

        joystick.setDirectionListener(new JoystickView.OnDirectionChangedListener() {
            @Override
            public void onDirectionChange(int angle) {
                setDirection(angle);
            }
        });
        joystick.setTrackBeginListener(new BasicTouchView.OnTrackBeginListener() {
            @Override
            public void onTrackBegin() {
                startDirection();
            }
        });
        joystick.setTrackEndListener(new BasicTouchView.OnTrackEndListener() {
            @Override
            public void onTrackEnd() {
                resetDirection();
            }
        });

        powerView.setPowerChangeListener(new PowerView.OnPowerChangeListener() {
            @Override
            public void onPowerChange(int power) {
                setPower(power);
            }
        });
        powerView.setTrackBeginListener(new BasicTouchView.OnTrackBeginListener() {
            @Override
            public void onTrackBegin() {
                startPower();
            }
        });
        powerView.setTrackEndListener(new BasicTouchView.OnTrackEndListener() {
            @Override
            public void onTrackEnd() {
                resetPower();
            }
        });

        turnLeft.setPressListener(new TouchButtonView.OnPressListener() {
            @Override
            public void onPress() {
                setTurningLeft();
            }
        });
        turnLeft.setReleaseListener(new TouchButtonView.OnReleaseListener() {
            @Override
            public void onRelease() {
                resetWheels();
            }
        });

        turnRight.setPressListener(new TouchButtonView.OnPressListener() {
            @Override
            public void onPress() {
                setTurningRight();
            }
        });
        turnRight.setReleaseListener(new TouchButtonView.OnReleaseListener() {
            @Override
            public void onRelease() {
                resetWheels();
            }
        });

        powerView.setOperational(false);
        joystick.setOperational(false);

        turnRight.setOperational(true);
        turnLeft.setOperational(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @SuppressLint("DefaultLocale")
    void setPower(int power) {
        Log.i(Constants.DATA_TAG, String.format("Power : %d", power));
        pipe.write(String.format("PO#%d", power).getBytes());
    }

    @SuppressLint("DefaultLocale")
    void setDirection(int angle) {
        Log.i(Constants.DATA_TAG, String.format("Angle : %d°", angle));
        pipe.write(String.format("DI#%d", angle).getBytes());
    }

    @SuppressLint("DefaultLocale")
    void setTurningLeft() {
        powerView.setOperational(true);

        turnRight.setOperational(false);
        joystick.setOperational(false);

        Log.i(Constants.FCT_TAG, "TurningLeft");
        pipe.write("TL#".getBytes());
        sendPower();
    }

    @SuppressLint("DefaultLocale")
    void setTurningRight() {
        powerView.setOperational(true);

        turnLeft.setOperational(false);
        joystick.setOperational(false);

        Log.i(Constants.FCT_TAG, "TurningRight");
        pipe.write("TR#".getBytes());
        sendPower();
    }

    void resetWheels() {
        Log.i(Constants.FCT_TAG, "Reset Wheels positions");

        powerView.setOperational(false);

        joystick.setOperational(true);
        turnRight.setOperational(true);
        turnLeft.setOperational(true);

        pipe.write("RW#".getBytes());
        sendNullPower();
    }


    @SuppressLint("DefaultLocale")
    void startDirection() {

        powerView.setOperational(true);

        turnRight.setOperational(false);
        turnLeft.setOperational(false);

        Log.i(Constants.FCT_TAG, "Start Direction Track");
        sendPower();
    }

    void resetDirection() {
        Log.i(Constants.FCT_TAG, "Stop Direction Track");

        powerView.setOperational(false);

        turnRight.setOperational(true);
        turnLeft.setOperational(true);
        joystick.setOperational(true);

        sendNullPower();
    }


    void startPower() {
        joystick.setOperational(true);

        turnRight.setOperational(true);
        turnLeft.setOperational(true);

        Log.i(Constants.FCT_TAG, "Start Power Track");
    }

    void resetPower() {
        Log.i(Constants.FCT_TAG, "Stop Power Track");
        joystick.setOperational(false);

        turnRight.setOperational(true);
        turnLeft.setOperational(true);

        sendNullPower();
    }

    @SuppressLint("DefaultLocale")
    void sendPower() {
        pipe.write(String.format("PO#%d", powerView.getPercent()).getBytes());
    }

    @SuppressLint("DefaultLocale")
    void sendNullPower() {
        pipe.write("PO#0".getBytes());
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (velocityY > 1000) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            } else if (velocityY < -1000) {
                hideSystemUI();
            }
            return true;
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.disconnect) {
            Log.i(Constants.FCT_TAG, "Disconnect From Server");
            pipe.stop();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.controller_menu, menu);
        return true;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), R.string.connection_lost, Toast.LENGTH_SHORT).show();
                    setResult(CONNECTION_LOST);
                    finish();
                    break;
                case CONNECTION_FAILED:
                    Toast.makeText(getApplicationContext(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                    setResult(CONNECTION_FAILED);
                    finish();
                    break;
                case Constants.CONNECTED:
                    String deviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), getString(R.string.connected, deviceName), Toast.LENGTH_SHORT).show();
                    connect = true;
                    break;
                case Constants.CONNECTION_CLOSED:
                    Toast.makeText(getApplicationContext(), R.string.connection_closed, Toast.LENGTH_SHORT).show();
                    setResult(CONNECTION_CLOSED);
                    finish();
                    break;
            }
        }
    };

}
