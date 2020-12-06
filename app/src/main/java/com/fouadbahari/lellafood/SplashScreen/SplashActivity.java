package com.fouadbahari.lellafood.SplashScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.fouadbahari.lellafood.MainActivity;
import com.fouadbahari.lellafood.R;

import pl.droidsonroids.gif.GifImageView;

public class SplashActivity extends AppCompatActivity {

    private GifImageView gifImageView;
    private RelativeLayout relativeLayout;
    private static final int NUM_PAGER = 3;
    private ViewPager viewPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private Animation animation;

    private static int SPLASH_TIME_OUT = 1500;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sharedPreferences = getSharedPreferences("SharedPref",MODE_PRIVATE);
                boolean isFirstTime = sharedPreferences.getBoolean("fistTime",true);
                  if (isFirstTime)
                {
                    setContentView(R.layout.activity_splash);
                    gifImageView = findViewById(R.id.benna_gif);
                    viewPager = findViewById(R.id.pager);
                    relativeLayout = findViewById(R.id.layout_gif);
                    pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
                    viewPager.setAdapter(pagerAdapter);

                    animation = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.animation_duration);
                    viewPager.setAnimation(animation);

                    gifImageView.animate().translationY(-1800).setDuration(4000).setStartDelay(4000);
                    relativeLayout.animate().translationY(-1800).setDuration(4000).setStartDelay(4000);


                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("fistTime",false);
                    editor.commit();

                    //go to on boarding activity
                }
                else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        },SPLASH_TIME_OUT);
    }
}