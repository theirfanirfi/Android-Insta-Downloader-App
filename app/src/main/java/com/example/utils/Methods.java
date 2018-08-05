package com.example.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.apps.instadownloader.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;

public class Methods {

    Context context;

    public Methods(Context context){
        this.context = context;
    }

    public void showHideEmptyView(View view,View recycle, int count){
        if(count > 0) {
            view.setVisibility(View.GONE);
            recycle.setVisibility(View.VISIBLE);
        } else {
            recycle.setVisibility(View.GONE);
            view.setVisibility(View.VISIBLE);
        }
    }

    public void openItem(String path){
        File file = new File(path);
        if(file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = file.getName().substring(file.getName().indexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);
            intent.setDataAndType(Uri.fromFile(file), type);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "File Not Found", Toast.LENGTH_SHORT).show();
        }
    }

    public void shareItem(String path,String type){
        File file = new File(path);
        if(file.exists()) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            if(type.equals("image")) {
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM,Uri.parse("file://" + path));
            } else {
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_STREAM,Uri.parse(path));
            }
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "File Not Found", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadInterAd()
    {

        final InterstitialAd mInterstitialAd;
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getResources().getString(R.string.admob_interstitial));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // TODO Auto-generated method stub
                super.onAdLoaded();
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
    }

    public void setView(View view)
    {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            view.setVisibility(View.VISIBLE);
        }
    }

//    public void serviceStartStop(Menu menu)
//    {
//        MenuItem item = menu.findItem(R.id.item_startservice);
//        if(Constants.isService) {
//            Intent intent = new Intent(context, DownloadService.class)
//                    .setAction(DownloadService.ACTION_STOP);
//            context.startService(intent);
//            item.setTitle("Stop Service");
//        } else {
//            Intent intent = new Intent(context, DownloadService.class)
//                    .setAction(DownloadService.ACTION_START);
//            context.startService(intent);
//            item.setTitle("Start Service");
//        }
//    }

    public boolean isConnectingToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }
}
