package com.github.xiaofei_dev.locksurface.backstage;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.github.xiaofei_dev.locksurface.R;
import com.github.xiaofei_dev.locksurface.ui.MainActivity;
import com.github.xiaofei_dev.locksurface.util.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Date;



public final class LockService extends Service {

    private FrameLayout iconFloatView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private boolean isAddView;
    public static final String TYPE = "TYPE";
    public static final String TAG = "LockService";
    public static final String TIME = "TIME";
    private String awakenTime;
    private long alarmTime;
    //private Handler mHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        //mHandler = new Handler();
        iconFloatView = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.floating_icon,null);
        iconFloatView.findViewById(R.id.lock_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showShort("此手机已被锁定，预计解锁时间 : \n" + awakenTime);

            }
        });

        mWindowManager = (WindowManager)(/*getApplicationContext().*/getSystemService(Context.WINDOW_SERVICE));

        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.gravity = Gravity.CENTER;
        //mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
                |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                |WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR;
                /*WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                |WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH*/
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if(intent == null){
            return super.onStartCommand(intent, flags, startId);
        }
        if(intent.getStringExtra(TYPE).equals(TAG)){
            //解锁界面，停止服务
            removeView();
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(this).edit();
            editor.putLong(TIME,-1);
            editor.apply();
            stopSelf();////
            return super.onStartCommand(intent, flags, startId);
        }else if(intent.getStringExtra(TYPE).equals(MainActivity.TAG)){
            //获取用户输入的时长，换算成毫秒
            int hours = intent.getIntExtra(MainActivity.HOUR,-1) * 60 * 60 * 1000;//把设置的小时换算成毫秒数
            int minutes = intent.getIntExtra(MainActivity.MINUTE,-1) * 60 * 1000;//把设置的分钟换算成毫秒数
            alarmTime = System.currentTimeMillis() + hours + minutes;

            //根据用户提交的时长格式化一个时刻
            formatTime(alarmTime);

            //开始定时任务
            startAlarmTask();
        }else if(intent.getStringExtra(TYPE).equals(MyReceiver.TAG)){
            alarmTime = PreferenceManager.getDefaultSharedPreferences(this).getLong(TIME,1000);

            //根据用户提交的时长格式化一个时刻
            formatTime(alarmTime);

            //开始定时任务
            startAlarmTask();
        }
        //如果设置了开机保持锁定，则把用户设置的时长保存到首选项
        saveTime(alarmTime);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        //
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private synchronized void addView(){
        if(!isAddView){
            mWindowManager.addView(iconFloatView,mLayoutParams);
            isAddView = true;
        }

    }

    private synchronized void removeView(){
        if(isAddView){
            mWindowManager.removeView(iconFloatView);
            isAddView = false;
        }
    }

    private void saveTime (Long time){
        //如果设置了开机保持锁定，则把用户设置的时长保存到首选项
        if(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(MainActivity.IS_CHECKED_BOOT,false)){
            SharedPreferences.Editor editor = PreferenceManager
                    .getDefaultSharedPreferences(this).edit();
            editor.putLong(TIME,time);
            editor.apply();
        }
    }

    private void formatTime(Long time){
        //根据用户提交的时长格式化一个时刻
        SimpleDateFormat format=new SimpleDateFormat("MM 月 dd 号 HH : mm : ss");
        Date date=new Date(time);
        awakenTime = format.format(date);
    }

    private void startAlarmTask(){
        //开始定时任务
        Intent i = new Intent(this,LockService.class);
        i.putExtra(TYPE,TAG);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);

        //设置定时任务，用户设置的时间到了开始服务解锁界面
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        manager.setExact(AlarmManager.RTC_WAKEUP,alarmTime,pi);

        addView();
    }
}
