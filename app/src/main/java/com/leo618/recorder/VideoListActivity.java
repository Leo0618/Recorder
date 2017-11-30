package com.leo618.recorder;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * function:
 *
 * <p>
 * Created by Leo on 2017/11/30.
 */
public class VideoListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView mListUI = (RecyclerView) findViewById(R.id.mListUI);
        mListUI.setLayoutManager(new LinearLayoutManager(this));
        final File[] videos = new File(getExternalCacheDir().getAbsolutePath()).listFiles();
        if (videos == null) return;
        mListUI.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ItemViewHolder myholder = (ItemViewHolder) holder;
                final File     item     = videos[position];
                String         name     = item.getName();
                myholder.name.setText(String.format("%s.mp4", formatName(name.substring(0, name.lastIndexOf(".")))));
                myholder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(VideoListActivity.this, VideoPlayActivity.class);
                        intent.putExtra("path", item.getAbsolutePath());
                        startActivity(intent);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return videos.length;
            }

            class ItemViewHolder extends RecyclerView.ViewHolder {
                TextView name;

                ItemViewHolder(View itemView) {
                    super(itemView);
                    name = itemView.findViewById(android.R.id.text1);
                }
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private String formatName(String name) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.parseLong(name));
    }
}
