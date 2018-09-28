package ru.uj.sunset;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by Blokhin Evgeny on 27.09.2018.
 */
public class SunsetFragment extends Fragment {

    private boolean mSunset;
    private View mSceneView;
    private View mSunView;
    private View mSkyView;
    private View mReflectionView;

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;
    private int mHotSunColor;
    private int mColdSunColor;
    private int mSunsetSkyColorCurrent;
    private int mNightSkyColorCurrent;
    private float mSunYCurrent;
    private float mReflectionYCurrent;
    private AnimatorSet mSunriseAnimatorSet;
    private AnimatorSet mSunsetAnimatorSet;
    private String TAG = "Sunset";

    public static final int DURATION = 3000;

    public static SunsetFragment newInstance() {
        return new SunsetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sunset, container, false);

        mSceneView = view;
        mSunView = view.findViewById(R.id.sun);
        mSkyView = view.findViewById(R.id.sky);
        mReflectionView = view.findViewById(R.id.reflection);
        mSunset = true;

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);
        mHotSunColor = resources.getColor(R.color.heat_sun);
        mColdSunColor = resources.getColor(R.color.cold_sun);
        mSunsetSkyColorCurrent = mBlueSkyColor;
        mNightSkyColorCurrent = mSunsetSkyColor;


        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSunset) {
                    startSunsetAnimation();
                    if (mSunriseAnimatorSet != null) {
                        mSunriseAnimatorSet.end();
                        mSunriseAnimatorSet = null;
                    }
                } else {
                    startSunriseAnimation();
                    if (mSunsetAnimatorSet != null) {
                        mSunsetAnimatorSet.end();
                        mSunsetAnimatorSet = null;
                    }
                }

                mSunset = !mSunset;
                startSunHeatAnimation();
            }
        });

        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSunYCurrent = mSunView.getTop();
                mReflectionYCurrent = mReflectionView.getTop();
            }
        });

        return view;
    }

    private void startSunsetAnimation() {
        long duration = (long) (DURATION / (mSkyView.getHeight() - mSunView.getTop()) * (mSkyView.getHeight() - mSunYCurrent));

        ObjectAnimator sunHeightAnimator = ObjectAnimator
                .ofFloat(mSunView, "y", mSunYCurrent, mSkyView.getHeight())
                .setDuration(duration);
        sunHeightAnimator.setInterpolator(new AccelerateInterpolator());

        sunHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSunYCurrent = (float) animation.getAnimatedValue();
            }
        });

        ObjectAnimator reflectionHeightAnimator = ObjectAnimator.ofFloat(mReflectionView, "y", mReflectionYCurrent, -mReflectionView.getHeight())
                .setDuration(duration);
        reflectionHeightAnimator.setInterpolator(new AccelerateInterpolator());
        reflectionHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mReflectionYCurrent = (float) animation.getAnimatedValue();
            }
        });

        ObjectAnimator sunsetSkyAnimator = ObjectAnimator
                .ofObject(mSkyView, "backgroundColor", new ArgbEvaluator(), mSunsetSkyColorCurrent, mSunsetSkyColor)
                .setDuration(duration);
        sunsetSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSunsetSkyColorCurrent = (int) animation.getAnimatedValue();
            }
        });

        ObjectAnimator nightSkyAnimator = ObjectAnimator.ofObject(mSkyView, "backgroundColor",
                new ArgbEvaluator(), mNightSkyColorCurrent, mNightSkyColor)
                .setDuration(DURATION);

        nightSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNightSkyColorCurrent = (int) animation.getAnimatedValue();
            }
        });

        mSunsetAnimatorSet = new AnimatorSet();
        mSunsetAnimatorSet
                .play(sunHeightAnimator)
                .with(reflectionHeightAnimator)
                .with(sunsetSkyAnimator)
                .before(nightSkyAnimator);

        mSunsetAnimatorSet.start();
    }

    private void startSunHeatAnimation() {
        ObjectAnimator sunRayAnimator = ObjectAnimator.ofFloat(mSunView, "rotation", 0, 23)
                .setDuration(500);
        sunRayAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        sunRayAnimator.setRepeatCount(ObjectAnimator.INFINITE);


        ObjectAnimator sunHeatAnimator = ObjectAnimator.ofInt(mSunView, "tint", mHotSunColor, mColdSunColor)
                .setDuration(500);
        sunHeatAnimator.setEvaluator(new ArgbEvaluator());
        sunHeatAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        sunHeatAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(sunRayAnimator).with(sunHeatAnimator);
        animatorSet.start();
    }

    private void startSunriseAnimation() {
        long duration = (long) (DURATION / (mSkyView.getHeight() - mSunView.getTop()) * (mSunYCurrent - mSunView.getTop()));
        long nightDuration = (long) (DURATION * ((double) (mSunsetSkyColor - mNightSkyColorCurrent) / (double) (mSunsetSkyColor - mNightSkyColor)));

        ObjectAnimator sunHeightAnimator = ObjectAnimator.ofFloat(mSunView, "y", mSunYCurrent, mSunView.getTop())
                .setDuration(duration);

        sunHeightAnimator.setInterpolator(new DecelerateInterpolator());

        sunHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSunYCurrent = (float) animation.getAnimatedValue();
            }
        });


        ObjectAnimator reflectionHeightAnimator = ObjectAnimator.ofFloat(mReflectionView, "y", mReflectionYCurrent, mReflectionView.getTop())
                .setDuration(duration);
        reflectionHeightAnimator.setInterpolator(new DecelerateInterpolator());
        reflectionHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mReflectionYCurrent = (float) animation.getAnimatedValue();
            }
        });


        ObjectAnimator daySkyAnimator = ObjectAnimator.ofObject(mSkyView, "backgroundColor", new ArgbEvaluator(), mSunsetSkyColorCurrent, mBlueSkyColor)
                .setDuration(duration);
        daySkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSunsetSkyColorCurrent = (int) animation.getAnimatedValue();
            }
        });


        ObjectAnimator nightSkyAnimator = ObjectAnimator.ofObject(mSkyView, "backgroundColor", new ArgbEvaluator(), mNightSkyColorCurrent, mSunsetSkyColor)
                .setDuration(nightDuration);

        nightSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mNightSkyColorCurrent = (int) animation.getAnimatedValue();
            }
        });

        mSunriseAnimatorSet = new AnimatorSet();
        mSunriseAnimatorSet
                .play(sunHeightAnimator)
                .with(reflectionHeightAnimator)
                .with(daySkyAnimator)
                .after(nightSkyAnimator);

        mSunriseAnimatorSet.start();
    }
}
