package com.apps.instadownloader;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.DownloadItems;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class DownloadService extends IntentService {

    public static final String ACTION_STOP = "com.prince.viavi.saveimage.action.STOP";
    public static final String ACTION_START = "com.prince.viavi.saveimage.action.START";

    DBHelper dbHelper;
    NotificationCompat.Builder notification;
    String action;
    String img = "";
    String vid = "";
    String name = "";
    String by = "";
    Boolean isError = false, isVideo = false;
    DownloadItems downloadItems;
    String temp_url = "";

    public DownloadService() {
        super(null);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();

        dbHelper = new DBHelper(getApplicationContext());
        try {
            dbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        notificationIntent.putExtra("from","service");
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, DownloadService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent pstopIntent = PendingIntent.getService(this, 0,
                stopIntent, 0);


        notification = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle("iSave Downloader")
                .setTicker("iSave")
                .setContentText("Just Copy Url From Instagram")
                .setSmallIcon(R.drawable.instagramvideodownloader)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, "Stop",
                        pstopIntent);

        ClipboardManager clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(ClipboardListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        action = intent.getAction();

        if(action.equals(ACTION_START)) {
            processNotificationShowRequest();
            Constants.isService = true;
        } else if(action.equals(ACTION_STOP)) {
            stopService(intent);
            stopForeground(true);
            Constants.isService = false;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    private void processNotificationShowRequest() {
        startForeground(101, notification.build());
    }

    ClipboardManager.OnPrimaryClipChangedListener ClipboardListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = clipboard.getPrimaryClip();
            ClipData.Item item = clipData.getItemAt(0);
            String s = item.getText().toString();
            if(s.startsWith("https://www.instagram.com/")) {
                new GetUrl().execute(s);
            }
        }
    };

    public class GetUrl extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            Document doc = null;
            Elements description_image = null;
            Elements description_video = null;
            Elements description_desc = null;
            try {
                doc = Jsoup.connect(url).get();
                description_image = doc.select("meta[property=og:image]");
                img = description_image.attr("content");
                description_video = doc.select("meta[property=og:video:secure_url]");
                vid = description_video.attr("content");
                description_desc = doc.select("meta[property=og:description]");
                String a = description_desc.attr("content");
                String [] b = a.split("@");
                String [] c = b[1].split("•");
                by = c[0].trim();
                Random random = new Random();
                long n = random.nextInt(999999999 - 100000000);
                name = String.valueOf(n);
                isVideo = !vid.equals("");
            } catch (IOException e) {
                isError = true;
                isVideo = false;
                e.printStackTrace();
            } catch (Exception e) {
                isError = true;
                isVideo = false;
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(Constants.isAutoDownload) {
                if (isVideo) {
                    temp_url = vid;
                    downloadItems = new DownloadItems(name,by,img,vid,"video",temp_url);
                    downloadVideo(vid, name,downloadItems);
                } else {
                    temp_url = img;
                    downloadItems = new DownloadItems(name,by,img,vid,"image",temp_url);
                    downloadimage(img, name,downloadItems);
                }
            } else {
                if(isVideo){
                    temp_url = vid;
                    downloadItems = new DownloadItems(name,by,img,vid,"video",temp_url);
                }else {
                    temp_url = img;
                    downloadItems = new DownloadItems(name,by,img,vid,"video",temp_url);
                }
                insertToDatabase(0,"temp");
            }
        }
    }

    public void downloadimage(String uRl, String name, DownloadItems di) {

        File path = new File(Environment.getExternalStorageDirectory()+"/Insta Downloader/images/");
        if(!path.exists()) {
            path.mkdirs();
        }

        String nm = di.getName();
        DownloadManager mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("iSave Downloader")
                .setDescription("Downloading:" + nm + ".jpeg")
                .setDestinationInExternalPublicDir("", "/Insta Downloader/images/"+nm + ".jpeg");
        mgr.enqueue(request);
//        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//        Intent intent = new Intent(getApplicationContext(),BroadCast.class);
//        intent.setAction("android.intent.action.DOWNLOAD_COMPLETE");
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.putExtra("array", di);
//        sendBroadcast(intent);
        insertToDatabase(1,"data",di);
    }

    public void downloadVideo(String uRl, String name, DownloadItems di) {

        File path = new File(Environment.getExternalStorageDirectory()+"/Insta Downloader/videos/");
        if(!path.exists()) {
            path.mkdirs();
        }

        String nm = di.getName();

        DownloadManager mgr = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("iSave Downloader")
                .setDescription("Downloading:" + nm + ".mp4")
                .setDestinationInExternalPublicDir("", "/Insta Downloader/videos/"+nm + ".mp4");
        mgr.enqueue(request);
//        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

//        Intent intent = new Intent(getApplicationContext(),BroadCast.class);
//        intent.setAction("android.intent.action.DOWNLOAD_COMPLETE");
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.putExtra("array", di);
//        intent.putExtra("img", img);
//        sendBroadcast(intent);
        insertToDatabase(1,"data",di);
    }

//    BroadcastReceiver onComplete = new BroadcastReceiver() {
//        public void onReceive(Context ctxt, Intent intent) {
//            if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
//            {
//                Log.e("--","In Broadcast receiver condition");
//                DownloadItems dwi = (DownloadItems) intent.getSerializableExtra("array");
//                insertToDatabase(1,"data");
//            }
//        }
//    };

    public void insertToDatabase(int status, String table)
    {
        String query = "";
        if(vid.equals("")) {
            query = "insert into '"+table+"' (name,by,image,video,type,status,temp_url) values ('" + name + "','" + by + "','" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Insta Downloader/images/" + name + "','"+null+"','image','"+status+"','"+temp_url+"')";
        } else {
            query = "insert into '"+table+"' (name,by,image,video,type,status,temp_url) values ('" + name + "','" + by + "','" + img + "','"+Environment.getExternalStorageDirectory().getAbsolutePath() + "/Insta Downloader/videos/" + name+"','video','"+status+"','"+temp_url+"')";
        }
        dbHelper.dml(query);
        Log.e("--Data::","inserted");
    }

    public void insertToDatabase(int status, String table, DownloadItems dwi)
    {
        String query = "";
        String nm = dwi.getName();
        String post_by = dwi.getBy();
        String temp = dwi.getTemp_url();
        String vid = dwi.getVideo();
        if(vid.equals("")) {
            query = "insert into '"+table+"' (name,by,image,video,type,status,temp_url) values ('" + nm + "','" + post_by + "','" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/Insta Downloader/images/" + nm + "','"+null+"','image','"+status+"','"+temp+"')";
            Log.e("--Data::","image inserted");
        } else {
            query = "insert into '"+table+"' (name,by,image,video,type,status,temp_url) values ('" + nm + "','" + post_by + "','" + img + "','"+Environment.getExternalStorageDirectory().getAbsolutePath() + "/Insta Downloader/videos/" + nm+"','video','"+status+"','"+temp+"')";
            Log.e("--Data::","video inserted");
        }

        dbHelper.dml(query);
    }
}
