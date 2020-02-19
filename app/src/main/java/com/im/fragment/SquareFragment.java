package com.im.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codesaid.lib_framework.adapter.CommonAdapter;
import com.codesaid.lib_framework.adapter.CommonViewHolder;
import com.codesaid.lib_framework.base.BaseFragment;
import com.codesaid.lib_framework.bmob.BmobManager;
import com.codesaid.lib_framework.bmob.IMUser;
import com.codesaid.lib_framework.bmob.SquareSet;
import com.codesaid.lib_framework.helper.FileHelper;
import com.codesaid.lib_framework.mediaplayer.MediaPlayerManager;
import com.codesaid.lib_framework.view.VideoJzvdStd;
import com.im.R;
import com.im.ui.ImagePreviewActivity;
import com.im.ui.PushSquareActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.jzvd.Jzvd;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created By codesaid
 * On :2020-01-02
 * Package Name: com.im.fragment
 */
public class SquareFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    /**
     * 1.设计并且实现云数据库 SquareSet
     * 2.实现我们的媒体发送 PsuhSquareActivity
     * 3.实现列表 并且实现我们的文本和图片的发送
     */

    private static final int REQUEST_CODE = 1024;

    private ImageView iv_push;
    private RecyclerView mSquareView;
    private SwipeRefreshLayout mSquareSwipeLayout;
    private View item_empty_view;

    private List<SquareSet> mList = new ArrayList<>();
    private CommonAdapter<SquareSet> mSquareAdapter;

    private SimpleDateFormat dateFormat;

    //播放
    private MediaPlayerManager mMusicManager;
    //音乐是否在播放
    private boolean isMusicPlay = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_square, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        mMusicManager = new MediaPlayerManager();
        mMusicManager.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                isMusicPlay = false;
            }
        });

        iv_push = view.findViewById(R.id.iv_push);
        mSquareView = view.findViewById(R.id.mSquareView);
        mSquareSwipeLayout = view.findViewById(R.id.mSquareSwipeLayout);
        item_empty_view = view.findViewById(R.id.item_empty_view);

        iv_push.setOnClickListener(this);
        mSquareSwipeLayout.setOnRefreshListener(this);
        mSquareView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSquareView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mSquareAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindMoreDataListener<SquareSet>() {
            @Override
            public int getItemType(int position) {
                return position;
            }

            @Override
            public void onBindViewHolder(final SquareSet model, final CommonViewHolder holder, int type, int position) {
                // 加载个人信息
                BmobManager.getInstance().queryObjectIdUser(model.getUserId(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if (e == null) {
                            if (list != null && list.size() > 0) {
                                IMUser user = list.get(0);
                                holder.setImgUrl(getActivity(), R.id.iv_photo, user.getPhoto());
                                holder.setText(R.id.tv_nickname, user.getNickName());
                            }
                        }
                    }
                });
                // 设置时间
                holder.setText(R.id.tv_time, dateFormat.format(model.getPushTime()));
                // 设置文本
                if (!TextUtils.isEmpty(model.getText())) {
                    holder.setText(R.id.tv_text, model.getText());
                } else {
                    holder.getView(R.id.tv_text).setVisibility(View.GONE);
                }
                // 多媒体
                switch (model.getPush_type()) {
                    case SquareSet.PUSH_TEXT:
                        goneItemView(holder, false, false, false);
                        break;
                    case SquareSet.PUSH_IMAGE: // 图片
                        goneItemView(holder, true, false, false);
                        holder.setImgUrl(getActivity(), R.id.iv_img, model.getMediaUrl());
                        holder.getView(R.id.iv_img).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ImagePreviewActivity.startActivity(getActivity(), true, model.getMediaUrl());
                            }
                        });
                        break;
                    case SquareSet.PUSH_MUSIC:
                        goneItemView(holder, false, true, false);

                        holder.getView(R.id.ll_music).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // 播放音乐
                                if (mMusicManager.isPlaying()) { // 判断是否正在播放
                                    mMusicManager.pausePlay();
                                } else {
                                    if (isMusicPlay) {
                                        mMusicManager.continuePlay();
                                    } else {
                                        mMusicManager.startPlay(model.getMediaUrl());
                                        isMusicPlay = true;
                                    }
                                }
                            }
                        });
                        break;
                    case SquareSet.PUSH_VIDEO:
                        goneItemView(holder, false, false, true);
                        holder.getView(R.id.tv_text).setVisibility(View.GONE);

                        // 视频 播放
                        VideoJzvdStd videoJzvdStd = holder.getView(R.id.jz_video);
                        videoJzvdStd.setUp(model.getMediaUrl(), model.getText());

                        Observable.create(new ObservableOnSubscribe<Bitmap>() {
                            @Override
                            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                                Bitmap mBitmap = FileHelper.getInstance()
                                        .getNetVideoBitmap(model.getMediaUrl());
                                if (mBitmap != null) {
                                    emitter.onNext(mBitmap);
                                    emitter.onComplete();
                                }
                            }
                        }).subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(bitmap -> {
                                    if (bitmap != null) {
                                        videoJzvdStd.thumbImageView.setImageBitmap(bitmap);
                                    }
                                });
                        break;
                }
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_square_item;
            }
        });

        mSquareView.setAdapter(mSquareAdapter);

        // 加载数据
        loadSquare();
    }

    private void goneItemView(CommonViewHolder holder,
                              boolean img, boolean audio, boolean video) {
        holder.getView(R.id.tv_text).setVisibility(View.VISIBLE);
        holder.getView(R.id.iv_img).setVisibility(img ? View.VISIBLE : View.GONE);
        holder.getView(R.id.ll_music).setVisibility(audio ? View.VISIBLE : View.GONE);
        holder.getView(R.id.ll_video).setVisibility(video ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_push:
                Intent intent = new Intent(getActivity(), PushSquareActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE) {
                // 刷新
                loadSquare();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 加载 圈子数据
     */
    public void loadSquare() {
        mSquareSwipeLayout.setRefreshing(true);
        BmobManager.getInstance().querySquareSet(new FindListener<SquareSet>() {
            @Override
            public void done(List<SquareSet> list, BmobException e) {
                mSquareSwipeLayout.setRefreshing(false);
                if (e == null) {
                    if (list != null && list.size() > 0) {
                        //倒序
                        Collections.reverse(list);
                        mSquareView.setVisibility(View.VISIBLE);
                        item_empty_view.setVisibility(View.GONE);

                        if (mList.size() > 0) {
                            mList.clear();
                        }
                        mList.addAll(list);
                        mSquareAdapter.notifyDataSetChanged();
                    } else {
                        mSquareView.setVisibility(View.GONE);
                        item_empty_view.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        loadSquare();
    }

    @Override
    public void onPause() {
        super.onPause();
        Jzvd.releaseAllVideos();
    }
}
