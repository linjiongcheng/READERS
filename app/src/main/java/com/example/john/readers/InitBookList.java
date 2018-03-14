package com.example.john.readers;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by John on 2017/5/16.
 */

//  根据联网下载下来的字符串数据来实例化BookLab

public class InitBookList {

    private List<Book> mBookList;

    public InitBookList(String url,String xmlData){
        mBookList = new ArrayList<>();
        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();

            String imageUrl = "";
            String title = "";
            String author = "";
            String voiceUrl = "";
            String chapter = "";
            while(eventType != XmlPullParser.END_DOCUMENT){
                String nodeName = xmlPullParser.getName();
                switch(eventType){
                    //开始解析某个节点
                    case XmlPullParser.START_TAG: {
                        if("imageUrl".equals(nodeName)){
                            imageUrl = xmlPullParser.nextText();
                        }else if("title".equals(nodeName)){
                            title = xmlPullParser.nextText();
                        } else if("author".equals(nodeName)) {
                            author = xmlPullParser.nextText();
                        }else if("voiceUrl".equals(nodeName)){
                            voiceUrl = xmlPullParser.nextText();
                        }else if("chapter".equals(nodeName)) {
                            chapter = xmlPullParser.nextText();
                        }
                        break;
                    }
                    //完成解析某个节点
                    case XmlPullParser.END_TAG:{
                        if("book".equals(nodeName)){
                            Book book = new Book();
                            book.setImageUrl(url+"pic/"+imageUrl);
                            book.setTitle(title);
                            book.setAuthor(author);
                            book.setVoiceUrl(url+"voice/"+voiceUrl);

                            String[] chapterArray = chapter.split("\\|");
                            for(int i = 0; i < chapterArray.length; i++ ) {
                                chapterArray[i] = url + "voice/" + voiceUrl + "/" + chapterArray[i];
                            }
                            List<String> temp = Arrays.asList(chapterArray);
                            book.setChapterList(temp);
                            mBookList.add(book);
                        }
                        break;
                    }
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public List<Book> getBookList(){
        return mBookList;
    }

}
