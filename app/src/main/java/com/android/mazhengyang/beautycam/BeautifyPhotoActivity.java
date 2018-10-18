package com.android.mazhengyang.beautycam;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.mazhengyang.beautycam.adapter.FilterAdapter;
import com.android.mazhengyang.beautycam.effect.EffectBean;
import com.android.mazhengyang.beautycam.effect.EffectFactory;
import com.android.mazhengyang.beautycam.ui.widget.HorizontalListView;
import com.android.mazhengyang.beautycam.utils.DataBuffer;
import com.android.mazhengyang.beautycam.utils.LoadImageCallback;
import com.android.mazhengyang.beautycam.utils.LoadImageTask;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * Created by mazhengyang on 18-10-17.
 */

public class BeautifyPhotoActivity extends Activity {

    private static final String TAG = BeautifyPhotoActivity.class.getSimpleName();

    private static final int PERMISSIONS_STORE_REQUEST_EXTERNAL_STORAGE = 1024;

    @BindView(R.id.gpu_image)
    GPUImageView mGPUImageView;
    @BindView(R.id.list_tools)
    HorizontalListView mHorizontalListView;
    @BindView(R.id.indicator)
    AVLoadingIndicatorView mIndicator;

    //当前预览的大图片
    private Bitmap currentBitmap;
    //底部特效预览的小图片
    private Bitmap smallImageBackgroud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_beautify_photo);
        ButterKnife.bind(this);

        byte[] data = DataBuffer.getByteArray();

        if (data == null) {
            Log.e(TAG, "onCreate: data is null");
        } else {
            mIndicator.show();
            new LoadImageTask().load(data, new LoadImageCallback() {
                @Override
                public void callback(Bitmap result) {
                    Log.d(TAG, "callback: currentBitmap=" + result);
                    currentBitmap = result;
                    mGPUImageView.setImage(currentBitmap);
                    mIndicator.hide();
                }
            });

            new LoadImageTask().load(data, new LoadImageCallback() {
                @Override
                public void callback(Bitmap result) {
                    Log.d(TAG, "callback: smallImageBackgroud=" + result);
                    smallImageBackgroud = result;
                    initFilterToolBar();
                }
            }, 300, 300);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataBuffer.cleanByteArray();
    }

    /**
     * 初始化滤镜
     */
    private void initFilterToolBar() {
        final List<EffectBean> filters = EffectFactory.getInst().getLocalFilters(this);
        final FilterAdapter adapter = new FilterAdapter(this, filters, smallImageBackgroud);
        adapter.setOnItemClickListener(new FilterAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "onItemClick: position=" + position);

                for (EffectBean b : filters) {
                    b.setChecked(false);
                }

                filters.get(position).setChecked(true);
                adapter.notifyDataSetChanged();

                GPUImageFilter filter = filters.get(position).getFilter();
                mGPUImageView.setFilter(filter);

            }
        });
        mHorizontalListView.setAdapter(adapter);
        mHorizontalListView.setItemViewCacheSize(15);
    }

    public void onCheckClick(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(BeautifyPhotoActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_STORE_REQUEST_EXTERNAL_STORAGE);
        } else {
            backImage();
        }
    }

    /**
     * 返回图片到CameraActivity
     */
    private void backImage() {
        Bitmap b = null;
        try {
            b = mGPUImageView.capture();
            DataBuffer.setBitmap(b);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "onConfirmClick: InterruptedException " + e);
            DataBuffer.setBitmap(null);
        }
        finish();
    }

    public void onCancelClick(View view) {
        if (currentBitmap != null && !currentBitmap.isRecycled()) {
            currentBitmap.recycle();
            currentBitmap = null;
        }
        if (smallImageBackgroud != null && !smallImageBackgroud.isRecycled()) {
            smallImageBackgroud.recycle();
            smallImageBackgroud = null;
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult: requestCode = " + requestCode);
        Log.d(TAG, "onRequestPermissionsResult: grantResults.length = " + grantResults.length);
        if (grantResults.length > 0) {
            Log.d(TAG, "onRequestPermissionsResult: grantResults[0] = " + grantResults[0]);
        }

        switch (requestCode) {
            case PERMISSIONS_STORE_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    backImage();
                } else {
                    Toast.makeText(getApplicationContext(), getApplicationContext().
                            getString(R.string.no_storage_permission), Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                break;
            }
            default:
                break;
        }
    }
}
