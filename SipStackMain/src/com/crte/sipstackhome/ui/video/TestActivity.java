package com.crte.sipstackhome.ui.video;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.models.VideoBean;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.util.ArrayList;

/**
 * Created by wangz on 2016/1/2.
 */
public class TestActivity extends Activity {
    private Button add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        add = (Button) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoBean callRecordBean = new VideoBean();
                callRecordBean.toSipUri = "<sip:100@192.168.1.1>";
                VideoBean.insertDatas(getContentResolver(), callRecordBean);
            }
        });


        Cursor cursor = VideoBean.queryAllDatas(getContentResolver());
        ArrayList<VideoBean> callRecordBean = VideoBean.getVideoBean(cursor);
        for (int i = 0; i < callRecordBean.size(); i++) {
            LogUtils.e("TAST", "callRecordBean:" + callRecordBean.get(i).toString());
        }

    }
}
