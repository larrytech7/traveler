package com.satra.traveler.models;

import com.orm.SugarRecord;

/**
 * Created by Larry Akah on 3/10/17.
 */

public class News extends SugarRecord {

    private String newsId;
    private String newsSource;
    private String newsTitle;
    private String newsContent;
    private String newsMultimediaLink;
    private String newsTag;
    private long newsTimeStamp;

    public News() {
    }

    public String getNewsId() {
        return newsId;
    }

    public void setNewsId(String newsId) {
        this.newsId = newsId;
    }

    public String getNewsSource() {
        return newsSource == null ? "" : newsSource;
    }

    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    public String getNewsTitle() {
        return newsTitle == null ? "" : newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsContent() {
        return newsContent == null ? "" : newsContent;
    }

    public void setNewsContent(String newsContent) {
        this.newsContent = newsContent;
    }

    public String getNewsMultimediaLink() {
        return newsMultimediaLink == null ? "" : newsMultimediaLink;
    }

    public void setNewsMultimediaLink(String newsMultimediaLink) {
        this.newsMultimediaLink = newsMultimediaLink;
    }

    public String getNewsTag() {
        return newsTag;
    }

    public void setNewsTag(String newsTag) {
        this.newsTag = newsTag;
    }

    public long getNewsTimeStamp() {
        return newsTimeStamp;
    }

    public void setNewsTimeStamp(long newsTimeStamp) {
        this.newsTimeStamp = newsTimeStamp;
    }
}
