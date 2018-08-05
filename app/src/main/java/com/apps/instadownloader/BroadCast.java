package com.apps.instadownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.example.utils.DBHelper;
import com.example.utils.DownloadItems;

public class BroadCast extends BroadcastReceiver {

    DBHelper dbHelper;
    DownloadItems downloadItems;
    String img = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals("android.intent.action.DOWNLOAD_COMPLETE"))
        {
            dbHelper = new DBHelper(context);
            downloadItems = (DownloadItems) intent.getExtras().getSerializable("array");
            if(!downloadItems.getVideo().equals(""))
            {
                img = intent.getExtras().getString("img");
            }
            insertToDatabase(1,"data",downloadItems);
        }

    }

    public void insertToDatabase(int status, String table, DownloadItems dwi)
    {
        String query = "";
        String nm = dwi.getName();
        String post_by = dwi.getBy();
        String temp = dwi.getTemp_url();
        String vid = dwi.getVideo();
        if(vid.equals("")) {
            query = "insert into '"+table+"' (name,by,image,video,type,status,temp_url) values ('" + nm + "','" + post_by + "','" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/InstaSave/images/" + nm + "','"+null+"','image','"+status+"','"+temp+"')";
            Log.e("--Data::","image inserted");
        } else {
            query = "insert into '"+table+"' (name,by,image,video,type,status,temp_url) values ('" + nm + "','" + post_by + "','" + img + "','"+Environment.getExternalStorageDirectory().getAbsolutePath() + "/InstaSave/videos/" + nm+"','video','"+status+"','"+temp+"')";
            Log.e("--Data::","video inserted");
        }

        dbHelper.dml(query);
    }
}
