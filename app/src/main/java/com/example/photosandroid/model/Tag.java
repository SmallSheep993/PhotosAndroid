package com.example.photosandroid.model;

import java.io.Serializable;

/**
 * Represents a tag on a photo, consisting of a tag name and a tag value.
 */
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The tag name (type), e.g., "person" or "location" */
    private String name;

    /** The tag value, e.g., "mom", "new york" */
    private String value;

    /**
     * Constructs a Tag with the given name and value.
     * @param name the tag name (should be either "person" or "location")
     * @param value the tag value (user-defined)
     */
    public Tag(String name, String value) {
        this.name = name.trim().toLowerCase();   // normalized name
        this.value = value.trim();
    }

    /**
     * Gets the tag name (type).
     * @return tag type, e.g., "person"
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the tag value.
     * @return tag value, e.g., "mom"
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts the tag to a display string.
     * Example: 👤 person: mom
     */
    @Override
    public String toString() {
        if (name.equals("person")) {
            return "👤 person: " + value;
        } else if (name.equals("location")) {
            return "📍 location: " + value;
        } else {
            return name + ": " + value; // fallback, should not happen
        }
    }

    /**
     * Tags are considered equal if name and value match (case-insensitive).
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tag)) return false;
        Tag other = (Tag) obj;
        return this.name.equalsIgnoreCase(other.name) &&
                this.value.equalsIgnoreCase(other.value);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode() * 31 + value.toLowerCase().hashCode();
    }
}
