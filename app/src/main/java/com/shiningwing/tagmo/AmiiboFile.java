package com.shiningwing.tagmo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public class AmiiboFile
        implements Parcelable {

    protected File filePath;
    protected long id;

    public AmiiboFile(File filePath, long id) {
        this.filePath = filePath;
        this.id = id;
    }

    public File getFilePath() {
        return filePath;
    }

    public void setFilePath(File filePath) {
        this.filePath = filePath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.filePath);
        dest.writeLong(this.id);
    }

    protected AmiiboFile(Parcel in) {
        this.filePath = (File) in.readSerializable();
        this.id = in.readLong();
    }

    public static final Parcelable.Creator<AmiiboFile> CREATOR =
            new Parcelable.Creator<AmiiboFile>() {
        @Override
        public AmiiboFile createFromParcel(Parcel source) {
            return new AmiiboFile(source);
        }

        @Override
        public AmiiboFile[] newArray(int size) {
            return new AmiiboFile[size];
        }
    };
}
