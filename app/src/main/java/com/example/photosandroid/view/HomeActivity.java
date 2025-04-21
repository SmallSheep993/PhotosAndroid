package com.example.photosandroid.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.photosandroid.R;
import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.FileStorage;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    // UI elements
    private ListView albumListView;
    private Button addAlbumButton;

    // App data
    private ArrayList<Album> albums;           // List of album objects
    private ArrayList<String> albumNames;      // Album names (used for display in ListView)
    private ArrayAdapter<String> albumAdapter; // Adapter to connect albumNames to the ListView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI components
        albumListView = findViewById(R.id.albumListView);
        addAlbumButton = findViewById(R.id.addAlbumButton);

        // Load album data from file storage
        albums = FileStorage.loadAlbums(this);
        if (albums == null) albums = new ArrayList<>();

        // Extract album names from loaded albums
        albumNames = new ArrayList<>();
        for (Album a : albums) {
            albumNames.add(a.getName());
        }

        // Set up the adapter and bind it to the ListView
        albumAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, albumNames);
        albumListView.setAdapter(albumAdapter);

        // Listener for creating new album
        addAlbumButton.setOnClickListener(v -> showAddAlbumDialog());

        // Listener for tapping on an album (opens options: open, rename, delete)
        albumListView.setOnItemClickListener((parent, view, position, id) -> {
            showAlbumOptionsDialog(position);
        });

        // 🔍 Setup search button
        Button searchPhotosButton = findViewById(R.id.searchPhotosButton);
        searchPhotosButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

    }

    /**
     * Show a dialog to create a new album.
     * Ensures the album name is not empty and does not already exist.
     */
    private void showAddAlbumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Album Name");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String albumName = input.getText().toString().trim();
            if (!albumName.isEmpty() && !albumExists(albumName)) {
                Album newAlbum = new Album(albumName);
                albums.add(newAlbum);
                albumNames.add(albumName);
                albumAdapter.notifyDataSetChanged();
                FileStorage.saveAlbums(this, albums);
            } else {
                Toast.makeText(this, "Invalid or duplicate name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Show a dialog to rename an existing album.
     * Ensures the new name is valid and not already used by another album.
     */
    private void showRenameAlbumDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Album");

        final EditText input = new EditText(this);
        input.setText(albums.get(position).getName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();

            if (!newName.isEmpty() && !albumExists(newName)) {
                albums.get(position).setName(newName);
                albumNames.set(position, newName);
                albumAdapter.notifyDataSetChanged();
                FileStorage.saveAlbums(this, albums);
            } else {
                Toast.makeText(this, "Invalid or duplicate name", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Show a dialog with options: open album, rename album, or delete album.
     */
    private void showAlbumOptionsDialog(int position) {
        String[] options = {"Open Album", "Rename Album", "Delete Album"};

        new AlertDialog.Builder(this)
                .setTitle("Choose Action")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Open Album
                            Intent intent = new Intent(HomeActivity.this, AlbumActivity.class);
                            intent.putExtra("albumName", albums.get(position).getName());
                            startActivity(intent);
                            break;

                        case 1: // Rename Album
                            showRenameAlbumDialog(position);
                            break;

                        case 2: // Delete Album
                            new AlertDialog.Builder(this)
                                    .setTitle("Delete Album")
                                    .setMessage("Are you sure you want to delete \"" + albums.get(position).getName() + "\"?")
                                    .setPositiveButton("Delete", (d, w) -> {
                                        albums.remove(position);
                                        albumNames.remove(position);
                                        albumAdapter.notifyDataSetChanged();
                                        FileStorage.saveAlbums(this, albums);
                                        Toast.makeText(this, "Album deleted", Toast.LENGTH_SHORT).show();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            break;
                    }
                })
                .show();
    }

    /**
     * Check if an album with the same name already exists (case-insensitive).
     */
    private boolean albumExists(String name) {
        for (Album a : albums) {
            if (a.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Save album data when leaving the activity (for safety).
     */
    @Override
    protected void onStop() {
        super.onStop();
        FileStorage.saveAlbums(this, albums);
        Log.d("DEBUG", "Albums saved in onStop, total: " + albums.size());
    }
}
