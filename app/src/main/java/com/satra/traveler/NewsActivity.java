package com.satra.traveler;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.satra.traveler.adapter.NewsAdapter;
import com.satra.traveler.models.News;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;

public class NewsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "NewsActivity";
    private NewsAdapter newsAdapter;
    private RecyclerView newsRecyclerView;
    private Snackbar snackbar;
    private RecyclerView.AdapterDataObserver adapterObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Toolbar toolbar = (Toolbar) findViewById(R.id.newstoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        User travelerUser = User.findAll(User.class).next();
        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            FirebaseAuth.getInstance().signInWithEmailAndPassword(travelerUser.getUseremail(), travelerUser.getPassword())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(LOG_TAG, "State: "+task.isSuccessful());
                        }
                    });
        //prepare recyclerview
        newsRecyclerView = (RecyclerView) findViewById(R.id.newsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.setSmoothScrollbarEnabled(true);
        newsRecyclerView.setLayoutManager(layoutManager);
        newsAdapter = new NewsAdapter(NewsActivity.this, News.class, R.layout.news_item_layout, NewsAdapter.ViewHolder.class,
                FirebaseDatabase.getInstance().getReference(TConstants.FIREBASE_NEWS_FEED));
        newsRecyclerView.setAdapter(newsAdapter);
        newsRecyclerView.setHasFixedSize(true);
        newsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        adapterObserver = new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (itemCount > 0)
                    snackbar.dismiss();
            }
        };

        newsAdapter.registerAdapterDataObserver(adapterObserver);

    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(newsRecyclerView.getAdapter().getItemCount() == 0){
            snackbar = Snackbar.make(newsRecyclerView, getString(R.string.empty_news), Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        newsAdapter.unregisterAdapterDataObserver(adapterObserver);
    }
}
