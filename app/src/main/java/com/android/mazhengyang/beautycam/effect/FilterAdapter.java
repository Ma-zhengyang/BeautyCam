package com.android.mazhengyang.beautycam.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.mazhengyang.beautycam.R;
import com.android.mazhengyang.beautycam.utils.GPUImageFilterTools;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * Created by mazhengyang on 18-10-17.
 */

public class FilterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = FilterAdapter.class.getSimpleName();

    List<FilterEffect> filterUris;
    Context mContext;
    private Bitmap background;

    public FilterAdapter(Context context, List<FilterEffect> effects, Bitmap backgroud) {
        filterUris = effects;
        mContext = context;
        this.background = backgroud;
    }

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bottom_filter, parent, false);
        EffectHolder vh = new EffectHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        Log.d(TAG, "onBindViewHolder: positon=" + position);

        final FilterEffect effect = filterUris.get(position);

        ((EffectHolder) holder).filteredImg.setImage(background);
        ((EffectHolder) holder).filterName.setText(effect.getTitle());
        ((EffectHolder) holder).filteredImg.setFilter(effect.getFilter());
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + filterUris.size());
        return filterUris.size();
    }

    public class EffectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.small_filter)
        GPUImageView filteredImg;
        @BindView(R.id.filter_name)
        TextView filterName;

        public EffectHolder(View itemView) {
            super(itemView);
            setIsRecyclable(false);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(v, this.getPosition());
            }
        }
    }

}
