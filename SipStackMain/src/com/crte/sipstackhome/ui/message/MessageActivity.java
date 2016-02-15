package com.crte.sipstackhome.ui.message;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.ui.BaseActivity;


/**
 * 短消息聊天界面
 */
public class MessageActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ListView mListview;
    private EditText mEditMesasge;
    private ImageView mSendMessage;

    private ShortMessageAdapter mShortMessageAdapter;

    private int mColorIndex;
    private int mColorResources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mColorIndex = getIntent().getIntExtra(ACTIVITY_THEME_COLOR, -1);
        mColorResources = getResources().getColor(BaseActivity.HEADER_COLOR[mColorIndex]);

        mToolbar = getActionBarToolbar(true);
        mToolbar.setTitle("短信");
        mToolbar.setBackgroundColor(mColorResources);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(getResources().getColor(BaseActivity.HEADER_COLOR_BAR[mColorIndex]));
        }

        mListview = (ListView) findViewById(R.id.listview);
        mShortMessageAdapter = new ShortMessageAdapter(this, 1);
        mListview.setAdapter(mShortMessageAdapter);
        mEditMesasge = (EditText) findViewById(R.id.edit_message);
        mSendMessage = (ImageView) findViewById(R.id.send_message);
        mSendMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                String message = mEditMesasge.getText().toString().trim();
                if (TextUtils.isEmpty(message)) {
                    Snackbar.make(mEditMesasge, "消息不能为空！", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.action_user:
                Snackbar.make(mToolbar, "查看联系人信息功能未实现！", Snackbar.LENGTH_SHORT).show();
                return true;
            case R.id.action_delete:
                Snackbar.make(mToolbar, "清空聊天信息功能未实现！", Snackbar.LENGTH_SHORT).show();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}