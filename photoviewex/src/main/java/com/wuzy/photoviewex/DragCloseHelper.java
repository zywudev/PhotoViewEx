package com.wuzy.photoviewex;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.LinearInterpolator;

import androidx.annotation.FloatRange;

/**
 * @author wuzy
 * @date 2019/4/30
 * @description 拖拽图片关闭类
 */
public class DragCloseHelper {

    private final static long DURATION = 100;
    private static final float MIN_SCALE = 0.4F;
    private final static int MAX_EXIT_Y = 500;

    private float mMinScale = MIN_SCALE;
    private int mMaxExitY = MAX_EXIT_Y;
    private ViewConfiguration mViewConfiguration;
    private boolean mIsSwipeToClose;
    private float mLastX, mLastRawY, mLastY, mLastRawX;
    private int mLastPointerId;
    private float mCurrentTranslationY, mCurrentTranslationX;
    private float mLastTranslationY, mLastTranslationX;
    private boolean mIsResetingAnimate = false;
    private boolean mIsShareElementMode = false;

    private View mParentView, mChildView;

    private OnDragCloseListener mOnDragCloseListener;
    private Context mContext;

    public DragCloseHelper(Context context) {
        this.mContext = context;
        mViewConfiguration = ViewConfiguration.get(context);
    }

    public void setOnDragCloseListener(OnDragCloseListener onDragCloseListener) {
        mOnDragCloseListener = onDragCloseListener;
    }

    /**
     * 设置拖拽关闭的view
     *
     * @param parentV
     * @param childV
     */
    public void setDragCloseViews(View parentV, View childV) {
        this.mParentView = parentV;
        this.mChildView = childV;
    }

    /**
     * 设置最大退出距离
     *
     * @param maxExitY
     */
    public void setMaxExitY(int maxExitY) {
        this.mMaxExitY = maxExitY;
    }

    /**
     * 设置最小缩放尺寸
     *
     * @param minScale
     */
    public void setMinScale(@FloatRange(from = 0.1f, to = 1.0f) float minScale) {
        this.mMinScale = minScale;
    }

    public void setShareElementMode(boolean shareElementMode) {
        mIsShareElementMode = shareElementMode;
    }

    public boolean handleEvent(MotionEvent event) {
        if (mOnDragCloseListener != null && mOnDragCloseListener.intercept()) {
            //拦截
            mIsSwipeToClose = false;
            return false;
        } else {
            //不拦截
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //初始化数据
                mLastPointerId = event.getPointerId(0);
                reset(event);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (event.getPointerCount() > 1) {
                    //如果有多个手指
                    if (mIsSwipeToClose) {
                        //已经开始滑动关闭，恢复原状，否则需要派发事件
                        mIsSwipeToClose = false;
                        resetCallBackAnimation();
                        return true;
                    }
                    reset(event);
                    return false;
                }
                if (mLastPointerId != event.getPointerId(0)) {
                    //手指不一致，恢复原状
                    if (mIsSwipeToClose) {
                        resetCallBackAnimation();
                    }
                    reset(event);
                    return true;
                }
                float currentY = event.getY();
                float currentX = event.getX();
                if (mIsSwipeToClose || Math.abs(currentY - mLastY) > 2 * mViewConfiguration.getScaledTouchSlop()) {
                    //已经触发或者开始触发，更新view
                    mLastY = currentY;
                    mLastX = currentX;
                    float currentRawY = event.getRawY();
                    float currentRawX = event.getRawX();
                    if (!mIsSwipeToClose) {
                        //准备开始
                        mIsSwipeToClose = true;
                        if (mOnDragCloseListener != null) {
                            mOnDragCloseListener.onDragBegin();
                        }
                    }
                    //已经开始，更新view
                    mCurrentTranslationY = currentRawY - mLastRawY + mLastTranslationY;
                    mCurrentTranslationX = currentRawX - mLastRawX + mLastTranslationX;
                    float percent = 1 - Math.abs(mCurrentTranslationY / (mMaxExitY + mChildView.getHeight()));
                    if (percent > 1) {
                        percent = 1;
                    } else if (percent < 0) {
                        percent = 0;
                    }
                    mParentView.getBackground().mutate().setAlpha((int) (percent * 255));
                    if (mOnDragCloseListener != null) {
                        mOnDragCloseListener.onDragging(percent);
                    }
                    mChildView.setTranslationY(mCurrentTranslationY);
                    mChildView.setTranslationX(mCurrentTranslationX);
                    if (percent < mMinScale) {
                        percent = mMinScale;
                    }
                    mChildView.setScaleX(percent);
                    mChildView.setScaleY(percent);
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                //手指抬起事件
                if (mIsSwipeToClose) {
                    if (mCurrentTranslationY > mMaxExitY) {
                        if (mIsShareElementMode) {
                            //会执行共享元素的离开动画
                            if (mOnDragCloseListener != null) {
                                mOnDragCloseListener.onDragEnd(true);
                            }
                        } else {
                            //会执行定制的离开动画
                            exitWithTranslation(mCurrentTranslationY);
                        }
                    } else {
                        resetCallBackAnimation();
                    }
                    mIsSwipeToClose = false;
                    return true;
                }
            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                //取消事件
                if (mIsSwipeToClose) {
                    resetCallBackAnimation();
                    mIsSwipeToClose = false;
                    return true;
                }
            }
        }
        return false;
    }


