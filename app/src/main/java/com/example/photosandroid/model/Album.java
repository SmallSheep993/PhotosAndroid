package com.example.photosandroid.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Album implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void addPhoto(Photo photo) {
        photos.add(photo);
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
    }

    // Used to determine if a photo already exists in an album (based on the path)
    public boolean containsPhoto(Photo photo) {
        for (Photo p : photos) {
            if (p.getFilePath().equals(photo.getFilePath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return name + " (" + photos.size() + " photos)";
    }
}
