package com.satra.traveler;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.database.FirebaseDatabase;
import com.satra.traveler.adapter.NewsAdapter;
import com.satra.traveler.models.News;
import com.satra.traveler.utils.TConstants;

public class NewsActivity extends AppCompatActivity {

    private NewsAdapter newsAdapter;
    private RecyclerView newsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Toolbar toolbar = (Toolbar) findViewById(R.id.newstoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //prepare recyclerview
        newsRecyclerView = (RecyclerView) findViewById(R.id.newsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);
        newsRecyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter(NewsActivity.this, News.class, R.layout.news_item_layout, NewsAdapter.ViewHolder.class,
                FirebaseDatabase.getInstance().getReference(TConstants.FIREBASE_NEWS_FEED).orderByChild("newsTimeStamp"));
        newsRecyclerView.setAdapter(newsAdapter);
        newsRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }
}
