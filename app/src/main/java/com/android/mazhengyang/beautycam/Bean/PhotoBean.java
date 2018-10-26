package com.android.mazhengyang.beautycam.Bean;

/**
 * Created by mazhengyang on 18-10-26.
 */

public class PhotoBean {

    String path;
    String title;
    boolean isChecked = false;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
