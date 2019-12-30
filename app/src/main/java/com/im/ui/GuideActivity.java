package com.im.ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.codesaid.lib_framework.base.BasePagerAdapter;
import com.codesaid.lib_framework.base.BaseUIActivity;
import com.codesaid.lib_framework.mediaplayer.MediaPlayerManager;
import com.codesaid.lib_framework.utils.anim.AnimUtils;
import com.im.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created By codesaid
 * On :2019-12-24
 * Package Name: com.im.ui
 * desc : 引导页面
 */
public class GuideActivity extends BaseUIActivity implements View.OnClickListener {

    private ImageView iv_music_switch;
    private TextView tv_guide_skip;
    private ImageView iv_guide_point_1;
    private ImageView iv_guide_point_2;
    private ImageView iv_guide_point_3;
    private ViewPager mViewPager;

    /**
     * 1.ViewPager : 适配器|帧动画播放
     * 2.小圆点的逻辑
     * 3.歌曲的播放
     * 4.属性动画旋转
     * 5.跳转
     */

    private View viewOne;
    private View viewTwo;
    private View viewThree;

    private List<View> mViewList = new ArrayList<>();
    private BasePagerAdapter mPagerAdapter;

    private ImageView iv_guide_star;
    private ImageView iv_guide_night;
    private ImageView iv_guide_smile;

    private MediaPlayerManager mPlayerManager;
    private ObjectAnimator mAnimator;

    // ViewPager 当前页面的 position
    private int currentPage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_layout);

        initView();
        initData();
    }

    private void initView() {
        iv_music_switch = findViewById(R.id.iv_music_switch);
        tv_guide_skip = findViewById(R.id.tv_guide_skip);
        iv_guide_point_1 = findViewById(R.id.iv_guide_point_1);
        iv_guide_point_2 = findViewById(R.id.iv_guide_point_2);
        iv_guide_point_3 = findViewById(R.id.iv_guide_point_3);
        mViewPager = findViewById(R.id.mViewPager);

        iv_music_switch.setOnClickListener(this);
        tv_guide_skip.setOnClickListener(this);

        viewOne = View.inflate(this, R.layout.layout_pager_guide_1, null);
        viewTwo = View.inflate(this, R.layout.layout_pager_guide_2, null);
        viewThree = View.inflate(this, R.layout.layout_pager_guide_3, null);

        // 帧动画
        iv_guide_star = viewOne.findViewById(R.id.iv_guide_star);
        iv_guide_night = viewTwo.findViewById(R.id.iv_guide_night);
        iv_guide_smile = viewThree.findViewById(R.id.iv_guide_smile);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initData() {
        mViewList.add(viewOne);
        mViewList.add(viewTwo);
        mViewList.add(viewThree);

        // 预加载
        mViewPager.setOffscreenPageLimit(mViewList.size());

        mPagerAdapter = new BasePagerAdapter(mViewList);
        mViewPager.setAdapter(mPagerAdapter);

        // 播放帧动画
        AnimationDrawable animStar = (AnimationDrawable) iv_guide_star.getBackground();
        animStar.start();

        AnimationDrawable animNight = (AnimationDrawable) iv_guide_night.getBackground();
        animNight.start();

        AnimationDrawable animSmile = (AnimationDrawable) iv_guide_smile.getBackground();
        animSmile.start();

        // 小圆点逻辑
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                selectPoint(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //设置ViewPager的滑动监听,为了滑动到最后一页,继续滑动实现页面的跳转
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            float startX;

            float endX;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取按下的位置
                        startX = motionEvent.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        // 获取抬起的位置
                        endX = motionEvent.getX();

                        WindowManager windowManager = (WindowManager) getApplicationContext()
                                .getSystemService(Context.WINDOW_SERVICE);
                        //获取屏幕的宽度
                        Point size = new Point();
                        windowManager.getDefaultDisplay().getSize(size);
                        int width = size.x;

                        // 首先要确定的是，是否到了最后一页
                        // 然后判断是否向左滑动，并且滑动距离是否符合
                        if ((mViewList.size() - 1 == currentPage) && (startX - endX > (width / 5))) {
                            startActivity(new Intent(GuideActivity.this, LoginActivity.class));
                            finish();
                        }
                        break;
                }
                return false;
            }
        });

        // 播放歌曲
        playMusic();
    }

    /**
     * 部分歌曲
     */
    private void playMusic() {
        mPlayerManager = new MediaPlayerManager();
        mPlayerManager.setLooping(true);
        AssetFileDescriptor path = getResources().openRawResourceFd(R.raw.guide);
        mPlayerManager.startPlay(path);

        // 设置旋转动画
        mAnimator = AnimUtils.rotation(iv_music_switch);
        mAnimator.start();
    }

    /**
     * 动态选择小圆点
     *
     * @param position view position
     */
    private void selectPoint(int position) {
        switch (position) {
            case 0:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point_p);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point);
                break;
            case 1:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point_p);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point);
                break;
            case 2:
                iv_guide_point_1.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_2.setImageResource(R.drawable.img_guide_point);
                iv_guide_point_3.setImageResource(R.drawable.img_guide_point_p);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_music_switch:
                if (mPlayerManager.MEDIA_STATUS_CURRENT == MediaPlayerManager.MEDIA_STATUS_PAUSE) {
                    // 当前是暂停状态
                    mAnimator.start();
                    mPlayerManager.continuePlay();
                    iv_music_switch.setImageResource(R.drawable.img_guide_music);
                } else if (mPlayerManager.MEDIA_STATUS_CURRENT == MediaPlayerManager.MEDIA_STATUS_PLAY) {
                    // 当前是播放状态
                    mAnimator.pause();
                    mPlayerManager.pausePlay();
                    iv_music_switch.setImageResource(R.drawable.img_guide_music_off);
                }
                break;
            case R.id.tv_guide_skip:
                // 跳转到 LoginActivity
                startActivity(new Intent(GuideActivity.this, LoginActivity.class));
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerManager.stopPlay();
    }
}
