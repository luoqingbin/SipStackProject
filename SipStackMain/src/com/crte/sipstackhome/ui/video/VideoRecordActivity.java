package com.crte.sipstackhome.ui.video;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.ui.BaseActivity;

/**
 * 视频记录
 * Created by Administrator on 2015/12/14.
 */
public class VideoRecordActivity extends BaseActivity {
    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private Button test_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);

        mToolbar = getActionBarToolbar(false);
        mToolbar.setTitle("视频记录");
        mRecyclerView = getLinearLayoutRecyclerView(LinearLayoutManager.VERTICAL);

        test_video = (Button) findViewById(R.id.test_video);
        test_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(VideoRecordActivity.this, VideoCamera.class);
//                startActivity(intent);
            }
        });

    }
}
