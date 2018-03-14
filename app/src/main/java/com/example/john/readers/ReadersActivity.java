package com.example.john.readers;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.john.readers.util.ChangeButton;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.john.readers.MainFragment.mySpeechSynthesizer;

public class ReadersActivity extends FragmentActivity implements Chronometer.OnChronometerTickListener, SeekBar.OnSeekBarChangeListener, BaseFragment.OnFragmentInteractionListener {

    private InitBookList initBookList;

    private Fragment fragment;

    private LinearLayout mLinearLayout;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private ChangeButton mChangeButton;
    private VoicePlayer mVoicePlayer;
    private Book mBook;

    private Chronometer et_time;
    private TextView max_time;
    private SeekBar sb;
    private TelephonyManager manager;
    private SimpleDateFormat formatter;
    private String ms;

    /**
     * subtime:点击“续播”到暂停时的间隔的和 beginTime：重新回到播放时的bash值 falgTime：点击“播放”时的值
     * pauseTime：“暂停”时的值
     */
    public static long subtime = 0, beginTime = 0, falgTime = 0, pauseTime = 0;

    private long exitTime = 0;
    private FragmentManager fm;
    private Handler mMainFragmentHandler, mChapterListFragmentHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readers);

        manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        manager.listen(new MyListener(), PhoneStateListener.LISTEN_CALL_STATE);

        sb = (SeekBar) this.findViewById(R.id.activity_readers_seek_bar);
        et_time = (Chronometer) this.findViewById(R.id.et_time);
        max_time = (TextView) this.findViewById(R.id.max_time);

        sb.setEnabled(false);
        sb.setOnSeekBarChangeListener(this);
        et_time.setOnChronometerTickListener(this);

        updateData();

        mBook = (Book) getObject("book");
        mVoicePlayer = VoicePlayer.getInstance(mBook);

        fm = getSupportFragmentManager();
        fragment = fm.findFragmentById(R.id.fragment_container);

        mTitleTextView = (TextView) findViewById(R.id.activity_readers_text_view_title);
        mTitleTextView.setSingleLine();
        mAuthorTextView = (TextView) findViewById(R.id.activity_readers_text_view_author);
        mLinearLayout = (LinearLayout) findViewById(R.id.activity_readers_info);
        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBook != null) {
                    if (fm.getBackStackEntryCount() < 1) {
                        mySpeechSynthesizer.startSynthesis(mBook.getTitle() + "。作者" + mBook.getAuthor());
                        fm.beginTransaction()
                                .setCustomAnimations(R.anim.fragment_slide_left_enter,
                                        R.anim.fragment_slide_left_exit,
                                        R.anim.fragment_slide_right_enter,
                                        R.anim.fragment_slide_right_exit)
                                .replace(R.id.fragment_container, ChapterListFragment.newInstance(mBook))
                                .addToBackStack(null).commit();
                    } else if (!(ChapterListFragment.mCurrentBook != null && mBook.getUUID().equals(ChapterListFragment.mCurrentBook.getUUID()))) {
                        mySpeechSynthesizer.startSynthesis(mBook.getTitle() + "。作者" + mBook.getAuthor());
                        fm.beginTransaction()
                                .setCustomAnimations(R.anim.fragment_slide_left_enter,
                                        R.anim.fragment_slide_left_exit,
                                        R.anim.fragment_slide_right_enter,
                                        R.anim.fragment_slide_right_exit)
                                .replace(R.id.fragment_container, ChapterListFragment.newInstance(mBook))
                                .addToBackStack(null)
                                .commit();
                    }
                }
            }
        });

        setInfoTextView();

        mChangeButton = (ChangeButton) findViewById(R.id.btn_control);
        mChangeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mVoicePlayer != null) {
                    if (mVoicePlayer.getMediaPlayer() != null) {
                        setVoicePlayState();
                        pause();
                    } else {
                        play(mBook);
                    }
                }
            }
        });
    }

    @Override
    public void updateData() {
        UpdateTask updateTask = new UpdateTask();
        updateTask.execute();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fm.getBackStackEntryCount() < 1) {
                exit();
                return false;
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
            System.exit(0);
        }
    }

    private class MyListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (mVoicePlayer != null && mVoicePlayer.getMediaPlayer() != null) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        // 音乐播放器暂停
                        if (mVoicePlayer.getMediaPlayer().isPlaying()) {
                            pause();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // 重新播放音乐
                        if (!mVoicePlayer.getMediaPlayer().isPlaying() && mVoicePlayer.isPrePlayState()) {
                            pause();
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        // 音乐播放器暂停
                        if (mVoicePlayer.getMediaPlayer().isPlaying()) {
                            pause();
                        }
                        break;
                }
            }
        }
    }

    public void onChronometerTick(Chronometer chronometer) {

    }

    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // TODO 自动生成的方法存根
        if (fromUser == true && mVoicePlayer != null && mVoicePlayer.getMediaPlayer() != null) {
            mVoicePlayer.getMediaPlayer().seekTo(progress);
            falgTime = SystemClock.elapsedRealtime();
            beginTime = falgTime - sb.getProgress();
            et_time.setBase(beginTime);
            et_time.start();
        }

    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO 自动生成的方法存根

    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO 自动生成的方法存根

    }

    class UpdateTask extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try{
                String url = "http://139.159.234.95/";
//                String url = "http://10.42.0.116/";
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(url+"data.xml")
                        .build();
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                initBookList = new InitBookList(url,responseData);
            }catch (Exception e){
                return false;
            }
            return true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            BookLab.getInstance(initBookList.getBookList()).setBookList(initBookList.getBookList());

            //同步保存的mBook的ID
            for (Book book : BookLab.getInstance().getBookList()) {
                if (mBook != null && book.getTitle().equals(mBook.getTitle()) && book.getAuthor().equals(mBook.getAuthor())) {
                    mBook.setUUID(book.getUUID());
                }
            }

            if (fragment == null) {
                fragment = new MainFragment();
                fm.beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .commit();
            } else {
                if (mMainFragmentHandler != null) {
                    Message msg = new Message();
                    msg.what = 2;
                    mMainFragmentHandler.sendMessage(msg);
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    //从文件中获得当前播放的书籍信息
    private Object getObject(String name) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = this.openFileInput(name);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //将当前播放的书籍信息存储到文件中
    private void saveObject(String name,Book data) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = this.openFileOutput(name, MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void play(final Book book) {
        if (book != null && book.getChapterList() != null && book.getChapterList().size() > book.getCurrentChapterPosition()) {

            String path = book.getChapterList().get(book.getCurrentChapterPosition());
            if ("".equals(path)) {
                Toast.makeText(this, "路径不能为空", Toast.LENGTH_LONG).show();
                mySpeechSynthesizer.startSynthesis("路径不能为空");
                return;
            }
            try {
                falgTime = SystemClock.elapsedRealtime();
                mVoicePlayer = VoicePlayer.getInstance(book);
                mChangeButton.setIsStart(false);
                if (mVoicePlayer != null && mVoicePlayer.getMediaPlayer() != null) {
                    mVoicePlayer.getMediaPlayer().release();
                    mVoicePlayer.setMediaPlayer(null);
                }
                mVoicePlayer.setMediaPlayer(new MediaPlayer());
                mVoicePlayer.getMediaPlayer().setDataSource(path);
                Log.i("hahahavoice",mVoicePlayer.getMediaPlayer().isPlaying()+"");

                // 采用异步的方式
                mVoicePlayer.getMediaPlayer().prepareAsync();
                // 为播放器注册
                mVoicePlayer.getMediaPlayer().setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                    public void onPrepared(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        mVoicePlayer.getMediaPlayer().start();
                        mVoicePlayer.setPrePlayState(true);
                        sb.setMax(mVoicePlayer.getMediaPlayer().getDuration());

                        formatter = new SimpleDateFormat("mm:ss");//初始化Formatter的转换格式。
                        ms = formatter.format(mVoicePlayer.getMediaPlayer().getDuration());
                        max_time.setText(ms);
                        handler.post(updateThread);
                        sb.setEnabled(true);
                        int num = book.getCurrentChapterPosition() + 1;
                        mySpeechSynthesizer.startSynthesis("正在播放" + book.getTitle() + "第" + num + "集");

                    }
                });

                // 注册播放完毕后的监听事件
                mVoicePlayer.getMediaPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    public void onCompletion(MediaPlayer mp) {
                        mVoicePlayer.getMediaPlayer().release();
                        mVoicePlayer.setMediaPlayer(null);
                        mVoicePlayer.setPrePlayState(true);
                        et_time.setBase(SystemClock.elapsedRealtime());
                        et_time.start();
                        et_time.stop();
                        sb.setProgress(0);

                        clickToPlay(mBook, mBook.getCurrentChapterPosition() + 1);
                        if (mChapterListFragmentHandler != null) {
                            Message msg = new Message();
                            msg.what = 1;
                            mChapterListFragmentHandler.sendMessage(msg);
                        }

                    }
                });
                pauseTime = 0;
                et_time.setBase(falgTime);
                et_time.start();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "文件播放出现异常", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void pause() {
        // 判断音乐是否在播放
        mChangeButton.setIsStart(!mChangeButton.isStart());
        Log.i("choushabi", "haha");
        if (mVoicePlayer != null && mVoicePlayer.getMediaPlayer() != null && mVoicePlayer.getMediaPlayer().isPlaying()) {
            // 暂停音乐播放器
            mVoicePlayer.getMediaPlayer().pause();
            sb.setEnabled(false);
            et_time.stop();
            pauseTime = SystemClock.elapsedRealtime();
            mySpeechSynthesizer.startSynthesis("已暂停");
        } else if (mVoicePlayer != null && mVoicePlayer.getMediaPlayer() != null) {
            subtime += SystemClock.elapsedRealtime() - pauseTime;
            mVoicePlayer.getMediaPlayer().start();
            sb.setEnabled(true);
            beginTime = falgTime + subtime;
            et_time.setBase(beginTime);
            et_time.start();
            mySpeechSynthesizer.startSynthesis("正在播放");
        }
    }

    public Handler handler = new Handler();
    public Runnable updateThread = new Runnable() {
        public void run() {
            // 获得歌曲现在播放位置并设置成播放进度条的值
            if (mVoicePlayer != null && mVoicePlayer.getMediaPlayer() != null) {
                sb.setProgress(mVoicePlayer.getMediaPlayer().getCurrentPosition());
                // 每次延迟100毫秒再启动线程
                handler.postDelayed(updateThread, 100);
            }
        }
    };

    @Override
    public void clickToPlay(Book book, int position) {
        if (mBook != null && mBook.getUUID().equals(book.getUUID()) &&
                mBook.getCurrentChapterPosition() == position) {

            if (mVoicePlayer.getMediaPlayer() != null) {
                setVoicePlayState();
                pause();
            } else {
                play(mBook);
            }
        } else if (position >= 0 && position < book.getChapterList().size()){
            mBook = book;
            mBook.setCurrentChapterPosition(position);
            play(mBook);
            setInfoTextView();
            saveObject("book", (Book)mBook);
        }
    }

    @Override
    public Book getBook() {
        return mBook;
    }

    @Override
    public VoicePlayer getVoicePlayer() {
        return mVoicePlayer;
    }

    public void setInfoTextView() {
        if (mBook != null) {
            String[] chapters = mBook.getChapterList().get(mBook.getCurrentChapterPosition()).split("/");
            mTitleTextView.setText(mBook.getTitle() + " " + chapters[chapters.length - 1]);
            mAuthorTextView.setText(mBook.getAuthor());
        }
    }

    @Override
    public void setVoicePlayState() {
        if (!mChangeButton.isStart()) {
            mVoicePlayer.setPrePlayState(false);
        } else {
            mVoicePlayer.setPrePlayState(true);
        }
    }

    @Override
    public void setChapterListFragmentHandler(Handler handler) {
        mChapterListFragmentHandler = handler;
    }

    @Override
    public void setMainFragmentHandler(Handler handler) {
        mMainFragmentHandler = handler;
    }
}
