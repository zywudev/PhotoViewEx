package com.wuzy.photoviewex;

import android.content.Context;
import android.view.MotionEvent;

/**
 * @author wuzy
 * @date 2019/4/29
 * @description
 */
public class RotateGestureDetector {

    public interface OnRotateGestureListener {

        void onRotate(int degrees, int pivotX, int pivotY);

        void onToRightAngle(int pivotX, int pivotY);

    }

    private final Context mContext;
    private final OnRotateGestureListener mListener;

    private int mLastAngle = 0;
    private boolean mIsRotate;

    public RotateGestureDetector(Context context, OnRotateGestureListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() != 2) {
            return false;
        }
        int pivotX = (int) (event.getX(0) + event.getX(1)) / 2;
        int pivotY = (int) (event.getY(0) + event.getY(1)) / 2;
        float deltaX = event.getX(0) - event.getX(1);
        float deltaY = event.getY(0) - event.getY(1);
        int degrees = (int) Math.round(Math.toDegrees(Math.atan2(deltaY, deltaX)));

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastAngle = degrees;
                mIsRotate = false;
                break;
            case MotionEvent.ACTION_UP:
                mIsRotate = false;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mLastAngle = degrees;
                mIsRotate = false;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                mIsRotate = false;
                toRightAngle(pivotX, pivotY);
                mLastAngle = degrees;
                break;
            case MotionEvent.ACTION_MOVE:
                mIsRotate = true;
                int degreesValue = degrees - mLastAngle;
                if (degreesValue > 45) {
                    rotate(-5, pivotX, pivotY);
                } else if (degreesValue < -45) {
                    rotate(5, pivotX, pivotY);
                } else {
                    rotate(degreesValue, pivotX, pivotY);
                }
                mLastAngle = degrees;
                break;
        }

        return true;
    }

    private void toRightAngle(int pivotX, int pivotY) {
        if (mListener != null) {
            mListener.onToRightAngle(pivotX, pivotY);
        }
    }

    private void rotate(int degrees, int pivotX, int pivotY) {
        if (mListener != null) {
            mListener.onRotate(degrees, pivotX, pivotY);
        }
    }

    public boolean isRotating() {
        return mIsRotate;
    }
}
