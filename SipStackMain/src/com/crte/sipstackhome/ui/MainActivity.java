package com.crte.sipstackhome.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.exception.SameThreadException;
import com.crte.sipstackhome.pjsip.PjSipAccount;
import com.crte.sipstackhome.pjsip.UserAgentBroadcastReceiver;
import com.crte.sipstackhome.ui.login.LoginActivity;
import com.crte.sipstackhome.ui.preferences.MainPrefsActivity;
import com.crte.sipstackhome.ui.suspensionwindow.SuspensionWindowService;
import com.crte.sipstackhome.utils.log.LogUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static String MAIN_PACKAGE;

    private static final String TAG = "MainActivity";

    private TextView tUsername;
    private TextView tPassword;
    private TextView tService;
    private TextView tCallId;
    private TextView tContent;
    private Button bLogin;
    private Button bRegister;
    private Button bSendAudio;
    private Button bSendMessage;
    private Button bSettings;
    private Button bSuspension;

    private Button test;

    private SipProfile mSipProfile;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化SIP协议栈
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        MAIN_PACKAGE = this.getPackageName();
        LogUtils.d("SipService", "MAIN_PACKAGE: " + MAIN_PACKAGE);
        serviceIntent.setPackage(MAIN_PACKAGE);
        startService(serviceIntent);

        mSipProfile = SipProfile.getProfileFromDbId(this, LoginActivity.ACCONT_ID, DatabaseContentProvider.ACCOUNT_FULL_PROJECTION);

        setContentView(R.layout.activity_main);
        initView();

        if (mSipProfile.id != SipProfile.INVALID_ID) {
            tUsername.setText(mSipProfile.username);
            tPassword.setText(mSipProfile.getPassword());
            tService.setText(mSipProfile.reg_uri.split(":")[1]);
        }
    }

    private void initView() {
        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        mActionBarToolbar.setTitle("测试工程");
        setSupportActionBar(mActionBarToolbar);

        tUsername = (TextView) findViewById(R.id.username);
        tPassword = (TextView) findViewById(R.id.password);
        tService = (TextView) findViewById(R.id.service);
        tCallId = (TextView) findViewById(R.id.call_id);
        tContent = (TextView) findViewById(R.id.content);
        bLogin = (Button) findViewById(R.id.login);
        bRegister = (Button) findViewById(R.id.register);
        bSendMessage = (Button) findViewById(R.id.send_message);
        bSendAudio = (Button) findViewById(R.id.send_audio);
        bSettings = (Button) findViewById(R.id.settings);
        bSuspension = (Button) findViewById(R.id.suspension);

        test = (Button) findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent publishIntent = new Intent(SipManager.ACTION_SIP_STACK_CHANGE);
                MainActivity.this.sendBroadcast(publishIntent);
            }
        });

        bSuspension.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SuspensionWindowService.class);
                startService(intent);
            }
        });

        bLogin.setOnClickListener(this);
        bRegister.setOnClickListener(this);
        bSendMessage.setOnClickListener(this);
        bSendAudio.setOnClickListener(this);
        bSettings.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            Toast.makeText(this, "退出登陆", Toast.LENGTH_SHORT).show();
            Intent publishIntent = new Intent(SipManager.ACTION_FINISH);
            MainActivity.this.sendBroadcast(publishIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login: // 登陆
                String username = tUsername.getText().toString().trim();
                String password = tPassword.getText().toString().trim();
                String service = tService.getText().toString().trim();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(service)) {
                    Toast.makeText(MainActivity.this, "输入的内容不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 构建用户信息
                mSipProfile = PjSipAccount.buildAccount(mSipProfile, username, password, service);

                // 更新数据库，并向服务器注册
                LogUtils.d(TAG, "mSipProfile.id:" + mSipProfile.id);
                if (mSipProfile.id == SipProfile.INVALID_ID) {
                    LogUtils.i(TAG, "没有账户添加一个");
                    getContentResolver().insert(SipProfile.ACCOUNT_URI, mSipProfile.getDbContentValues());
                } else {
                    LogUtils.i(TAG, "更新这个账户");
                    getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, LoginActivity.ACCONT_ID), mSipProfile.getDbContentValues(), null, null);
                }
                break;

            case R.id.register: // 发起视频呼叫
                try {
                    UserAgentBroadcastReceiver.getUserAgent().makeCallOrVideo(tCallId.getText().toString().trim(), 1, null, true);
                } catch (SameThreadException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.send_message: // 发送短信
                try {
                    UserAgentBroadcastReceiver.getUserAgent().sendMessage(
                            tCallId.getText().toString().trim(), // call id
                            tContent.getText().toString().trim(), // 短信内容
                            LoginActivity.ACCONT_ID);
                } catch (SameThreadException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.send_audio: // 发送音频
                try {
                    UserAgentBroadcastReceiver.getUserAgent().makeCallOrVideo(tCallId.getText().toString().trim(), 1, null, false);
                } catch (SameThreadException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.settings: { // 设置界面
                Intent intent = new Intent(this, MainPrefsActivity.class);
                startActivity(intent);
            }
            break;
        }
    }
}