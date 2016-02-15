package com.crte.sipstackhome.ui.login;

import android.content.ContentUris;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.models.Contacts;
import com.crte.sipstackhome.models.ShortMessage;
import com.crte.sipstackhome.pjsip.PjSipAccount;
import com.crte.sipstackhome.ui.BaseActivity;
import com.crte.sipstackhome.ui.home.HomeActivity;

/**
 * 登陆界面
 * Created by Torment on 2015/12/26.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    public static final int ACCONT_ID = 1; // 单用户，ID只有一个

    private EditText mUsername;
    private EditText mPassword;
    private EditText mSipAddress;

    private Button mLogin;

    private SipProfile mSipProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化SIP协议栈
        Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
        serviceIntent.setPackage(this.getPackageName());
        startService(serviceIntent);

        mSipProfile = SipProfile.getProfileFromDbId(this, ACCONT_ID, DatabaseContentProvider.ACCOUNT_FULL_PROJECTION);
        if (mSipProfile.id != SipProfile.INVALID_ID) {
            startIntent(HomeActivity.class, true);
            return;
        }

        setContentView(R.layout.activity_login);

        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mSipAddress = (EditText) findViewById(R.id.address);
        mLogin = (Button) findViewById(R.id.login);
        mLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login: // 登陆
                build();

                // 添加测试联系人
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        // new
                        Contacts.setFastIndex(LoginActivity.this); // 添加联系人信息
                        ShortMessage.addTestData(LoginActivity.this);// 添加短信消息

//                        ContactPersonDao.getInstance().setFastIndex(LoginActivity.this); // 添加联系人信息
//                        ShortMessageDao.getInstance().addTestData(LoginActivity.this); // 添加短信消息
//                        CallRecordDao.getInstance().addTestData(LoginActivity.this); // 添加通话记录
                    }
                }.start();
                break;
        }
    }

    public void build() {
        String username = mUsername.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String service = mSipAddress.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(service)) {
            Toast.makeText(this, "输入的内容不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构建用户信息
        mSipProfile = PjSipAccount.buildAccount(mSipProfile, username, password, service);

        // 更新数据库，并向服务器注册
        if (mSipProfile.id == SipProfile.INVALID_ID) {
//            PjSipAccount.applyNewAccountDefault(mSipProfile);
            getContentResolver().insert(SipProfile.ACCOUNT_URI, mSipProfile.getDbContentValues());
        } else {
            getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_ID_URI_BASE, mSipProfile.id), mSipProfile.getDbContentValues(), null, null);
        }

        startIntent(HomeActivity.class, true);
    }
}
