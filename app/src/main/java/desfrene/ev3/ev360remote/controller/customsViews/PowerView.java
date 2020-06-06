package desfrene.ev3.ev360remote.controller.customsViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import desfrene.ev3.ev360remote.R;

import static desfrene.ev3.ev360remote.Constants.DATA_TAG;

public class PowerView extends View implements BasicTouchView, BasicView {

    //<editor-fold desc="Interfaces">
    public interface OnPowerChangeListener {
        void onPowerChange(int power);
    }

    private OnPowerChangeListener powerListener;
    private OnTrackBeginListener trackBeginListener;
    private OnTrackEndListener trackEndListener;

    public void setPowerChangeListener(OnPowerChangeListener powerListener) {
        this.powerListener = powerListener;
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

    //<editor-fold desc="Variables">
    private int m_normalColor;
    private int m_waitingColor;

    private int m_borderSize;
    private float m_widthRatio;

    private Paint m_backgroundPaint;
    private Paint m_borderPaint;

    private Rect m_backgroundRect;
    private Rect m_borderRect;

    private int m_touchPos;
    private int m_maxTop;
    private int m_minBottom;

    private boolean m_track;
    private boolean m_operational;
    //</editor-fold>

    //<editor-fold desc="Constructors">
    public PowerView(Context context) {
        super(context);
        init(null, 0);
    }

    public PowerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public PowerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PowerView, defStyle, 0);

        m_normalColor = a.getColor(R.styleable.PowerView_normalColor, Color.BLUE);
        m_waitingColor = a.getColor(R.styleable.PowerView_waitingColor, Color.RED);
        int border_color = a.getColor(R.styleable.PowerView_backgroundBorderColor, Color.GRAY);

        m_borderSize = a.getInteger(R.styleable.PowerView_borderSize, 5);
        m_widthRatio = a.getFloat(R.styleable.PowerView_widthRatio, 0.7f);

        a.recycle();

        m_backgroundPaint = new Paint();
        m_backgroundPaint.setAntiAlias(true);
        m_backgroundPaint.setColor(m_waitingColor);
        m_backgroundPaint.setStyle(Paint.Style.FILL);

        m_borderPaint = new Paint();
        m_borderPaint.setAntiAlias(true);
        m_borderPaint.setColor(border_color);
        m_borderPaint.setStyle(Paint.Style.STROKE);
        m_borderPaint.setStrokeWidth(m_borderSize);

        m_borderRect = new Rect();
        m_backgroundRect = new Rect();

        m_touchPos = 0;
        m_maxTop = 0;
        m_minBottom = 0;

        m_track = false;
        m_operational = false;

        updateSize(getWidth(), getHeight());
    }
    //</editor-fold>

    //<editor-fold desc="Compute Size">
    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);
        updateSize(w, h);
    }

    private void updateSize(int w, int h) {
        int paddingTop = getPaddingTop();

        int contentWidth = w - getPaddingLeft() - getPaddingRight();
        int contentHeight = h - getPaddingTop() - getPaddingBottom();

        int targetWidth = (int) (contentWidth * m_widthRatio);
        int leftSpace = (w - targetWidth) / 2;


        m_borderRect = new Rect(leftSpace, paddingTop, leftSpace + targetWidth,
                contentHeight + paddingTop);

        m_maxTop = m_borderRect.top + (m_borderSize / 2);
        m_minBottom = m_borderRect.bottom - (m_borderSize / 2);

        m_backgroundRect = new Rect(m_borderRect.left + (m_borderSize / 2), m_minBottom,
                m_borderRect.right - (m_borderSize / 2), m_minBottom);

        m_touchPos = m_minBottom;
    }
    //</editor-fold>

    //<editor-fold desc="Draw & Touch Functions">
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(m_borderRect, m_borderPaint);
        canvas.drawRect(m_backgroundRect, m_backgroundPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled()) {
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
        float Y = event.getY();
        int targetPos = (int) Y;

        if (Y <= m_maxTop)
            targetPos = m_maxTop;
        else if (Y >= m_minBottom)
            targetPos = m_minBottom;

        if (computePercent(targetPos) != getPercent()) {
            m_touchPos = targetPos;
            m_backgroundRect.top = targetPos;

            if (powerListener != null && isOperational()) {
                powerListener.onPowerChange(getPercent());
            }
        }
    }

    private int computePercent(int targetPos) {
        return (int) (100.0 * (float) ((float) targetPos - (float) m_minBottom) / (float) ((float) m_maxTop - (float) m_minBottom));
    }

    private void holdPress() {
        if (!isTracking()) {
            m_track = true;

            if (trackBeginListener != null) {
                trackBeginListener.onTrackBegin();
            }
        }
    }

    private void holdRelease() {
        m_touchPos = m_minBottom;
        m_backgroundRect.top = m_minBottom;

        if (isTracking()) {
            m_track = false;

            if (trackEndListener != null) {
                trackEndListener.onTrackEnd();
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Getter & Setters">

    public int getPercent() {
        return computePercent(m_touchPos);
    }

    public boolean isTracking() {
        return m_track;
    }

    public void setNormalColor(int backgroundColor) {
        m_normalColor = backgroundColor;
    }

    public void setWaitingColor(int waitingColor) {
        m_waitingColor = waitingColor;
    }

    public boolean isOperational() {
        return m_operational;
    }

    public void setOperational(boolean operational) {
        if (operational != isOperational()) {
            m_operational = operational;

            if (operational)
                m_backgroundPaint.setColor(m_normalColor);
            else
                m_backgroundPaint.setColor(m_waitingColor);

            invalidate();
        }
    }
    //</editor-fold>
}
