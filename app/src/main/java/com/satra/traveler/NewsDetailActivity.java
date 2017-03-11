package com.satra.traveler;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.satra.traveler.utils.TConstants;

public class NewsDetailActivity extends AppCompatActivity {

    public final String LOGTAG = "NewsDetailActivity";
    public String newsDetailKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(getIntent().hasExtra(TConstants.NEWS_DETAIL_KEY)){
            String key = getIntent().getStringExtra(TConstants.NEWS_DETAIL_KEY);
            Log.d(LOGTAG, "Key: "+key);
            newsDetailKey = key;
            toolbar.setTitle(key);
            toolbar.setSubtitle(key);
        }
        setSupportActionBar(toolbar);

        com.github.clans.fab.FloatingActionButton fabShare = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabShare);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Share with your own Shit!! "+newsDetailKey , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        com.github.clans.fab.FloatingActionButton fabComment = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabComment);
        fabComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Comment with your own Shit!! "+newsDetailKey , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null){
            newsDetailKey = savedInstanceState.getString(TConstants.NEWS_DETAIL_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null){
            outState.putString(TConstants.NEWS_DETAIL_KEY, newsDetailKey);
        }
        super.onSaveInstanceState(outState);
    }
}
