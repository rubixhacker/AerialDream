package com.hackedbuce.aerialdream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.service.dreams.DreamService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.hackedbuce.aerialdream.data.Assest;
import com.hackedbuce.aerialdream.data.Videos;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by rubix on 1/10/2016.
 */
public class AerialDream extends DreamService implements SurfaceHolder.Callback, MediaPlayer.OnPreparedListener {

    DateFormat dateFormat = new SimpleDateFormat("h:mm", Locale.getDefault());

    AerialService mAerialService;
    PublishSubject<Void> mNextVideoSubject = PublishSubject.create();
    SurfaceView mSurfaceView;
    MediaPlayer mMediaPlayer;
    TextView mTime;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                updateTime();
            }
        }
    };

    TextView mLocation;
    Random mRandom = new Random();
    Subscription mSubscription;
    IntentFilter mIntentFilter;

    @Override
    public void onCreate() {
        super.onCreate();

        registerForTimeUpdates();
        initService();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setFullscreen(true);
        setInteractive(true);
        initVideoRequestSubscription();
        mMediaPlayer = new MediaPlayer();

    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        setContentView(R.layout.dream_aerial);
        mSurfaceView = (SurfaceView) findViewById(R.id.video);
        mTime = (TextView) findViewById(R.id.time);
        mLocation = (TextView) findViewById(R.id.location);

        updateTime();

        mSurfaceView.getHolder().addCallback(this);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mNextVideoSubject.onNext(null);
            }
        });

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
        mNextVideoSubject.onNext(null);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        mp.start();
    }

    private void initService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://a1.phobos.apple.com/us/r1000/000/Features/atv/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        mAerialService = retrofit.create(AerialService.class);
    }

    private void registerForTimeUpdates() {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mNextVideoSubject.onNext(null);
        }
        return super.dispatchTouchEvent(event);

    }

    private void initVideoRequestSubscription() {
        mSubscription = mNextVideoSubject.asObservable()
                .doOnNext(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {

                    }
                })
                .flatMap(new Func1<Void, Observable<List<Videos>>>() {
                    @Override
                    public Observable<List<Videos>> call(Void aVoid) {
                        return mAerialService.getVideos("AutumnResources")
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread());
                    }
                })
                .map(new Func1<List<Videos>, Assest>() {
                    @Override
                    public Assest call(List<Videos> videos) {
                        Videos selectedVideo = selectRandomVideo(videos);
                        return selectRandomAsset(selectedVideo.getAssests());
                    }
                })
                .subscribe(new Action1<Assest>() {
                    @Override
                    public void call(Assest assest) {
                        Uri uri = Uri.parse(assest.getUrl());

                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.release();
                            mMediaPlayer = new MediaPlayer();
                            mMediaPlayer.setDisplay(mSurfaceView.getHolder());
                        }

                        try {
                            mMediaPlayer.setOnPreparedListener(AerialDream.this);
                            mMediaPlayer.setDataSource(getApplicationContext(), uri);
                            mMediaPlayer.prepare();
                            mLocation.setText(assest.getAccessibilityLabel());
                        } catch (IOException e) {
                            e.printStackTrace();
                            mNextVideoSubject.onNext(null);
                        }
                    }
                });
    }

    private Assest selectRandomAsset(List<Assest> assests) {
        return assests.get(mRandom.nextInt(assests.size()));
    }

    private Videos selectRandomVideo(List<Videos> videos) {
        return videos.get(mRandom.nextInt(videos.size()));
    }

    private void updateTime() {
        mTime.setText(dateFormat.format(new Date()));
    }
}
