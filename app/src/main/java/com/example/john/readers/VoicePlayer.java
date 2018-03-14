package com.example.john.readers;

import android.content.Context;
import android.media.MediaPlayer;


/**
 * Created by John on 2017/5/16.
 */

public class VoicePlayer {
    private static VoicePlayer mVoicePlayer;
    private MediaPlayer mMediaPlayer;
    private static Book mBook;
    private boolean mPrePlayState;

    private VoicePlayer(Book book){
        mBook = book;
        mPrePlayState = false;
    }

    public static VoicePlayer getInstance(Book book) {
        if (mVoicePlayer == null) {
            synchronized (VoicePlayer.class) {
                if (book != null) {
                    mVoicePlayer = new VoicePlayer(book);
                }
            }
        } else {
            // 当点击的章节和当前单例中的章节不对应时，更新单例中的书籍信息
            if (!(mBook.getTitle().equals(book.getTitle()) && mBook.getAuthor().equals(book.getAuthor())
                    && mBook.getCurrentChapterPosition() == book.getCurrentChapterPosition())) {
                mBook = book;
            }
        }
        return mVoicePlayer;
    }


    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        mMediaPlayer = mediaPlayer;
    }


    public boolean isPrePlayState() {
        return mPrePlayState;
    }

    public void setPrePlayState(boolean prePlayState) {
        mPrePlayState = prePlayState;
    }
}
