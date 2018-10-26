package com.android.mazhengyang.beautycam;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.mazhengyang.beautycam.Bean.PhotoBean;
import com.android.mazhengyang.beautycam.adapter.GalleryAdapter;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by mazhengyang on 18-10-26.
 */

public class GalleryActivity extends Activity implements GalleryAdapter.OnDataChangedListener {

    private static final String TAG = GalleryActivity.class.getSimpleName();

    private GalleryAdapter galleryAdapter;

    @BindView(R.id.gallery_recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.btn_select)
    Button selectBtn;
    @BindView(R.id.btn_delete)
    Button deleteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_gallery);
        ButterKnife.bind(this);

        ArrayList<PhotoBean> list = initList();
        galleryAdapter = new GalleryAdapter(this, list, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(galleryAdapter);

        deleteBtn.setEnabled(false);
    }

    private ArrayList<PhotoBean> initList() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        String sd = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        String dir = sd.concat("/BeautyCam/");
        File f = new File(dir);
        if (!f.exists()) {
            return null;
        }

        ArrayList<PhotoBean> list = new ArrayList<>();

        File[] files = f.listFiles();

        for (int i = 0; i < files.length; i++) {
            String title = files[i].getName();

            if (title.startsWith("IMG_")) {

                PhotoBean bean = new PhotoBean();
                bean.setTitle(title);
                bean.setPath(dir.concat(title));

                list.add(bean);
            }
        }

        return list;
    }

    private void checkDeleteBtn() {
        for (PhotoBean bean : galleryAdapter.getData()) {
            if (bean != null) {
                if (bean.isChecked()) {
                    deleteBtn.setTextColor(getColor(R.color.red));
                    deleteBtn.setEnabled(true);
                    return;
                }
            }
        }
        deleteBtn.setTextColor(getColor(R.color.gray41));
        deleteBtn.setEnabled(false);
    }

    private boolean isAllSelected() {
        for (PhotoBean bean : galleryAdapter.getData()) {
            if (bean != null) {
                if (!bean.isChecked()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onChanged() {
        checkDeleteBtn();

        if (isAllSelected()) {
            selectBtn.setText(R.string.photo_select_cancel);
        } else {
            selectBtn.setText(R.string.photo_select_all);
        }
    }

    public void onSelectClick(View view) {
        if (isAllSelected()) {
            galleryAdapter.unSelectAll();
            selectBtn.setText(R.string.photo_select_all);
        } else {
            galleryAdapter.selectAll();
            selectBtn.setText(R.string.photo_select_cancel);
        }
        checkDeleteBtn();
    }

    public void onDeleteClick(View view) {

        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.photo_delete_title))
                /// .setContentText("")
                .setCancelText(getString(R.string.photo_delete_cancel))
                .showCancelButton(true)
                .setConfirmText(getString(R.string.photo_delete_confirm))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        delete(sDialog);
                    }
                })
                .show();
    }

    private void delete(SweetAlertDialog sDialog) {
        ArrayList<PhotoBean> old = galleryAdapter.getData();

        int successCount = 0;
        int failCount = 0;
        for (int i = 0; i < old.size(); i++) {
            PhotoBean bean = old.get(i);
            String path = bean.getPath();
            String title = bean.getTitle();

            boolean checked = bean.isChecked();
            Log.d(TAG, String.format("delete: %d %s is checked", i, title));

            if (!checked) {
                continue;
            }

            File file = new File(path);
            if (file.exists()) {
                boolean delete = file.delete();
                if (delete) {
                    //使用remove后list原始对应index的bean已经变掉，会导致循环中原本想要的对象得到错误的结果
//                  photoList.remove(i);
                    old.set(i, null);
                    successCount++;
                    Log.d(TAG, "onDeleteClick: delete " + title);
                } else {
                    failCount++;
                    Log.e(TAG, "onDeleteClick: delete fail" + title);
                }
            }
        }

        ArrayList<PhotoBean> list = new ArrayList<>();
        for (int i = 0; i < old.size(); i++) {
            PhotoBean bean = old.get(i);
            if (bean != null) {
                list.add(bean);
            }
        }

        checkDeleteBtn();
        galleryAdapter.updateData(list);

        sDialog
                .setTitleText(getString(R.string.photo_delete_success))
                .setContentText(String.format(getString(R.string.photo_delete_sum), successCount, failCount))
                .setConfirmText(getString(R.string.photo_delete_confirm))
                .setConfirmClickListener(null)
                .changeAlertType(failCount == 0 ? SweetAlertDialog.SUCCESS_TYPE : SweetAlertDialog.ERROR_TYPE);
    }

}