    private void reset(MotionEvent event) {
        mIsSwipeToClose = false;
        mLastY = event.getY();
        mLastX = event.getX();
        mLastRawY = event.getRawY();
        mLastRawX = event.getRawX();
        mLastTranslationY = 0;
        mLastTranslationX = 0;
    }

    /**
     * 更新缩放的view
     */
    private void updateChildView(float transX, float transY) {
        mChildView.setTranslationY(transY);
        mChildView.setTranslationX(transX);
        float percent = Math.abs(transY / (mMaxExitY + mChildView.getHeight()));
        float scale = 1 - percent;
        if (scale < mMinScale) {
            scale = mMinScale;
        }
        mChildView.setScaleX(scale);
        mChildView.setScaleY(scale);
    }

    /**
     * 恢复到原位动画
     */
    private void resetCallBackAnimation() {
        if (mIsResetingAnimate || mCurrentTranslationY == 0) {
            return;
        }
        final float ratio = mCurrentTranslationX / mCurrentTranslationY;
        ValueAnimator animatorY = ValueAnimator.ofFloat(mCurrentTranslationY, 0);
        animatorY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mIsResetingAnimate) {
                    mCurrentTranslationY = (float) animation.getAnimatedValue();
                    mCurrentTranslationX = ratio * mCurrentTranslationY;
                    mLastTranslationY = mCurrentTranslationY;
                    mLastTranslationX = mCurrentTranslationX;
                    updateChildView(mLastTranslationX, mCurrentTranslationY);
                }
            }
        });
        animatorY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mIsResetingAnimate = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mIsResetingAnimate) {
                    mParentView.getBackground().mutate().setAlpha(255);
                    mCurrentTranslationY = 0;
                    mCurrentTranslationX = 0;
                    mIsResetingAnimate = false;
                    if (mOnDragCloseListener != null) {
                        mOnDragCloseListener.onDragCancel();
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorY.setDuration(DURATION).start();
    }

    public void exitWithTranslation(float currentY) {
        int targetValue = currentY > 0 ? mChildView.getHeight() : -mChildView.getHeight();
        ValueAnimator anim = ValueAnimator.ofFloat(mCurrentTranslationY, targetValue);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateChildView(mCurrentTranslationX, (float) animation.getAnimatedValue());
            }
        });
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mOnDragCloseListener != null) {
                    mOnDragCloseListener.onDragEnd(false);
                }
                ((Activity) mContext).finish();
                ((Activity) mContext).overridePendingTransition(R.anim.dchlib_anim_empty, R.anim.dchlib_anim_alpha_out_long_time);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(DURATION);
        anim.setInterpolator(new LinearInterpolator());
        anim.start();
    }

}

