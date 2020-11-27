package com.shiningwing.tagmo;

import android.os.Parcel;
import android.os.Parcelable;

import com.shiningwing.tagmo.amiibo.AmiiboManager;

import java.io.File;
import java.util.ArrayList;

public class BrowserSettings
        implements Parcelable {
    protected ArrayList<BrowserSettingsListener> listeners = new ArrayList<>();
    protected AmiiboManager amiiboManager;
    protected BrowserSettings oldBrowserSettings;

    protected ArrayList<AmiiboFile> amiiboFiles = new ArrayList<>();
    protected ArrayList<File> folders = new ArrayList<>();
    protected File browserFolder;
    protected String query;
    protected int sort;
    protected String filterGameSeries;
    protected String filterCharacter;
    protected String filterAmiiboSeries;
    protected String filterAmiiboType;
    protected int browserAmiiboView;
    protected String imageNetworkSettings;
    protected boolean recursiveFiles;

    public BrowserSettings() {
        oldBrowserSettings = new BrowserSettings(false);
    }

    public BrowserSettings(
        ArrayList<AmiiboFile> amiiboFiles, ArrayList<File> folders, File browserFolder,
        String query, int sort, String filterGameSeries, String filterCharacter,
        String filterAmiiboSeries, String filterAmiiboType, int browserAmiiboView,
        String imageNetworkSettings, boolean recursiveFiles
    ) {
        super();

        this.amiiboFiles.addAll(amiiboFiles);
        this.folders.addAll(folders);
        this.browserFolder = browserFolder;
        this.query = query;
        this.sort = sort;
        this.filterGameSeries = filterGameSeries;
        this.filterCharacter = filterCharacter;
        this.filterAmiiboSeries = filterAmiiboSeries;
        this.filterAmiiboType = filterAmiiboType;
        this.browserAmiiboView = browserAmiiboView;
        this.imageNetworkSettings = imageNetworkSettings;
        this.recursiveFiles = recursiveFiles;
    }

    private BrowserSettings(boolean duplicate) {
        if (duplicate) {
            this.oldBrowserSettings = this.copy();
        }
    }

    public AmiiboManager getAmiiboManager() {
        return this.amiiboManager;
    }

    public void setAmiiboManager(AmiiboManager amiiboManager) {
        this.amiiboManager = amiiboManager;
    }

    public ArrayList<AmiiboFile> getAmiiboFiles() {
        return amiiboFiles;
    }

    public void setAmiiboFiles(ArrayList<AmiiboFile> amiiboFiles) {
        this.amiiboFiles.clear();
        this.amiiboFiles.addAll(amiiboFiles);
    }

    public ArrayList<File> getFolders() {
        return folders;
    }

    public void setFolders(ArrayList<File> folders) {
        this.folders.clear();
        this.folders.addAll(folders);
    }

    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getSort() {
        return this.sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getGameSeriesFilter() {
        return this.filterGameSeries;
    }

    public void setGameSeriesFilter(String filterGameSeries) {
        this.filterGameSeries = filterGameSeries;
    }

    public String getCharacterFilter() {
        return this.filterCharacter;
    }

    public void setCharacterFilter(String filterCharacter) {
        this.filterCharacter = filterCharacter;
    }

    public String getAmiiboSeriesFilter() {
        return this.filterAmiiboSeries;
    }

    public void setAmiiboSeriesFilter(String filterAmiiboSeries) {
        this.filterAmiiboSeries = filterAmiiboSeries;
    }

    public String getAmiiboTypeFilter() {
        return this.filterAmiiboType;
    }

    public void setAmiiboTypeFilter(String filterAmiiboType) {
        this.filterAmiiboType = filterAmiiboType;
    }

    public int getAmiiboView() {
        return this.browserAmiiboView;
    }

    public void setAmiiboView(int amiiboView) {
        this.browserAmiiboView = amiiboView;
    }

    public File getBrowserRootFolder() {
        return this.browserFolder;
    }

    public void setBrowserRootFolder(File browserRootFolder) {
        this.browserFolder = browserRootFolder;
    }

    public String getImageNetworkSettings() {
        return imageNetworkSettings;
    }

    public void setImageNetworkSettings(String imageNetworkSettings) {
        this.imageNetworkSettings = imageNetworkSettings;
    }

    public boolean isRecursiveFiles() {
        return recursiveFiles;
    }

    public void setRecursiveFiles(boolean recursiveFiles) {
        this.recursiveFiles = recursiveFiles;
    }

    public void notifyChanges() {
        for (BrowserSettingsListener listener : this.listeners) {
            listener.onBrowserSettingsChanged(this, this.oldBrowserSettings);
        }
        this.oldBrowserSettings = this.copy();
    }

    public void addChangeListener(BrowserSettingsListener listener) {
        this.listeners.add(listener);
    }

    public void removeChangeListener(BrowserSettingsListener listener) {
        this.listeners.remove(listener);
    }

    interface BrowserSettingsListener {
        void onBrowserSettingsChanged (BrowserSettings newBrowserSettings, BrowserSettings oldBrowserSettings);
    }

    private BrowserSettings copy() {
        BrowserSettings copy = new BrowserSettings(false);
        copy.setAmiiboManager(this.getAmiiboManager());
        copy.setAmiiboFiles(this.getAmiiboFiles());
        copy.setFolders(this.getFolders());
        copy.setQuery(this.getQuery());
        copy.setSort(this.getSort());
        copy.setGameSeriesFilter(this.getGameSeriesFilter());
        copy.setCharacterFilter(this.getCharacterFilter());
        copy.setAmiiboSeriesFilter(this.getAmiiboSeriesFilter());
        copy.setAmiiboTypeFilter(this.getAmiiboTypeFilter());
        copy.setAmiiboView(this.getAmiiboView());
        copy.setBrowserRootFolder(this.getBrowserRootFolder());
        copy.setImageNetworkSettings(this.getImageNetworkSettings());
        copy.setRecursiveFiles(this.isRecursiveFiles());

        return copy;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.amiiboFiles);
        dest.writeList(this.folders);
        dest.writeSerializable(this.browserFolder);
        dest.writeString(this.query);
        dest.writeInt(this.sort);
        dest.writeString(this.filterGameSeries);
        dest.writeString(this.filterCharacter);
        dest.writeString(this.filterAmiiboSeries);
        dest.writeString(this.filterAmiiboType);
        dest.writeInt(this.browserAmiiboView);
        dest.writeString(this.imageNetworkSettings);
        dest.writeByte(this.recursiveFiles ? (byte) 1 : (byte) 0);
    }

    protected BrowserSettings(Parcel in) {
        this.amiiboFiles = in.createTypedArrayList(AmiiboFile.CREATOR);
        this.folders = new ArrayList<File>();
        in.readList(this.folders, File.class.getClassLoader());
        this.browserFolder = (File) in.readSerializable();
        this.query = in.readString();
        this.sort = in.readInt();
        this.filterGameSeries = in.readString();
        this.filterCharacter = in.readString();
        this.filterAmiiboSeries = in.readString();
        this.filterAmiiboType = in.readString();
        this.browserAmiiboView = in.readInt();
        this.imageNetworkSettings = in.readString();
        this.recursiveFiles = in.readByte() != 0;
    }

    public static final Parcelable.Creator<BrowserSettings> CREATOR = new Parcelable.Creator<BrowserSettings>() {
        @Override
        public BrowserSettings createFromParcel(Parcel source) {
            return new BrowserSettings(source);
        }

        @Override
        public BrowserSettings[] newArray(int size) {
            return new BrowserSettings[size];
        }
    };
}
