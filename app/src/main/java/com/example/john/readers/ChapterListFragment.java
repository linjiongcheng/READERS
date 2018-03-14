package com.example.john.readers;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.john.readers.util.DividerItemDecoration;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class ChapterListFragment extends BaseFragment {

    public static Book mCurrentBook;
    private Book mBook;
    private ImageView mImageView;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private RecyclerView mRecyclerView;

//    private VoicePlayer mVoicePlayer;
    private List<Boolean> isClicks;
    private MyAdapter mMyAdapter;

    public static ChapterListFragment newInstance(Book book) {
        Bundle args = new Bundle();
        args.putSerializable("book", book);

        ChapterListFragment fragment = new ChapterListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentBook = (Book) getArguments().getSerializable("book");
        mBook = mListener.getBook();
        mListener.setChapterListFragmentHandler(myHandler);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chapter_list, container, false);

        mImageView = (ImageView) v.findViewById(R.id.activity_chapter_list_image_view);
        Picasso.with(getActivity())
                .load(mCurrentBook.getImageUrl())
                .into(mImageView);


        mTitleTextView = (TextView) v.findViewById(R.id.activity_chapter_list_text_view_title);
        mTitleTextView.setText(mCurrentBook.getTitle());

        mAuthorTextView = (TextView) v.findViewById(R.id.activity_chapter_list_text_view_author);
        mAuthorTextView.setText(mCurrentBook.getAuthor());

        mMyAdapter = new MyAdapter(mCurrentBook.getChapterList());

        mRecyclerView = (RecyclerView) v.findViewById(R.id.activity_chapter_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //添加Android自带的分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mMyAdapter);
        //添加Android自带的分割线

        return v;
    }

    Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    adjustClickColor(mCurrentBook.getCurrentChapterPosition());
                    break;
                default:
            }
        }
    };

    public void adjustClickColor(int position) {
        for(int i = 0; i <isClicks.size();i++){
            isClicks.set(i,false);
        }
        isClicks.set(position,true);
        mMyAdapter.notifyDataSetChanged();
    }

    private class MyHolder extends RecyclerView.ViewHolder{
        TextView chapterName;

        public MyHolder(View view){
            super(view);
            chapterName = (TextView) view;
        }

        public void bindChapterItem(String chapter) {
            String[] temp = chapter.split("/");
            chapterName.setText(temp[temp.length - 1]);
        }
    }




    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        private List<String> mChapterList;

        public MyAdapter(List<String> chapterList){
            mChapterList = chapterList;
            isClicks = new ArrayList<>();
            for(int i = 0; i < mChapterList.size(); i++){
                isClicks.add(false);
            }
            if (mBook != null && mBook.getUUID().equals(mCurrentBook.getUUID())) {
                isClicks.set(mBook.getCurrentChapterPosition(), true);
            }
//            notifyDataSetChanged();

//            if (mBook != null) {
//                mVoicePlayer = VoicePlayer.getInstance(getActivity(), mBook);
//            }
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            final MyHolder holder = new MyHolder(view);
            holder.chapterName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.clickToPlay(mCurrentBook, holder.getAdapterPosition());
                    adjustClickColor(holder.getAdapterPosition());
                    notifyDataSetChanged();
                }
            });
            return holder;
        }


        @Override
        public void onBindViewHolder(MyHolder holder, int position){
            String chapter = mChapterList.get(position);
            holder.bindChapterItem(chapter);

            if(isClicks.get(position)){
                holder.chapterName.setTextColor(Color.parseColor("#00a0e9"));
            }else{
                holder.chapterName.setTextColor(Color.parseColor("#000000"));
            }
        }

        @Override
        public int getItemCount(){
            return mChapterList.size();
        }

    }

}
