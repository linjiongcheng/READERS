package com.example.john.readers;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends BaseFragment implements View.OnClickListener {
    private Button recognition;
//    private Button synthesis;
    private EditText mSearchKeyword;
    private ImageButton mSearchButton;
    private ImageButton mCancelButton;
    private MySpeechRecognizer mySpeechRecognizer;
    public static MySpeechSynthesizer mySpeechSynthesizer;

    public SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private MyAdapter mMyAdapter;

    private List<Book> mSearchBookList;

//    private Book mBook;
    private VoicePlayer mVoicePlayer;

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSearchBookList = new ArrayList<>();
//        mBook = mListener.getBook();
//        Log.i("hahaha",mBook+"");
        mVoicePlayer = VoicePlayer.getInstance(mListener.getBook());
        mListener.setMainFragmentHandler(myHandler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        recognition = (Button) v.findViewById(R.id.speech_recognition);
//        synthesis = (Button) v.findViewById(R.id.speech_synthesis);
        mSearchKeyword = ((EditText) v.findViewById(R.id.search_keyword));
        mSearchKeyword.setSingleLine();
        mSearchKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchKeyword.setCursorVisible(true);
            }
        });

        mSearchKeyword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String temp = mSearchKeyword.getText().toString();

                if (!temp.equals("") && temp.substring(temp.length() - 1).equals("。")) {
                    temp = temp.substring(0,temp.length()-1);
                    mSearchKeyword.setText("");
                    mSearchKeyword.append(temp);
                }

                if (temp.equals("")) {
                    mRecyclerView.setAdapter(new MyAdapter(BookLab.getInstance().getBookList()));
                    Log.i("hahaha","空白");
                } else if ((temp.equals("暂停") || temp.equals("播放")) && mVoicePlayer != null) {
                    if (mVoicePlayer.getMediaPlayer() != null) {
                        if ((temp.equals("暂停") && mVoicePlayer.getMediaPlayer().isPlaying())
                                || (temp.equals("播放") && !mVoicePlayer.getMediaPlayer().isPlaying())) {
                            mListener.setVoicePlayState();
                            mListener.pause();
                            Log.i("hahaha","播放暂停");
                        }
                    } else if (temp.equals("播放")){
                        mListener.play(mListener.getBook());
                        Log.i("hahaha","开始播放");
                    }
                    mSearchKeyword.setText(null);
                } else if ((temp.equals("暂停") || temp.equals("播放")) && mVoicePlayer == null) {
                    mSearchKeyword.setText(null);
                    Log.i("hahaha","播放暂停没意义");
                } else if (temp.contains("播放")) {
                    Book target = null;
                    for (Book book : BookLab.getInstance().getBookList()) {
                        if (temp.substring(2,temp.length()).equals(book.getTitle())) {
                            target = book;
                            break;
                        }
                    }
                    if (target != null) {
                        mListener.clickToPlay(target, target.getCurrentChapterPosition());
                        mVoicePlayer = VoicePlayer.getInstance(target);
                        mSearchKeyword.setText(null);
                        Log.i("hahaha","播放或暂停某本书");
                    }
                }

                if (mVoicePlayer != null) {
                    if (temp.contains("第一")) {
                        mListener.clickToPlay(mListener.getBook(), 0);
                        mSearchKeyword.setText(null);
                    } else if (temp.contains("最后")) {
                        mListener.clickToPlay(mListener.getBook(), mListener.getBook().getChapterList().size() - 1);
                        mSearchKeyword.setText(null);
                    } else if (temp.contains("下一") || temp.contains("后一")) {
                        mListener.clickToPlay(mListener.getBook(), mListener.getBook().getCurrentChapterPosition() + 1);
                        if (mListener.getBook().getCurrentChapterPosition() + 1 >= mListener.getBook().getChapterList().size()) {
                            mySpeechSynthesizer.startSynthesis("已经是最后一集了");
                        }
                        mSearchKeyword.setText(null);
                    }else if (temp.contains("上一") || temp.contains("前一")) {
                        mListener.clickToPlay(mListener.getBook(), mListener.getBook().getCurrentChapterPosition() - 1);
                        if (mListener.getBook().getCurrentChapterPosition() == 0) {
                            mySpeechSynthesizer.startSynthesis("已经是第一集了");
                        }
                        mSearchKeyword.setText(null);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mCancelButton = (ImageButton)v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchKeyword.setText("");
            }
        });

        mSearchButton = (ImageButton)v.findViewById(R.id.search_button);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchBookList.clear();
                for (Book book : BookLab.getInstance().getBookList()) {
                    if (book.getTitle().contains(mSearchKeyword.getText().toString()) || book.getAuthor().contains(mSearchKeyword.getText().toString())) {
                        mSearchBookList.add(book);
                    }
                }
                mRecyclerView.setAdapter(new MyAdapter(mSearchBookList));
            }
        });

        mySpeechRecognizer = new MySpeechRecognizer(getActivity(),mSearchKeyword);
        mySpeechSynthesizer = new MySpeechSynthesizer(getActivity());
        SpeechUtility.createUtility(getActivity(), SpeechConstant.APPID + "= 58c56385");
        recognition.setOnClickListener(this);
