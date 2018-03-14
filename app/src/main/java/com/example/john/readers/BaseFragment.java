package com.example.john.readers;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    protected OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
        else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name

        // 接口回调，让Activity实现这些方法，子Fragment可以进行调用，实现数据Activity和Fragment之间的交互

        Book getBook();                                                 // 获取Activity的Book实例
        VoicePlayer getVoicePlayer();                                   // 获取Activity的VoicePlayer实例
        void setVoicePlayState();                                       // 设置播放状态，可以为播放或暂停的选择提供条件判断
        void pause();                                                   // 暂停操作
        void play(Book book);                                           // 播放操作
        void clickToPlay(Book book, int position);                      // 在Acitivity中实现该方法可以实现播放或暂停，在Fragment中均有调用
        void setMainFragmentHandler(Handler handler);                   // 接收Activity发送过来的刷新数据成功的消息，刷新列表视图
        void setChapterListFragmentHandler(Handler handler);            // 接收Activity中播放结束跳到下一集的消息，刷新ChapterListFragment的列表
        void updateData();              // 执行联网操作，装载数据
    }
}
