package com.example.photosandroid.view;

import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photosandroid.R;
import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.FileStorage;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class PhotoSlideshowActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView captionText;
    private Button prevButton, nextButton;

    private List<Photo> photos;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slideshow);

        // Bind UI components
        imageView = findViewById(R.id.slideshowImage);
        captionText = findViewById(R.id.slideshowText);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);

        // Get album name from intent
        String albumName = getIntent().getStringExtra("albumName");

        if (albumName == null) {
            Toast.makeText(this, "No album specified", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Load albums and find the one with matching name
        ArrayList<Album> albums = FileStorage.loadAlbums(this);
        if (albums == null) albums = new ArrayList<>();

        Album selectedAlbum = null;
        for (Album a : albums) {
            if (a.getName().equals(albumName)) {
                selectedAlbum = a;
                break;
            }
        }

        if (selectedAlbum == null) {
            Toast.makeText(this, "Album not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Get photos in album
        photos = selectedAlbum.getPhotos();

        if (photos == null || photos.isEmpty()) {
            Toast.makeText(this, "Album is empty", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Display first photo
        updateSlideshow();

        // Set navigation button actions
        prevButton.setOnClickListener(v -> {
            currentIndex = (currentIndex - 1 + photos.size()) % photos.size();
            updateSlideshow();
        });

        nextButton.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % photos.size();
            updateSlideshow();
        });
    }

    /**
     * Update image and caption based on currentIndex.
     */
    private void updateSlideshow() {
        Photo currentPhoto = photos.get(currentIndex);
        imageView.setImageURI(Uri.parse(currentPhoto.getFilePath()));

        StringBuilder tagText = new StringBuilder();
        for (Tag tag : currentPhoto.getTags()) {
            tagText.append(tag.toString()).append("\n");
        }

        String filename = Uri.parse(currentPhoto.getFilePath()).getLastPathSegment();
        captionText.setText(filename + "\n" + tagText);
    }
}
