package com.apps.instadownloader;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adapter.PendingAdapter;
import com.example.utils.Constants;
import com.example.utils.DBHelper;
import com.example.utils.DownloadItems;
import com.example.utils.Methods;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.onesignal.OneSignal;
import com.startapp.android.publish.Ad;
import com.startapp.android.publish.AdEventListener;
import com.startapp.android.publish.StartAppAd;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    Menu menu;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Toolbar toolbar;
    RecyclerView recyclerView;
    ArrayList<DownloadItems> arrayList;
    ArrayList<Integer> arrayList_id;
    TextView textView;
    DBHelper dbHelper;
    DownloadItems downloadItems;
    PendingAdapter pendingAdapter;
    Boolean isAutoDismiss = true;
    LinearLayout linearLayout;
    Methods methods;
    View view;
    final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
    private StartAppAd startAppAd = new StartAppAd(this);
    AdView mAdView1;
    Boolean isDestroy = true;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartAppAd.init(this, getString(R.string.startapp_dev_id), getString(R.string.startapp_app_id));
        OneSignal.startInit(this).init();
        setContentView(R.layout.activity_main);
        //showDialog();
        mAdView1 = (AdView) findViewById(R.id.adView1);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("iSave Insta Downloader");


        StartAppAd.showSlider(this);
        startAppAd.loadAd(new AdEventListener() {

            @Override
            public void onReceiveAd(Ad arg0) {
                // TODO Auto-generated method stub
                if(Constants.isFirst) {
                    startAppAd.showAd(); // show the ad
                    startAppAd.loadAd(); // load the next ad
                    Constants.isFirst = false;
                }
            }

            @Override
            public void onFailedToReceiveAd(Ad arg0) {
                // TODO Auto-generated method stub

            }
        });

        sharedPreferences = getSharedPreferences("auto_d", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        setStatusColor();
        checkPer();

        dbHelper = new DBHelper(this);
        try {
            dbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        view = (View)findViewById(R.id.view_main);
        methods = new Methods(this);
        methods.setView(view);


        Constants.isAutoDownload = sharedPreferences.getBoolean("isauto",false);

        Intent intent = new Intent(MainActivity.this, DownloadService.class)
                .setAction(DownloadService.ACTION_START);
        startService(intent);
        Constants.isAutoDownload = true;

        linearLayout = (LinearLayout)findViewById(R.id.ll_main);
        arrayList = new ArrayList<DownloadItems>();
        arrayList_id = new ArrayList<Integer>();
        textView = (TextView)findViewById(R.id.textView_emptyView_main);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        pendingAdapter = new PendingAdapter(arrayList,arrayList_id,this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        loadCompletedData();

        recyclerView.setAdapter(pendingAdapter);
        methods.showHideEmptyView(textView,recyclerView,pendingAdapter.getItemCount());

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                final int p = viewHolder.getAdapterPosition();
                final int id = arrayList_id.get(p);
                downloadItems = arrayList.get(p);
                final int tid = arrayList_id.get(p);
                pendingAdapter.remove(p);
                methods.showHideEmptyView(textView,recyclerView,pendingAdapter.getItemCount());

                Snackbar.make(linearLayout,"Deleted",Snackbar.LENGTH_SHORT)
                    .setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            isAutoDismiss = false;
                            pendingAdapter.undo(p,downloadItems,tid);
                            methods.showHideEmptyView(textView,recyclerView,pendingAdapter.getItemCount());
                        }
                    })
                    .setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            if(isAutoDismiss) {
                                deleteFromDatabase(id,p);
                            }
                            isAutoDismiss = true;
                            super.onDismissed(snackbar, event);
                        }
                    })
                    .show();
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        setAutoDownload();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_autodownload:
                if(Constants.isAutoDownload){
                    Constants.isAutoDownload = false;
                } else {
                    if(sharedPreferences.getBoolean("isPer",false)) {
                        Constants.isAutoDownload = true;
                    } else {
                        Toast.makeText(MainActivity.this, "Please Grant Storage Permission to use this feature", Toast.LENGTH_SHORT).show();
                    }
                }
                setAutoDownload();
                break;
            case R.id.item_downloaded:
                Intent ids = new Intent(MainActivity.this,Downloads.class);
                startActivity(ids);
                break;
            case R.id.item_about:
                Intent intent = new Intent(MainActivity.this,AboutUs.class);
                startActivity(intent);
                break;
            case R.id.item_clear:
                if(pendingAdapter.getItemCount() > 0) {
                    clearAll();
                } else {
                    Toast.makeText(MainActivity.this, "No items to clear", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.item_rate:
                final String appName = getPackageName();//your application package name i.e play store application url
                Log.e("package:", appName);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id="
                                    + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id="
                                    + appName)));
                }
                break;
            case R.id.item_moreapp:
                startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(getString(R.string.play_more_apps))));
                break;
            case R.id.item_help:
                openHelp();
                break;
