package desfrene.ev3.ev360remote.controller.customsViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import desfrene.ev3.ev360remote.R;

import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public class JoystickView extends View implements BasicView, BasicTouchView {

    //<editor-fold desc="Interfaces">
    public interface OnDirectionChangedListener {
        void onDirectionChange(int angle);
    }

    private OnDirectionChangedListener moveListener;
    private OnTrackBeginListener trackBeginListener;
    private OnTrackEndListener trackEndListener;

    public void setDirectionListener(OnDirectionChangedListener moveListener) {
        this.moveListener = moveListener;
    }

    @Override
    public void setTrackBeginListener(OnTrackBeginListener trackBeginListener) {
        this.trackBeginListener = trackBeginListener;
    }

    @Override
    public void setTrackEndListener(OnTrackEndListener trackEndListener) {
        this.trackEndListener = trackEndListener;
    }
    //</editor-fold>

    //<editor-fold desc="Privates Variables">
    private float m_backgroundRatio;
    private float m_moverRatio;

    private Paint m_paintBackground;
    private Paint m_paintMover;

    private Point m_center;
    private Point m_touchPoint;

    private int m_backgroundRadius;
    private int m_moverRadius;
    private int m_maxRadius;

    private boolean m_track;
    private boolean m_operational;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public JoystickView(Context context) {
        super(context);
        init(null, 0);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        moveListener = null;
        trackBeginListener = null;
        trackEndListener = null;

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.JoystickView, defStyle, 0);

        m_backgroundRatio = a.getFloat(R.styleable.JoystickView_backgroundRatio, 0.85f);
        m_moverRatio = a.getFloat(R.styleable.JoystickView_moverRatio, 0.3f);


        m_paintBackground = new Paint();
        m_paintBackground.setAntiAlias(true);
        m_paintBackground.setColor(a.getColor(R.styleable.JoystickView_backgroundColor, Color.TRANSPARENT));
        m_paintBackground.setStyle(Paint.Style.FILL);

        m_paintMover = new Paint();
        m_paintMover.setAntiAlias(true);
        m_paintMover.setColor(a.getColor(R.styleable.JoystickView_moverColor, Color.GRAY));
        m_paintMover.setStyle(Paint.Style.FILL);

        a.recycle();

        m_center = new Point();
        m_touchPoint = new Point();

        m_backgroundRadius = 0;
        m_moverRadius = 0;
        m_maxRadius = 0;

        m_track = false;
        m_operational = true;

        updatePosition(getWidth(), getHeight());
    }
    //</editor-fold>

    //<editor-fold desc="Compute Size Functions">
    private void updatePosition(int w, int h) {
        m_center.x = w / 2;
        m_center.y = h / 2;

        m_touchPoint = m_center;

        float contentRadius = (float) (Math.min(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom()) / 2.0);

        m_backgroundRadius = (int) (contentRadius * m_backgroundRatio);
        m_moverRadius = (int) (contentRadius * m_moverRatio);

        m_maxRadius = (int) (contentRadius - m_moverRadius);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        updatePosition(w, h);
    }
    //</editor-fold>

    //<editor-fold desc="Draw & Touch Functions">
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(m_center.x, m_center.y, m_backgroundRadius, m_paintBackground);
        canvas.drawCircle((float) (cos(toRadians(getAngle())) * getPower() * m_maxRadius / 100) + m_center.x,
                (float) (-sin(toRadians(getAngle())) * getPower() * m_maxRadius / 100) + m_center.y, m_moverRadius, m_paintMover);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled() || !isOperational()) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            holdRelease();
        } else {
            holdPress();
            holdMove(event);
        }

        invalidate();
        return true;
    }

    private void holdMove(@NonNull MotionEvent event) {
        Point touchLocation = new Point((int) event.getX(), (int) event.getY());

        if (computeAngle(touchLocation) != getAngle()) {
            m_touchPoint = touchLocation;

            if (moveListener != null)
                moveListener.onDirectionChange(getAngle());
        }
    }

    private void holdPress() {
        if (!isTracking() && isOperational()) {
            m_track = true;

            if (trackBeginListener != null) {
                trackBeginListener.onTrackBegin();
            }
        }
    }

    private void holdRelease() {
        m_touchPoint = m_center;

        if (isTracking() && isOperational()) {
            m_track = false;

            if (trackEndListener != null) {
                trackEndListener.onTrackEnd();
            }
        }
    }

    private int computeAngle(Point touchPoint) {
        Point delta = new Point(touchPoint.x - m_center.x, touchPoint.y - m_center.y);
        double tan = toDegrees(atan2(-delta.y, delta.x));
        return (int) (tan > 0 ? tan : tan + 360);
    }
    //</editor-fold>

    //<editor-fold desc="Getter & Setters">
    public int getAngle() {
        return computeAngle(m_touchPoint);
    }

    public int getPower() {
        Point delta = new Point(m_touchPoint.x - m_center.x, m_touchPoint.y - m_center.y);
        int distance = (int) sqrt(delta.x * delta.x + delta.y * delta.y);
        return (100 * Math.min(distance, m_maxRadius)) / m_maxRadius;
    }

    @Override
    public boolean isOperational() {
        return m_operational;
    }

    @Override
    public void setOperational(boolean operational) {
        if (operational != this.m_operational) {
            if (isTracking() && !operational) {
                holdRelease();
                invalidate();
            }
            this.m_operational = operational;
        }
    }

    @Override
    public boolean isTracking() {
        return m_track;
    }
    //</editor-fold>
}
