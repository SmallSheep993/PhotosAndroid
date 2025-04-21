package com.example.photosandroid.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.photosandroid.R;
import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.FileStorage;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    // UI components
    private TextView albumTitle;
    private Button addPhotoButton;
    private Button slideshowButton;
    private ListView photoList;

    // Photo and album data
    private List<Photo> photos;
    private PhotoAdapter photoAdapter;
    private Album album;
    private int albumIndex;

    // Photo picker result handler (Activity Result API)
    private final ActivityResultLauncher<Intent> photoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        String filePath = uri.toString();

                        // Avoid adding duplicate photos to the album
                        if (!albumContainsPhoto(filePath)) {
                            Photo newPhoto = new Photo(filePath);
                            album.addPhoto(newPhoto);
                            photoAdapter.notifyDataSetChanged();
                            saveAlbum();
                        } else {
                            Toast.makeText(this, "Photo already exists in album!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Bind UI components
        albumTitle = findViewById(R.id.albumTitle);
        addPhotoButton = findViewById(R.id.addPhotoButton);
        slideshowButton = findViewById(R.id.slideshowButton);
        photoList = findViewById(R.id.photoList);

        // Retrieve album name from intent
        String albumName = getIntent().getStringExtra("albumName");
        if (albumName == null) {
            Toast.makeText(this, "Error: Missing album name", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Load all albums from storage
        ArrayList<Album> allAlbums = FileStorage.loadAlbums(this);
        if (allAlbums == null) allAlbums = new ArrayList<>();

        // Find the album by name
        album = null;
        for (int i = 0; i < allAlbums.size(); i++) {
            if (allAlbums.get(i).getName().equals(albumName)) {
                album = allAlbums.get(i);
                albumIndex = i;
                break;
            }
        }

        // If album not found, exit the activity
        if (album == null) {
            Toast.makeText(this, "Error: Album not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set title and load photos
        photos = album.getPhotos();
        albumTitle.setText(album.getName());

        // Set up the adapter
        // Set up the adapter with current album (for move/delete logic)
        photoAdapter = new PhotoAdapter(this, photos, album, this::saveAlbum);
        photoList.setAdapter(photoAdapter);


        // Add photo button listener
        addPhotoButton.setOnClickListener(v -> openPhotoPicker());

        // Launch slideshow activity
        slideshowButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhotoSlideshowActivity.class);
            intent.putExtra("albumName", album.getName());
            startActivity(intent);
        });

        // Click photo: show options like add tag or delete
        photoList.setOnItemClickListener((parent, view, position, id) -> {
            String[] options = {"Add Tag", "Delete Photo"};

            new AlertDialog.Builder(this)
                    .setTitle("Choose Action")
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                showAddTagDialog(position);
                                break;
                            case 1:
                                showDeletePhotoDialog(position);
                                break;
                        }
                    })
                    .show();
        });
    }

    /**
     * Launches the system photo picker using ACTION_OPEN_DOCUMENT.
     */
    private void openPhotoPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        photoPickerLauncher.launch(intent);
    }

    /**
     * Show confirmation dialog to delete selected photo.
     */
    private void showDeletePhotoDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Photo photoToRemove = photos.get(position);
                    album.removePhoto(photoToRemove);
                    photoAdapter.notifyDataSetChanged();
                    saveAlbum();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Prompt user to add a tag (name and value) to the selected photo.
     */
    private void showAddTagDialog(int position) {
        Photo photo = photos.get(position);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText tagNameInput = new EditText(this);
        tagNameInput.setHint("Tag Name (e.g., person)");
        layout.addView(tagNameInput);

        final EditText tagValueInput = new EditText(this);
        tagValueInput.setHint("Tag Value (e.g., Alice)");
        layout.addView(tagValueInput);

        new AlertDialog.Builder(this)
                .setTitle("Add Tag")
                .setView(layout)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = tagNameInput.getText().toString().trim();
                    String value = tagValueInput.getText().toString().trim();

                    if (!name.isEmpty() && !value.isEmpty()) {
                        boolean added = photo.addTag(name, value);
                        if (added) {
                            Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
                            saveAlbum();
                            photoAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "Duplicate or invalid tag", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Check if photo with same path already exists in current album.
     */
    private boolean albumContainsPhoto(String filePath) {
        for (Photo p : photos) {
            if (p.getFilePath().equals(filePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Save the current album list with updated album state.
     */
    private void saveAlbum() {
        ArrayList<Album> allAlbums = FileStorage.loadAlbums(this);
        if (allAlbums == null) allAlbums = new ArrayList<>();
        allAlbums.set(albumIndex, album);
        FileStorage.saveAlbums(this, allAlbums);
    }

    /**
     * Persist album data when the activity is stopped.
     */
    @Override
    protected void onStop() {
        super.onStop();
        saveAlbum();
    }

    /**
     * Return the index of the currently viewed album.
     */
    public int getAlbumIndex() {
        return albumIndex;
    }
}
