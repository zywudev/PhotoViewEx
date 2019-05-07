# PhotoViewEx

基于 [PhotoView](https://github.com/chrisbanes/PhotoView)，增加手势旋转、拖动退出预览功能。

[![](https://jitpack.io/v/zywudev/PhotoViewEx.svg)](https://jitpack.io/#zywudev/PhotoViewEx)


## 效果

![](https://github.com/zywudev/PhotoViewEx/blob/master/screenshoots/1.gif)

## 依赖

在项目 `build.gradle` 中添加依赖

```groovy
allprojects {
	repositories {
        maven { url "https://jitpack.io" }
    }
}
```

module 的 `build.gradle` 中添加依赖

```groovy
dependencies {
    implementation 'com.github.zywudev:PhotoViewEx:1.0.0'
}
```

## 使用方法

使用下面的方式即可实现缩放、双指旋转功能。

```xml
<com.wuzy.photoviewex.PhotoView
    android:id="@+id/iv_photo"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

```java
PhotoView photoView = (PhotoView) findViewById(R.id.iv_photo);
photoView.setImageResource(R.drawable.image);
```

**拖动关闭使用方法**：

1、Activity 主题设为透明

```xml
<item name="android:windowIsTranslucent">true</item>
```

2、初始化

```java
DragCloseHelper mDragCloseHelper = new DragCloseHelper(this);
```

3、设置需要拖拽的 View 以及背景 ViewGroup

```java
mDragCloseHelper.setDragCloseViews(mConstraintLayout,mPhotoView);
```

4、设置监听

```java
mDragCloseHelper.setOnDragCloseListener(new OnDragCloseListener() {
    @Override
    public void onDragBegin() {

    }

    @Override
    public void onDragging(float percent) {

    }

    @Override
    public void onDragEnd(boolean isShareElementMode) {
        if (isShareElementMode) {
            onBackPressed();
        }
    }

    @Override
    public void onDragCancel() {

    }

    @Override
    public boolean intercept() {
        // 默认false
        return false;
    }
});
```

5、处理 Touch 事件

```java
@Override
public boolean dispatchTouchEvent(MotionEvent ev) {
    if (mDragCloseHelper.handleEvent(ev)) {
        return true;
    } else {
        return super.dispatchTouchEvent(ev);
    }
}
```

更多使用方法参见 [sample](https://github.com/zywudev/PhotoViewEx/tree/master/sample)。

## 参考

- [PhotoView](https://github.com/chrisbanes/PhotoView)

- [RotatePhotoView](https://github.com/ChenSiLiang/RotatePhotoView)
- [DragCloseHelper](https://github.com/bauer-bao/DragCloseHelper)