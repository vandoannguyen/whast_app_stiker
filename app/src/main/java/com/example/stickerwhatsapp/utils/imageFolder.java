package com.example.stickerwhatsapp.utils;

import android.net.Uri;

import java.util.List;

public class imageFolder {

    private String path;
    private String FolderName;
    private int numberOfPics = 0;
    private String firstPic;
    private List<pictureFacer> subItem;
    private long totalSize;
    public imageFolder(String path, String folderName, int numberOfPics, String firstPic, List<pictureFacer> subItem) {
        this.path = path;
        FolderName = folderName;
        this.numberOfPics = numberOfPics;
        this.firstPic = firstPic;
        this.subItem = subItem;
    }

    public imageFolder(){

    }

    public imageFolder(String path, String folderName) {
        this.path = path;
        FolderName = folderName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolderName() {
        return FolderName;
    }

    public void setFolderName(String folderName) {
        FolderName = folderName;
    }

    public int getNumberOfPics() {
        return numberOfPics;
    }

    public void setNumberOfPics(int numberOfPics) {
        this.numberOfPics = numberOfPics;
    }

    public void addpics(){
        this.numberOfPics++;
    }

    public String getFirstPic() {
        return firstPic;
    }

    public void setFirstPic(String firstPic) {
        this.firstPic = firstPic;
    }
    public List<pictureFacer> getSubItem() {
        return subItem;
    }

    public void setSubItem(List<pictureFacer> subItem) {
        this.subItem = subItem;
    }

}
