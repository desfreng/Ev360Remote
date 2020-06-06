package desfrene.ev3.ev360remote.controller.customsViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;

import desfrene.ev3.ev360remote.R;

public class TouchButtonView extends AppCompatButton implements BasicView {

    //<editor-fold desc="Interfaces">
    public interface OnPressListener {
        void onPress();
    }

    public interface OnReleaseListener {
        void onRelease();
    }

    OnPressListener pressListener;
    OnReleaseListener releaseListener;

    public void setPressListener(OnPressListener pressListener) {
        this.pressListener = pressListener;
    }

    public void setReleaseListener(OnReleaseListener releaseListener) {
        this.releaseListener = releaseListener;
    }
    //</editor-fold>

    //<editor-fold desc="Privates Variables">
    private int m_colorOnPress;

    private boolean m_pressed;
    private boolean m_operational;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public TouchButtonView(Context context) {
        super(context);
        init(null, 0);
    }

    public TouchButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TouchButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TouchButtonView, defStyle, 0);
        this.m_colorOnPress = a.getColor(R.styleable.TouchButtonView_colorOnPress, Color.RED);
        a.recycle();

        this.pressListener = null;
        this.releaseListener = null;

        m_pressed = false;
        m_operational = true;
    }
    //</editor-fold>

    //<editor-fold desc="Events Override">
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled() || !isOperational())
            return true;

        if (event.getAction() == MotionEvent.ACTION_DOWN && isButtonReleased()) {
            m_pressed = true;
            holdPress();
        } else if (event.getAction() == MotionEvent.ACTION_UP && isButtonPressed()) {
            m_pressed = false;
            holdRelease();
        }
        return true;
    }

    private void holdPress() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getBackground().setColorFilter(new BlendModeColorFilter(m_colorOnPress, BlendMode.SRC_ATOP));
        } else {
            getBackground().setColorFilter(m_colorOnPress, PorterDuff.Mode.SRC_ATOP);
        }

        if (pressListener != null) {
            pressListener.onPress();
        }
    }

    private void holdRelease() {
        getBackground().clearColorFilter();

        if (releaseListener != null) {
            releaseListener.onRelease();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Getters & Setters">
    public boolean isButtonPressed() {
        return m_pressed;
    }

    public boolean isButtonReleased() {
        return !isButtonPressed();
    }

    public void setOperational(boolean operational) {
        if (operational != this.m_operational) {
            this.m_operational = operational;

            if (!operational && m_pressed) {
                holdRelease();
            }
        }
    }

    public boolean isOperational() {
        return m_operational;
    }

    public int getColorOnPress() {
        return m_colorOnPress;
    }

    public void setColorOnPress(int m_colorOnPress) {
        this.m_colorOnPress = m_colorOnPress;
    }

    //</editor-fold>
}