//        synthesis.setOnClickListener(this);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView); // 获取RecyclerView组件
        mGridLayoutManager = new GridLayoutManager(getActivity(),3);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        updateRecyclerView();

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout_recycler_view);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                //  调用Activity里的方法，执行联网刷新数据操作
                mListener.updateData();
            }
        });
        return v;
    }

    // 接收Activity发送过来的刷新数据成功的消息，刷新列表视图
    Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 2:
                    updateRecyclerView();
                    mSwipeRefreshLayout.setRefreshing(false);
                    break;
                default:
            }
        }
    };

    public void updateRecyclerView() {
        mMyAdapter = new MyAdapter(BookLab.getInstance().getBookList());
        mRecyclerView.setAdapter(mMyAdapter); // 将适配器与GridView关联
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speech_recognition:
                if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.
                        permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(getActivity(),new
                            String[]{android.Manifest.permission.RECORD_AUDIO},1);
                }else{
                    startRecognition();
                }
                break;
//            case R.id.speech_synthesis:
//                mySpeechSynthesizer.startSynthesis(mSearchKeyword.getText().toString());
//                break;
            default:
        }
    }

    private void startRecognition() {
        mSearchKeyword.setText("");
        mySpeechRecognizer.srartRecognition();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,
                                           int[] grantResults){
        switch(requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startRecognition();
                }else{

                }
                break;
            default:
        }
    }



    private class MyHolder extends RecyclerView.ViewHolder{
        View bookView;
        ImageView bookImage;
        TextView bookName;

        public MyHolder(View view){
            super(view);
            bookView = view;
            bookImage = (ImageView)view.findViewById(R.id.image);
            bookName = (TextView)view.findViewById(R.id.title);
        }

        public void bindBookItem(Book book) {
            bookName.setText(book.getTitle());
            Picasso.with(getActivity())
                    .load(book.getImageUrl())
                    .into(bookImage);
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {

        private List<Book> mBookList;
        private int mPosition;

        public MyAdapter(List<Book> bookList){
            mBookList = bookList;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item,parent,false);
            final MyHolder holder = new MyHolder(view);
            holder.bookView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPosition = holder.getAdapterPosition();
                    Book book = mBookList.get(mPosition);
                    mySpeechSynthesizer.startSynthesis(book.getTitle() + "。作者" + book.getAuthor());
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fragment_slide_left_enter,
                                    R.anim.fragment_slide_left_exit,
                                    R.anim.fragment_slide_right_enter,
                                    R.anim.fragment_slide_right_exit)
                            .replace(R.id.fragment_container, ChapterListFragment.newInstance(book))
                            .addToBackStack(null)
                            .commit();
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position){
            Book book = mBookList.get(position);
            holder.bindBookItem(book);
        }

        @Override
        public int getItemCount(){
            return mBookList.size();
        }


    }
}
