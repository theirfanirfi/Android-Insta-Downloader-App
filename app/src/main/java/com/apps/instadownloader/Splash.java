package com.apps.instadownloader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class Splash extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("isFirst", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isFirst = sharedPreferences.getBoolean("first",true);

        setStatusColor();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isFirst) {
                    callAppIntro();
                } else {
                    callMain();
                }
            }
        },2000);
    }

    public void setStatusColor()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    public void callAppIntro()
    {
        Intent intent = new Intent(Splash.this, AppIntro.class);
        startActivity(intent);
        finish();
        editor.putBoolean("first",false);
        editor.commit();
    }

    public void callMain()
    {
        Intent intent = new Intent(Splash.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
