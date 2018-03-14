package com.example.john.readers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 2018/3/11.
 */

public class BookLab {

    private static BookLab mBookLab;
    private List<Book> mBookList;

    private BookLab(List<Book> bookList){
        mBookList = new ArrayList<>();
        if (bookList != null) {
            mBookList.addAll(bookList);
        }
    }

    public static BookLab getInstance(List<Book> bookList) {
        if (mBookLab == null) {
            synchronized (BookLab.class) {
                mBookLab = new BookLab(bookList);
            }
        }
        return mBookLab;
    }

    public static BookLab getInstance() {
        return getInstance(null);
    }

    public List<Book> getBookList() {
        return mBookList;
    }

    public void setBookList(List<Book> bookList) {
        mBookList = bookList;
    }
}
