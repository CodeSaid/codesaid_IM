package com.im.fragment.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
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
import com.codesaid.lib_framework.cloud.CloudManager;
import com.codesaid.lib_framework.db.CallRecord;
import com.codesaid.lib_framework.db.LitePalHelper;
import com.im.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created By codesaid
 * On :2020-01-18
 * Package Name: com.im.fragment.chat
 * desc : call 聊天记录
 */
public class CallRecordFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.CHINA);

    private View item_empty_view;
    private RecyclerView mChatRecordView;
    private SwipeRefreshLayout mChatRecordRefreshLayout;
    private Disposable mDisposable;

    private List<CallRecord> mList = new ArrayList<>();
    private CommonAdapter<CallRecord> mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_record, null);
        initView(view);
        return view;
    }

    private void initView(View view) {
        item_empty_view = view.findViewById(R.id.item_empty_view);
        mChatRecordRefreshLayout = view.findViewById(R.id.mChatRecordRefreshLayout);
        mChatRecordView = view.findViewById(R.id.mChatRecordView);

        mChatRecordRefreshLayout.setOnRefreshListener(this);

        mChatRecordView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatRecordView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));

        mAdapter = new CommonAdapter<>(mList, new CommonAdapter.onBindDataListener<CallRecord>() {
            @Override
            public void onBindViewHolder(final CallRecord model, final CommonViewHolder holder, int type, int position) {
                String mediaType = "";
                if (model.getMediaType() == CallRecord.MEDIA_TYPE_AUDIO) {
                    mediaType = getString(R.string.text_chat_record_audio);
                } else if (model.getMediaType() == CallRecord.MEDIA_TYPE_VIDEO) {
                    mediaType = getString(R.string.text_chat_record_video);
                }
                String callStatus = "";
                if (model.getCallStatus() == CallRecord.CALL_STATUS_UN_ANSWER) {
                    callStatus = getString(R.string.text_call_record_un_answer);
                    holder.setImgResource(R.id.iv_status_icon, R.drawable.img_un_answer_icon);
                    holder.setTextColor(R.id.tv_nickname, Color.RED);
                    holder.setTextColor(R.id.tv_type, Color.RED);
                } else if (model.getCallStatus() == CallRecord.CALL_STATUS_DIAL) {
                    callStatus = getString(R.string.text_chat_record_dial);
                    holder.setImgResource(R.id.iv_status_icon, R.drawable.img_dial_icon);
                } else if (model.getCallStatus() == CallRecord.CALL_STATUS_ANSWER) {
                    callStatus = getString(R.string.text_chat_record_answer);
                    holder.setImgResource(R.id.iv_status_icon, R.drawable.img_answer_icon);
                }

                holder.setText(R.id.tv_type, mediaType + " " + callStatus);
                holder.setText(R.id.tv_time, dateFormat.format(model.getCallTime()));

                BmobManager.getInstance().queryObjectIdUser(model.getUserId(), new FindListener<IMUser>() {
                    @Override
                    public void done(List<IMUser> list, BmobException e) {
                        if (e == null) {
                            if (list != null && list.size() > 0) {
                                IMUser user = list.get(0);
                                holder.setText(R.id.tv_nickname, user.getNickName());
                            }
                        }
                    }
                });

                // 点击通话记录 直接重新拨打
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (model.getMediaType() == CallRecord.MEDIA_TYPE_AUDIO) {
                            CloudManager.getInstance().startAudioCall(getActivity(), model.getUserId());
                        } else if (model.getMediaType() == CallRecord.MEDIA_TYPE_VIDEO) {
                            CloudManager.getInstance().startVideoCall(getActivity(), model.getUserId());
                        }
                    }
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_call_record;
            }
        });

        mChatRecordView.setAdapter(mAdapter);
    }

    @Override
    public void onRefresh() {
        if (mChatRecordRefreshLayout.isRefreshing()) {
            queryCallRecord();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        queryCallRecord();
    }

    /**
     * 查询通话记录
     */
    private void queryCallRecord() {
        mChatRecordRefreshLayout.setRefreshing(true);

        mDisposable = Observable.create(new ObservableOnSubscribe<List<CallRecord>>() {
            @Override
            public void subscribe(ObservableEmitter<List<CallRecord>> emitter) throws Exception {
                emitter.onNext(LitePalHelper.getInstance().queryCallRecord());
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<CallRecord>>() {
                    @Override
                    public void accept(List<CallRecord> callRecords) throws Exception {
                        mChatRecordRefreshLayout.setRefreshing(false);

                        if (callRecords != null) {
                            if (mList.size() > 0) {
                                mList.clear();
                            }

                            mList.addAll(callRecords);
                            mAdapter.notifyDataSetChanged();

                            item_empty_view.setVisibility(View.GONE);
                            mChatRecordView.setVisibility(View.VISIBLE);
                        } else {
                            // 无数据
                            item_empty_view.setVisibility(View.VISIBLE);
                            mChatRecordView.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposable != null) {
            if (mDisposable.isDisposed()) {
                mDisposable.dispose();
            }
        }
    }
}
