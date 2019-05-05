package com.wuzy.photoviewex;

/**
 * @author wuzy
 * @date 2019/4/30
 * @description
 */
public interface OnDragCloseListener {

    void onDragBegin();

    void onDragging(float percent);

    void onDragEnd(boolean isShareElementMode);

    void onDragCancel();

    boolean intercept();

}
