package com.apps.instadownloader;

import android.content.Intent;
import android.os.Bundle;

public class AppIntro extends com.github.paolorotolo.appintro.AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(SampleSlide.newInstance(R.layout.intro));
        addSlide(SampleSlide.newInstance(R.layout.intro2));
        addSlide(SampleSlide.newInstance(R.layout.intro3));
        addSlide(SampleSlide.newInstance(R.layout.intro4));
        addSlide(SampleSlide.newInstance(R.layout.intro5));
        addSlide(SampleSlide.newInstance(R.layout.intro6));
        showStatusBar(true);

        // Edit the color of the nav bar on Lollipop+ devices
//        setNavBarColor(Color.parseColor("#3F51B5"));

        // Turn vibration on and set intensity
        // NOTE: you will need to ask VIBRATE permission in Manifest if you haven't already
        setVibrate(true);
        setVibrateIntensity(30);

        // Animations -- use only one of the below. Using both could cause errors.
//        setFadeAnimation(); // OR
        setZoomAnimation(); // OR
//        setFlowAnimation(); // OR
//        setDepthAnimation(); // OR

        // Permissions -- takes a permission and slide number
//        askForPermissions(new String[]{Manifest.permission.}, 3);
    }

    @Override
    public void onSkipPressed() {
        callMain();
    }

    @Override
    public void onNextPressed() {
        // Do something when users tap on Next button.
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        callMain();
    }

    @Override
    public void onSlideChanged() {
        // Do something when slide is changed
    }

    public void callMain()
    {
        Intent intent = new Intent(AppIntro.this,HomeScreen.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        callMain();
        super.onBackPressed();
    }
}
