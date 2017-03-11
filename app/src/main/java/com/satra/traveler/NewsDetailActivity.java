package com.satra.traveler;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

public class NewsDetailActivity extends AppCompatActivity {

    public final String LOGTAG = "NewsDetailActivity";
    public String newsDetailKey, detailContent, newsAuthor, newsTitle;
    private TextView dateTextView, contentTextView;
    private long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        contentTextView = (TextView) findViewById(R.id.newsContentTextView);
        dateTextView = (TextView) findViewById(R.id.newsDetailTimestampTextView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(getIntent().hasExtra(TConstants.NEWS_DETAIL_KEY)){
            Intent itemIntent = getIntent();
            newsDetailKey = itemIntent.getStringExtra(TConstants.NEWS_DETAIL_KEY);
            detailContent = itemIntent.getStringExtra(TConstants.NEWS_DETAIL_CONTENT);
            timestamp = itemIntent.getLongExtra(TConstants.NEWS_DETAIL_TIME, 0);
            newsAuthor = itemIntent.getStringExtra(TConstants.NEWS_AUTHOR);
            newsTitle = itemIntent.getStringExtra(TConstants.NEWS_TITLE);
            Log.d(LOGTAG, "Key: "+newsDetailKey);
            Log.d(LOGTAG, "content: "+detailContent);
            toolbar.setTitle(newsTitle);
            toolbar.setSubtitle(newsAuthor);
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
        setupUI();
        loadComments(newsDetailKey);
    }

    /**
     * Setup the views with the appropriate content
     */
    private void setupUI() {
        dateTextView.setText(Tutility.getMicroTimeString(this, timestamp, System.currentTimeMillis(), ""));
        contentTextView.setText(detailContent);
    }

    /**
     * Fetch comments for this item and display in list
     * @param newsDetailKey key of the item to be fetched from datastore
     */
    private void loadComments(String newsDetailKey) {

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null){
            newsDetailKey = savedInstanceState.getString(TConstants.NEWS_DETAIL_KEY);
            newsTitle = savedInstanceState.getString(TConstants.NEWS_TITLE);
            detailContent = savedInstanceState.getString(TConstants.NEWS_DETAIL_CONTENT);
            newsAuthor = savedInstanceState.getString(TConstants.NEWS_AUTHOR);
            timestamp = savedInstanceState.getLong(TConstants.NEWS_DETAIL_TIME);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outState != null){
            outState.putString(TConstants.NEWS_DETAIL_KEY, newsDetailKey);
            outState.putString(TConstants.NEWS_TITLE, newsTitle);
            outState.putString(TConstants.NEWS_AUTHOR, newsAuthor);
            outState.putString(TConstants.NEWS_DETAIL_CONTENT, detailContent);
            outState.putLong(TConstants.NEWS_DETAIL_TIME, timestamp);
        }
        super.onSaveInstanceState(outState);
    }
}