//            case R.id.item_startservice:
//                methods.serviceStartStop(menu);
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setAutoDownload()
    {
        MenuItem item = menu.findItem(R.id.item_autodownload);
        if(Constants.isAutoDownload) {
            item.setChecked(true);
        } else {
            item.setChecked(false);
        }
        editor.putBoolean("isauto", Constants.isAutoDownload);
        editor.commit();
    }


    public void loadCompletedData()
    {
        Cursor c = dbHelper.getData("select * from temp where status = 0");
        if (c!=null && c.getCount()>0) {
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                int id = c.getInt(c.getColumnIndex("id"));
                String name = c.getString(c.getColumnIndex("name"));
                String by = c.getString(c.getColumnIndex("by"));
                String image = c.getString(c.getColumnIndex("image"));
                String video = c.getString(c.getColumnIndex("video"));
                String type = c.getString(c.getColumnIndex("type"));
                String temp = c.getString(c.getColumnIndex("temp_url"));

                downloadItems = new DownloadItems(name, by, image, video, type, temp);
                arrayList.add(downloadItems);
                arrayList_id.add(id);
                c.moveToNext();
            }
        }
    }

    public void deleteFromDatabase(int id,int p)
    {
        dbHelper.dml("delete from temp where id = '"+id+"'");
        methods.showHideEmptyView(textView,recyclerView,pendingAdapter.getItemCount());
    }

    public void clearAll()
    {
        AlertDialog.Builder ai = new AlertDialog.Builder(MainActivity.this)
            .setMessage("Are you sure you want to clear all")
            .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    pendingAdapter.removeAll();
                    methods.showHideEmptyView(textView,recyclerView,pendingAdapter.getItemCount());
                    dbHelper.dml("delete from temp where status = 0");
                    Toast.makeText(MainActivity.this, "All items cleared", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
        ai.show();
    }

    public void setStatusColor()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
            toolbar.setElevation(10);
        }
    }

    public void checkPer()
    {
        final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102;
        if ((ContextCompat.checkSelfPermission(MainActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            // user already provided permission
            // perform function for what you want to achieve
            editor.putBoolean("isPer", true);
            editor.commit();
        }
    }

    public void openHelp()
    {
        isDestroy = false;
        Intent intent = new Intent(MainActivity.this,AppIntro.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        boolean canUseExternalStorage = false;

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                }

                if (!canUseExternalStorage) {
                    Toast.makeText(MainActivity.this, "Cannot use download feature without requested permission", Toast.LENGTH_SHORT).show();
                } else {
                    // user now provided permission
                    // perform function for what you want to achieve
                    editor.putBoolean("isPer", true);
                    editor.commit();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        arrayList.clear();
        arrayList_id.clear();
        loadCompletedData();
        recyclerView.setAdapter(pendingAdapter);
        methods.showHideEmptyView(textView,recyclerView,pendingAdapter.getItemCount());
        super.onResume();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keycode = event.getKeyCode();
        final int action = event.getAction();
        if (keycode == KeyEvent.KEYCODE_MENU && action == KeyEvent.ACTION_UP) {
            return true; // consume the key press
        }
        return super.dispatchKeyEvent(event);
    }
}
