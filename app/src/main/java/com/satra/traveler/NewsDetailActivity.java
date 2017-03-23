package com.satra.traveler;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.satra.traveler.adapter.CommentsAdapter;
import com.satra.traveler.models.Comments;
import com.satra.traveler.models.User;
import com.satra.traveler.utils.TConstants;
import com.satra.traveler.utils.Tutility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import mehdi.sakout.fancybuttons.FancyButton;


public class NewsDetailActivity extends AppCompatActivity {

    public final String LOGTAG = "NewsDetailActivity";
    public String newsDetailKey, detailContent, newsAuthor, newsTitle;
    private TextView dateTextView, contentTextView;
    private long timestamp;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private RecyclerView commentsRecyclerView;
    private TextView commentsTitleTextView;
    private ImageView detailImageView;
    private DatabaseReference databaseReference;
    private User travelerUser;
    private String newsImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        databaseReference = FirebaseDatabase.getInstance().getReference(TConstants.FIREBASE_NEWS_FEED);

        contentTextView = (TextView) findViewById(R.id.newsContentTextView);
        dateTextView = (TextView) findViewById(R.id.newsDetailTimestampTextView);
        detailImageView = (ImageView) findViewById(R.id.detailImageViewPreview);
        //comments view
        commentsRecyclerView = (RecyclerView) findViewById(R.id.commentsRecyclerView);
        commentsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        commentsTitleTextView = (TextView)findViewById(R.id.commentsTitleTextView);
        commentsTitleTextView.setText(getString(R.string.new_comments, 0));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(getIntent().hasExtra(TConstants.NEWS_DETAIL_KEY)){
            Intent itemIntent = getIntent();
            newsDetailKey = itemIntent.getStringExtra(TConstants.NEWS_DETAIL_KEY);
            detailContent = itemIntent.getStringExtra(TConstants.NEWS_DETAIL_CONTENT);
            timestamp = itemIntent.getLongExtra(TConstants.NEWS_DETAIL_TIME, 0);
            newsAuthor = itemIntent.getStringExtra(TConstants.NEWS_AUTHOR);
            newsTitle = itemIntent.getStringExtra(TConstants.NEWS_TITLE);
            newsImage = itemIntent.getStringExtra(TConstants.NEWS_MEDIA_LINK);
            Log.d(LOGTAG, "Key: "+newsDetailKey);
            toolbar.setTitle(newsTitle);
            toolbar.setSubtitle(newsAuthor);
        }
        setSupportActionBar(toolbar);

        //setup bottom sheet
        View bottomView = findViewById(R.id.comments_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomView);
        bottomSheetBehavior.setPeekHeight(0);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState){
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheetBehavior.setPeekHeight(0);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING | BottomSheetBehavior.STATE_SETTLING:
                        bottomSheetBehavior.setPeekHeight(200);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        com.github.clans.fab.FloatingActionButton fabShare = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabShare);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //IMPLEMENT SHARING title
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,newsTitle+" \n http://traveler.cm/news");
                NewsDetailActivity.this.startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            }
        });
        com.github.clans.fab.FloatingActionButton fabComment = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fabComment);
        fabComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Implement commenting via BottomSheet
                bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        FancyButton fabPostComment = (FancyButton) findViewById(R.id.buttonPostComment);
        fabPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get comment and post
                final EditText commentEditText = (EditText) findViewById(R.id.commentEditTextview);
                String comment = commentEditText.getText().toString();
                if (!comment.isEmpty()){
                    //send comment if not empty
                    databaseReference.child(newsDetailKey+"/comments").push()
                            .setValue(new Comments(travelerUser.getUsername(), comment, System.currentTimeMillis()))
                    .addOnSuccessListener(NewsDetailActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            commentEditText.setText("");
                        }
                    });
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupUI();
    }

    /**
     * Setup the views with the appropriate content
     */
    private void setupUI() {
        dateTextView.setText(Tutility.getMicroTimeString(this, timestamp, System.currentTimeMillis(),
                new SimpleDateFormat("dd/MM/yyyy H:mm:s a", Locale.US).format(new Date(timestamp))));
        contentTextView.setText(detailContent);
    }

    /**
     * Fetch comments for this item and display in list
     * @param newsDetailKey key of the item to be fetched from datastore
     */
    private void loadComments(String newsDetailKey) {
        CommentsAdapter commentsAdapter = new CommentsAdapter(this,
                Comments.class,
                R.layout.item_message_layout,
                CommentsAdapter.ViewHolder.class,
                databaseReference.child(newsDetailKey+"/comments")
                );
        commentsRecyclerView.setAdapter(commentsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        travelerUser = User.findAll(User.class).next();
        loadComments(newsDetailKey);
        commentsTitleTextView.setText(getString(R.string.new_comments, commentsRecyclerView.getAdapter().getItemCount()));
            Glide.with(this)
                    .load(Uri.parse(newsImage))
                    .placeholder(R.drawable.loading_drawable)
                    .crossFade()
                    .error(R.drawable.ic_launcher_web)
                    .into(detailImageView);
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
