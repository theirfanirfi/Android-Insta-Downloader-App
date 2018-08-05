package com.apps.instadownloader;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.utils.Constants;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.onesignal.OneSignal;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdDisplayListener;
import com.startapp.android.publish.AdEventListener;
import com.startapp.android.publish.StartAppAd;
import com.startapp.android.publish.StartAppSDK;
import com.startapp.android.publish.nativead.NativeAdDetails;
import com.startapp.android.publish.nativead.NativeAdPreferences;
import com.startapp.android.publish.nativead.StartAppNativeAd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import static com.apps.instadownloader.R.drawable.openactive;

public class HomeScreen extends AppCompatActivity {
    private StartAppAd startAppAd = new StartAppAd(this);
    StartAppNativeAd startAppNativeAd = new StartAppNativeAd(this);
    StartAppAd b;
    Switch startIservice;
    Button oi, idownloads;
    AdView mAdView1,mAdView2;
    TextView howt;
    ImageView iv1,iv2,iv3,idi;
    Toolbar toolbar;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    LinearLayout adlayout;
    boolean isStarted = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            StartAppSDK.init(HomeScreen.this, getString(R.string.startapp_app_id), getString(R.string.iSaveDevId), true);
            StartAppAd.showSplash(this, savedInstanceState);
            StartAppAd.showSlider(this);

        MobileAds.initialize(this,"ca-app-pub-5657492175984707~5196976272");

        setContentView(R.layout.activity_home_screen);
        howt = (TextView) findViewById(R.id.howto);
            toolbar = (Toolbar) findViewById(R.id.toolbar3);
            setSupportActionBar(toolbar);
            toolbar.setTitle("iSave");
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setNavigationIcon(R.drawable.hrt);
        idi = (ImageView) findViewById(R.id.idicon);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                    if (launchIntent != null)
                    {
                        try
                        {
                            if(sharedPreferences.getBoolean("isauto",false)) {
                                startActivity(launchIntent);
                            }
                            else
                            {
                                Toast.makeText(HomeScreen.this,"First start the iSave service by turning on the switch.",Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (ActivityNotFoundException ex) // in case Instagram not installed in your device
                        {
                            Toast.makeText(HomeScreen.this, ex.toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        Toast.makeText(HomeScreen.this,"Instagram is not installed in your device",Toast.LENGTH_LONG).show();
                    }
                }
            });




        iv1 = (ImageView) findViewById(R.id.iv1);
        iv2 = (ImageView) findViewById(R.id.iv2);
        iv3 = (ImageView) findViewById(R.id.iv3);
       mAdView1 = (AdView) findViewById(R.id.adView);
       mAdView2 = (AdView) findViewById(R.id.adVieww);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);
        AdRequest adRequest2 = new AdRequest.Builder().build();
       mAdView2.loadAd(adRequest2);
        startIservice = (Switch) findViewById(R.id.switchh);
        oi = (Button) findViewById(R.id.openinstagram);
        idownloads = (Button) findViewById(R.id.idown);


        sharedPreferences = getSharedPreferences("auto_d", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(sharedPreferences.getBoolean("isauto",false))
        {
            startIservice.setChecked(true);
            idi.setImageResource(R.drawable.app_icon_normal);
            idi.setMaxHeight(80);
            idi.setMaxWidth(80);
            oi.setBackground(getDrawable(R.drawable.open_instagram_unpressed));
        }
        else
        {
            startIservice.setChecked(false);
            idi.setImageResource(R.drawable.idicon);
            idi.setMaxWidth(100);
            idi.setMaxHeight(100);
            oi.setBackground(getDrawable(R.drawable.openinsta));

        }



        startIservice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    try {
                        Intent intent = new Intent(HomeScreen.this, DownloadService.class)
                                .setAction(DownloadService.ACTION_START);
                        startService(intent);
                        Constants.isAutoDownload = true;
                        editor.putBoolean("isauto", Constants.isAutoDownload);
                        editor.commit();
                        oi.setBackground(getDrawable(R.drawable.openactive));
                        isStarted = true;
                        idi.setImageResource(R.drawable.app_icon_normal);
                        oi.setBackground(getDrawable(R.drawable.open_instagram_unpressed));
                        idi.setMaxHeight(80);
                        idi.setMaxWidth(80);
                        Toast.makeText(HomeScreen.this, "iSave service Started",Toast.LENGTH_LONG).show();
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(HomeScreen.this, e.toString(),Toast.LENGTH_LONG).show();
                    }

                }
                else
                {
                    Intent intent = new Intent(HomeScreen.this, DownloadService.class)
                            .setAction(DownloadService.ACTION_STOP);
                    stopService(intent);
                    Constants.isAutoDownload = false;
                    editor.putBoolean("isauto", Constants.isAutoDownload);
                    editor.commit();
                    oi.setBackground(getDrawable(R.drawable.openinsta));
                    isStarted = false;
                    idi.setImageResource(R.drawable.idicon);
                    idi.setMaxWidth(100);
                    idi.setMaxHeight(100);
                    oi.setBackground(getDrawable(R.drawable.openinsta));
                    Toast.makeText(HomeScreen.this, "iSave service Stopped",Toast.LENGTH_LONG).show();
                }
            }
        });

        oi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
        if (launchIntent != null)
        {
            try
            {
                if(sharedPreferences.getBoolean("isauto",false)) {
                    startActivity(launchIntent);
                }
                else
                {
                    Toast.makeText(HomeScreen.this,"First start the iSave service by turning on the switch.",Toast.LENGTH_LONG).show();
                }
            }
            catch (ActivityNotFoundException ex) // in case Instagram not installed in your device
            {
                Toast.makeText(HomeScreen.this, ex.toString(),Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(HomeScreen.this,"Instagram is not installed in your device",Toast.LENGTH_LONG).show();
        }
            }
        });


        idownloads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent d = new Intent(HomeScreen.this, Downloads.class);
                startActivity(d);
            }
        });

        howt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent h = new Intent(HomeScreen.this,Howto.class);
                startActivity(h);
            }
        });

        startAppNativeAd.loadAd(new NativeAdPreferences().setAdsNumber(3).setAutoBitmapDownload(true).setImageSize(NativeAdPreferences.NativeAdBitmapSize.SIZE72X72), new AdEventListener() {
            @Override
            public void onReceiveAd(Ad ad) {
                ArrayList<NativeAdDetails> ads = startAppNativeAd.getNativeAds();    // get NativeAds list

                // Print all ads details to log
                Iterator<NativeAdDetails> iterator = ads.iterator();
                int x = 1;
                while(iterator.hasNext()){
                    if(x == 1)
                    {
                        iv1.setImageBitmap(iterator.next().getImageBitmap());
                        x++;
                    }
                    else if(x == 2)
                    {
                        iv2.setImageBitmap(iterator.next().getImageBitmap());

                        x++;
                    }
                    else if(x==3)
                    {
                        iv3.setImageBitmap(iterator.next().getImageBitmap());

                        x = 1;
                    }

                }

            }

            @Override
            public void onFailedToReceiveAd(Ad ad) {
               // Toast.makeText(HomeScreen.this,ad.toString(),Toast.LENGTH_LONG).show();

            }




        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.of,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.of:
                Intent dow = new Intent(HomeScreen.this,Downloads.class);
                startActivity(dow);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        startAppAd.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAppAd.onResume();
    }

    @Override
    public void onBackPressed() {
        startAppAd.onBackPressed();
        super.onBackPressed();
    }

}
