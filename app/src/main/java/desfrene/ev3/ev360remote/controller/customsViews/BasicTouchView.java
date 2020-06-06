package desfrene.ev3.ev360remote.controller.customsViews;

import android.annotation.SuppressLint;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

public interface BasicTouchView {

    interface OnTrackBeginListener {
        void onTrackBegin();
    }

    interface OnTrackEndListener {
        void onTrackEnd();
    }

    @SuppressLint("ClickableViewAccessibility")
    boolean onTouchEvent(@NonNull MotionEvent event);

    void setTrackBeginListener(OnTrackBeginListener trackBeginListener);

    void setTrackEndListener(OnTrackEndListener trackEndListener);

    boolean isTracking();
}