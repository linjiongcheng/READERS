package com.example.john.readers.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.example.john.readers.R;

// 自定义播放暂停按钮组件

public class ChangeButton extends LinearLayout {
    /**
     * 命名空间
     */
    private static String NAMESPACE = "http://schemas.android.com/apk/res/com.cmb.music";
    /**
     * 存放第一张图片在R文件中的Int值，默认为0
     */
    private int startImage = 0;
    /**
     * 存放第二张图片在R文件中的Int值，默认为0
     */
    private int stopImage = 0;
    /**
     * 判断处于哪个状态，默认为true
     */
    private static boolean isStart = true;

    /**
     * 构造函数1
     * @param context
     */
    public ChangeButton(Context context) {
        super(context);
        initView();
    }

    /**
     * 构造函数2
     * @param context
     */
    public ChangeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 构造函数2
     * @param context
     */
    public ChangeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 获取图片的Int值
        startImage = attrs.getAttributeResourceValue(NAMESPACE, "start", R.drawable.start);
        stopImage = attrs.getAttributeResourceValue(NAMESPACE, "stop", R.drawable.stop);
        initView();
    }

    public boolean isStart(){
        return isStart;
    }
    /**
     * 初始化函数
     */
    private void initView(){
        View.inflate(getContext(), R.layout.control_button, this);
        this.setClickable(true);
        if(stopImage == 0){
            return ;
        }else{
            setIsStart(isStart);
        }
    }
    /**
     * 通过传进一个boolean值，设置按钮的状态
     * @param isStart 设置开始或者暂停的状态
     */
    public void setIsStart(boolean isStart){
        this.isStart = isStart;
        if(isStart){
            this.setBackgroundResource(startImage);
        }
        else{
            this.setBackgroundResource(stopImage);
        }
    }
}
