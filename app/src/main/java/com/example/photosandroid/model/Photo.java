package com.example.photosandroid.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Photo implements Serializable {
    private static final long serialVersionUID = 2L;

    private String filePath;
    private String description;
    private long dateMillis; // Save as Unix timestamp for Android compatibility
    private List<Tag> tags;

    public Photo(String filePath) {
        this.filePath = filePath;
        this.description = "";
        this.tags = new ArrayList<>();
        File file = new File(filePath);
        this.dateMillis = file.lastModified();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return new Date(dateMillis);
    }

    public List<Tag> getTags() {
        return tags;
    }

    public boolean addTag(String tagName, String tagValue) {
        tagName = tagName.toLowerCase();
        tagValue = tagValue.toLowerCase();
        for (Tag t : tags) {
            if (t.getName().equalsIgnoreCase(tagName) && t.getValue().equalsIgnoreCase(tagValue)) {
                return false;
            }
        }
        if (tagName.equals("location")) {
            for (Tag t : tags) {
                if (t.getName().equalsIgnoreCase("location")) {
                    return false;
                }
            }
        }
        tags.add(new Tag(tagName, tagValue));
        return true;
    }

    public void removeTag(String tagName, String tagValue) {
        Tag toRemove = null;
        for (Tag t : tags) {
            if (t.getName().equalsIgnoreCase(tagName) && t.getValue().equalsIgnoreCase(tagValue)) {
                toRemove = t;
                break;
            }
        }
        if (toRemove != null) {
            tags.remove(toRemove);
        }
    }

    public boolean hasTag(String name, String value) {
        for (Tag t : tags) {
            if (t.getName().equalsIgnoreCase(name) && t.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String fileName = new File(filePath).getName();
        return fileName + (description.isEmpty() ? "" : " - " + description);
    }

    // Ensure photos with same path are treated as same
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Photo)) return false;
        Photo other = (Photo) obj;
        return filePath != null && filePath.equals(other.filePath);
    }

    @Override
    public int hashCode() {
        return filePath != null ? filePath.hashCode() : 0;
    }
}
