package com.example.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apps.instadownloader.R;
import com.example.utils.DownloadItems;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private ArrayList<DownloadItems> list;
    public Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView_name, textView_PostBy;
        public RoundedImageView imageView_downloaded;

        public MyViewHolder(View view) {
            super(view);
            textView_name = (TextView)view.findViewById(R.id.textView_name);
            textView_PostBy = (TextView)view.findViewById(R.id.textView_PostBy);
            imageView_downloaded = (RoundedImageView) view.findViewById(R.id.imageView_downloaded);
        }
    }


    public MyAdapter(ArrayList<DownloadItems> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_listview_downloads, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        Uri uri = null;
        holder.textView_name.setText(list.get(position).getName());
        holder.textView_PostBy.setText(list.get(position).getBy());
        if(list.get(position).getType().equals("image")) {
            uri = Uri.fromFile(new File(list.get(position).getImage() + ".jpeg"));
            Picasso
                    .with(context)
                    .load(uri)
                    .fit().centerCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(holder.imageView_downloaded);
        } else {
          Picasso
            .with(context)
                  .load(list.get(position).getImage())
                  .fit().centerCrop()
                  .placeholder(R.mipmap.ic_launcher)
                  .into(holder.imageView_downloaded);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void remove(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void removeAll() {
        int a = list.size();
        for (int i = 0; i < a; i++) {
            list.remove(0);
        }
        notifyItemRangeRemoved(0,a);
    }

    public void undo(int position, DownloadItems e) {
        list.add(position,e);
        notifyItemInserted(position);
    }
}