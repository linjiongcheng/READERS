package com.example.john.readers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by John on 2017/5/16.
 */

public class Book implements Serializable{

    private UUID mUUID;
    private String imageUrl;
    private String title;
    private String author;
    private String voiceUrl;            //书籍章节所在的目录

    private int currentChapterPosition;         //指示当前正在播放的章节

    private List<String> mChapterList;          //指向书籍所有章节的url的集合

    public Book() {
        mUUID = UUID.randomUUID();
        currentChapterPosition = 0;
        mChapterList = new ArrayList<>();
    }

    public UUID getUUID() {
        return mUUID;
    }

    public void setUUID(UUID UUID) {
        mUUID = UUID;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public int getCurrentChapterPosition() {
        return currentChapterPosition;
    }

    public void setCurrentChapterPosition(int currentChapterPosition) {
        this.currentChapterPosition = currentChapterPosition;
    }

    public List<String> getChapterList() {
        return mChapterList;
    }

    public void setChapterList(List<String> chapterList) {
        mChapterList = chapterList;
    }
}
