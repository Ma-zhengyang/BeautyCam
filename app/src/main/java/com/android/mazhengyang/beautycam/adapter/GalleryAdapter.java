package com.android.mazhengyang.beautycam.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.mazhengyang.beautycam.Bean.PhotoBean;
import com.android.mazhengyang.beautycam.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mazhengyang on 18-10-26.
 */

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = GalleryAdapter.class.getSimpleName();

    private ArrayList<PhotoBean> photoList;
    private Context mContext;

    public interface OnDataChangedListener {
        void onChanged();
    }

    private OnDataChangedListener listener;

    public GalleryAdapter(Context context, ArrayList<PhotoBean> list, OnDataChangedListener listener) {
        mContext = context;
        this.photoList = list;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        PhotoHolder vh = new PhotoHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: positon=" + position);

        PhotoBean bean = photoList.get(position);

        String title = bean.getTitle();
        String path = bean.getPath();

        ((PhotoHolder) holder).checkedIcon.setVisibility(View.INVISIBLE);
        if (bean.isChecked()) {
            ((PhotoHolder) holder).checkedIcon.setVisibility(View.VISIBLE);
        }

        ((PhotoHolder) holder).photoImg.setImageDrawable(null);
        Glide.with(mContext).load(path).into(((PhotoHolder) holder).photoImg);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + photoList.size());
        return photoList.size();
    }

    public ArrayList<PhotoBean> getData() {
        return photoList;
    }

    public void updateData(ArrayList<PhotoBean> list) {
        this.photoList = list;
        notifyDataSetChanged();
    }

    public void selectAll() {
        for (PhotoBean bean : photoList) {
            if (!bean.isChecked()) {
                bean.setChecked(true);
            }
        }
        notifyDataSetChanged();
    }

    public void unSelectAll() {
        for (PhotoBean bean : photoList) {
            if (bean.isChecked()) {
                bean.setChecked(false);
            }
        }
        notifyDataSetChanged();
    }

    public class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.iv_photo)
        ImageView photoImg;
        @BindView(R.id.iv_photo_checked)
        ImageView checkedIcon;

        public PhotoHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            PhotoBean bean = photoList.get(this.getPosition());
            if (bean.isChecked()) {
                bean.setChecked(false);
            } else {
                bean.setChecked(true);
            }
            if (listener != null) {
                listener.onChanged();
            }
            notifyDataSetChanged();
        }
    }
}


