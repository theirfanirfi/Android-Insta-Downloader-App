package com.example.adapter;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.instadownloader.R;
import com.example.utils.DBHelper;
import com.example.utils.DownloadItems;
import com.example.utils.Methods;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.MyViewHolder> {

    private ArrayList<DownloadItems> list;
    private ArrayList<Integer> list_id;
    public Context context;
    int posi;
    Methods methods;
    Boolean isDownloading = false;
    DBHelper dbHelper;
    Boolean isPer;
    SharedPreferences sharedPreferences;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView_PostBy, textView_type;
        public RoundedImageView imageView_pending;
        public Button button_download;

        public MyViewHolder(View view) {
            super(view);
            textView_PostBy = (TextView)view.findViewById(R.id.textView_pending_by);
            textView_type = (TextView)view.findViewById(R.id.textView_pending_type);
            imageView_pending = (RoundedImageView) view.findViewById(R.id.imageView_pending);
            button_download = (Button)view.findViewById(R.id.button_pending_download);
        }
    }


    public PendingAdapter(ArrayList<DownloadItems> list, ArrayList<Integer> list_id, Context context) {
        this.list = list;
        this.context = context;
        this.list_id = list_id;
        methods = new Methods(context);
        dbHelper = new DBHelper(context);
        try {
            dbHelper.createDataBase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sharedPreferences = context.getSharedPreferences("auto_d", Context.MODE_PRIVATE);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_download_pending, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.textView_PostBy.setText("Post by: "+list.get(position).getBy());
        holder.textView_type.setText("Type: "+list.get(position).getType());
        if(list.get(position).getType().equals("image")) {
            Picasso
                .with(context)
                .load(Uri.parse(list.get(position).getTemp_url()))
                .fit().centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imageView_pending);
        } else {
            Picasso
                .with(context)
                .load(list.get(position).getImage())
                .fit().centerCrop()
                .placeholder(R.mipmap.ic_launcher)
                .into(holder.imageView_pending);
        }


        holder.button_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPer = sharedPreferences.getBoolean("isPer",false);
                if(isPer) {
                    if (methods.isConnectingToInternet()) {
                        if (!isDownloading) {
                            String name = list.get(holder.getAdapterPosition()).getName();
                            String uri = list.get(holder.getAdapterPosition()).getTemp_url();
                            if (list.get(holder.getAdapterPosition()).getType().equals("video")) {
                                downloadVideo(uri, name, holder.getAdapterPosition());
                            } else {
                                downloadimage(uri, name, holder.getAdapterPosition());
                            }
                        } else {
                            Toast.makeText(context, "wait for file to complete download", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Write Permission not Granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void remove(int position) {
        posi = posi - 1;
        list.remove(position);
        list_id.remove(position);
        notifyItemRemoved(position);

    }

    public void removeAll() {
        int a = list.size();
        for (int i = 0; i < a; i++) {
            list.remove(0);
            list_id.remove(0);
        }
        notifyItemRangeRemoved(0,a);
    }

    public void undo(int position, DownloadItems e, int idd) {
        list.add(position,e);
        list_id.add(position,idd);
        notifyItemInserted(position);
    }

    public void downloadimage(String uRl, String name, int pos) {

        File path = new File(Environment.getExternalStorageDirectory()+"/Insta Downloader/images/");
        if(!path.exists()) {
            path.mkdirs();
        }

        posi = pos;
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Instagram Downloader")
                .setDescription("Downloading:" + name + ".jpeg")
                .setDestinationInExternalPublicDir("", "/Insta Downloader/images/"+name + ".jpeg");
        mgr.enqueue(request);
        isDownloading = true;
        regisBroadcast(pos);
        insertToDatabase(posi,list_id.get(posi));
        remove(pos);
    }

    public void downloadVideo(String uRl, String name, int pos) {

        File path = new File(Environment.getExternalStorageDirectory()+"/Insta Downloader/videos/");
        if(!path.exists()) {
            path.mkdirs();
        }

        posi = pos;
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Instagram Downloader")
                .setDescription("Downloading:" + name + ".mp4")
                .setDestinationInExternalPublicDir("", "/Insta Downloader/videos/"+name + ".mp4");
        mgr.enqueue(request);
        regisBroadcast(pos);
        insertToDatabase(posi,list_id.get(posi));
        remove(pos);
    }

    public void regisBroadcast(int pos)
    {
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            isDownloading = false;
        }
    };

    public void insertToDatabase(int pos,int id)
    {
        String query = "update temp set status = 1 where id = '"+id+"'";
        dbHelper.dml(query);

        dbHelper.dml("INSERT INTO data (name,by,image,video,type,status,temp_url) select name,by,image,video,type,status,temp_url from temp where id = '"+id+"'");
        dbHelper.dml("delete from temp where id = '"+id+"'");
    }
}