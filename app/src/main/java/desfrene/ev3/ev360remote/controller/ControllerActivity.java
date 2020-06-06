package desfrene.ev3.ev360remote.controller;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import desfrene.ev3.ev360remote.Constants;
import desfrene.ev3.ev360remote.R;
import desfrene.ev3.ev360remote.controller.customsViews.BasicTouchView;
import desfrene.ev3.ev360remote.controller.customsViews.JoystickView;
import desfrene.ev3.ev360remote.controller.customsViews.PowerView;
import desfrene.ev3.ev360remote.controller.customsViews.TouchButtonView;

import static desfrene.ev3.ev360remote.Constants.INFO_TAG;

public class ControllerActivity extends AppCompatActivity {

    JoystickView joystick;
    PowerView powerView;
    TouchButtonView turnRight;
    TouchButtonView turnLeft;

    private GestureDetectorCompat mDetector;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
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

    void setPower(int power) {
        Log.i(Constants.DATA_TAG, String.format("Power : %d", power));
    }

    void setDirection(int angle) {
        Log.i(Constants.DATA_TAG, String.format("Angle : %d°", angle));
    }

    void setTurningLeft() {
        powerView.setOperational(true);

        turnRight.setOperational(false);
        joystick.setOperational(false);

        Log.i(Constants.FCT_TAG, "TurningLeft");
        log();
    }

    void setTurningRight() {
        powerView.setOperational(true);

        turnLeft.setOperational(false);
        joystick.setOperational(false);

        Log.i(Constants.FCT_TAG, "TurningRight");
        log();
    }

    void resetWheels() {
        Log.i(Constants.FCT_TAG, "Reset Wheels positions");

        powerView.setOperational(false);

        joystick.setOperational(true);
        turnRight.setOperational(true);
        turnLeft.setOperational(true);

        log();
    }


    void startDirection() {

        powerView.setOperational(true);

        turnRight.setOperational(false);
        turnLeft.setOperational(false);

        Log.i(Constants.FCT_TAG, "Start Direction Track");
        log();
    }

    void resetDirection() {
        Log.i(Constants.FCT_TAG, "Stop Direction Track");

        powerView.setOperational(false);

        turnRight.setOperational(true);
        turnLeft.setOperational(true);
        joystick.setOperational(true);

        log();
    }


    void startPower() {
        joystick.setOperational(true);

        turnRight.setOperational(true);
        turnLeft.setOperational(true);

        Log.i(Constants.FCT_TAG, "Start Power Track");
        log();
    }

    void resetPower() {
        Log.i(Constants.FCT_TAG, "Stop Power Track");
        joystick.setOperational(false);

        turnRight.setOperational(true);
        turnLeft.setOperational(true);
        log();
    }


    void log() {
        Log.i(INFO_TAG, String.format("Joystick Tracking : %b", joystick.isOperational()));
        Log.i(INFO_TAG, String.format("Right Tracking : %b", joystick.isOperational()));
        Log.i(INFO_TAG, String.format("Left Tracking : %b", joystick.isOperational()));
        Log.i(INFO_TAG, String.format("Joystick : %b", joystick.isOperational()));
//        Log.i(TAG_2, String.format("PowerView : %b", powerView.isWaiting()));
//        Log.i(TAG_2, String.format("Left : %b", turnLeft.isOperational()));
//        Log.i(TAG_2, String.format("Right : %b", turnRight.isOperational()));
//
//        Log.i(TAG_2, String.format("Pressed Left : %b", turnLeft.isButtonPressed()));
//        Log.i(TAG_2, String.format("Pressed Right : %b", turnRight.isButtonPressed()));
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
        switch (item.getItemId()) {
            case R.id.disconnect:
                Log.i(Constants.FCT_TAG, "Disconnect From Server");
                return true;

            case R.id.info:
                Log.i(Constants.FCT_TAG, "Show Information");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.controller_menu, menu);
        return true;
    }

}
