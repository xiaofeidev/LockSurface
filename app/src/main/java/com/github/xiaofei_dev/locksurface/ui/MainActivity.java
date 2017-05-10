package com.github.xiaofei_dev.locksurface.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.github.xiaofei_dev.locksurface.backstage.LockService;
import com.github.xiaofei_dev.locksurface.R;
import com.github.xiaofei_dev.locksurface.util.ToastUtils;

public final class MainActivity extends AppCompatActivity {

    private EditText mHour;
    private EditText mMinute;
    private final String HOUR_MAXVALUE = "24";
    private final String MINUTE_MAXVALUE = "86400";

    public static final String TAG = "MainActivity";
    public static final String HOUR = "HOUR";
    public static final String MINUTE = "MINUTE";
    public static final String IS_CHECKED_BOOT = "IS_CHECKED_BOOT";

    private boolean isCheckedBoot;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    //private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHour = (EditText) findViewById(R.id.hour);
        mMinute = (EditText) findViewById(R.id.minute);

        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        setIsCheckedBoot();
        //mHandler = new Handler();
    }

    private void setIsCheckedBoot(){
        isCheckedBoot= sharedPreferences.getBoolean(IS_CHECKED_BOOT,false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this,LockService.class));
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        startActivity(intent);
        moveTaskToBack(true);
    }

    //按钮点击方法
    public void onClick(View v){
        switch (v.getId()){
            case R.id.negative:
                moveTaskToBack(true);
                //finish();
                break;
            case R.id.positive:
                int intHour;
                if(mHour.length() != 0){
                    String hour = mHour.getText().toString();
                    intHour = Integer.valueOf(hour);
                }else {
                    intHour = 0;
                }
                if(intHour > Integer.valueOf(HOUR_MAXVALUE)){
                    ToastUtils.showShort(R.string.error_hour);
                    return;
                }


                int intMinute;
                if(mMinute .length() != 0){
                    String minute = mMinute.getText().toString();
                    intMinute = Integer.valueOf(minute);
                }else {
                    intMinute = 0;
                }
                if(intMinute > Integer.valueOf(MINUTE_MAXVALUE)){
                    ToastUtils.showShort(R.string.error_minute);
                    return;
                }
                if(intHour == 0 && intMinute == 0){
                    ToastUtils.showShort(R.string.error_zero);
                    return;
                }

                final Intent intent = new Intent(MainActivity.this,LockService.class);
                intent.putExtra(HOUR,intHour);
                intent.putExtra(MINUTE,intMinute);
                intent.putExtra(LockService.TYPE,TAG);

                AlertDialog alertDialog = new AlertDialog.Builder(this,R.style.Dialog)
                        .setTitle(R.string.alert)
                        .setMessage(R.string.alert_hint)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        startService(intent);
                                        dialog.cancel();
                                        //模拟点击 Home 键
                                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                                        startActivity(homeIntent);
                                    }
                                }).setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).create();
                alertDialog.show();

//                startService(intent);
//                //模拟点击 Home 键
//                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
//                homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                homeIntent.addCategory(Intent.CATEGORY_HOME);
//                startActivity(homeIntent);
                break;
            case R.id.setting:
                AlertDialog settingDialog = new AlertDialog.Builder(this,R.style.Dialog)
                        .setTitle(R.string.setting)
                        .setPositiveButton(getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }).create();
                settingDialog.setView(getSettingView());
                settingDialog.show();
//                Intent aboutIntent = new Intent(this,AboutActivity.class);
//                startActivity(aboutIntent);
                break;
            default:
                break;
        }
    }

    private View getSettingView(){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_setting, null);
        CheckBox checkBoxBoot = (CheckBox)view.findViewById(R.id.checkBoxBoot);
        checkBoxBoot.setChecked(isCheckedBoot);
        return view;
    }

    //设置对话框按钮方法
    public void onSetting(View view){
        switch (view.getId()){
            case R.id.checkBoxBoot:
                boolean checkedBoot= ((CheckBox) view).isChecked();
                if(checkedBoot){
                    ToastUtils.showLong(R.string.check_boot_alarm);
                }
                editor.putBoolean(IS_CHECKED_BOOT,checkedBoot);
                editor.apply();
                setIsCheckedBoot();
                break;
            case R.id.about:
                Intent intent = new Intent(this,AboutActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
