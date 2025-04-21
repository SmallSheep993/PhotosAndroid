package com.example.photosandroid.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.example.photosandroid.R;
import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.FileStorage;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.Tag;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Activity for searching photos by tag pairs.
 */
public class SearchActivity extends AppCompatActivity {

    private Spinner spinnerTag1;
    private AutoCompleteTextView valueInput1;
    private Spinner spinnerTag2;
    private AutoCompleteTextView valueInput2;
    private RadioButton andRadio;
    private RadioButton orRadio;
    private Button searchButton;
    private ListView resultsListView;

    // All albums loaded from storage
    private ArrayList<Album> albums;
    // Suggestions sets for person and location tags
    private Set<String> personTags;
    private Set<String> locationTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        spinnerTag1 = findViewById(R.id.spinnerTag1);
        valueInput1 = findViewById(R.id.valueInput1);
        spinnerTag2 = findViewById(R.id.spinnerTag2);
        valueInput2 = findViewById(R.id.valueInput2);
        andRadio = findViewById(R.id.andRadio);
        orRadio = findViewById(R.id.orRadio);
        searchButton = findViewById(R.id.searchButton);
        resultsListView = findViewById(R.id.resultsListView);

        // Prepare tag type choices for spinners (person or location)
        ArrayAdapter<CharSequence> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"person", "location"});
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTag1.setAdapter(typeAdapter);
        spinnerTag2.setAdapter(typeAdapter);

        // Load all albums and gather all existing tag values for suggestions
        albums = FileStorage.loadAlbums(this);
        if (albums == null) {
            albums = new ArrayList<>();
        }
        personTags = new HashSet<>();
        locationTags = new HashSet<>();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    if (tag.getName().equalsIgnoreCase("person")) {
                        personTags.add(tag.getValue());
                    } else if (tag.getName().equalsIgnoreCase("location")) {
                        locationTags.add(tag.getValue());
                    }
                }
            }
        }

        // Set up adapters for auto-complete suggestions
        ArrayAdapter<String> personAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>(personTags));
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, new ArrayList<>(locationTags));

        // Configure auto-complete text fields to use appropriate suggestions based on selected tag type
        spinnerTag1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerTag1.getSelectedItem().toString().equals("person")) {
                    valueInput1.setAdapter(personAdapter);
                } else {
                    valueInput1.setAdapter(locationAdapter);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinnerTag2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerTag2.getSelectedItem().toString().equals("person")) {
                    valueInput2.setAdapter(personAdapter);
                } else {
                    valueInput2.setAdapter(locationAdapter);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        // Set default adapters (both start as "person" by default)
        valueInput1.setAdapter(personAdapter);
        valueInput2.setAdapter(personAdapter);

        // Handle Search button click
        searchButton.setOnClickListener(v -> performSearch());
    }

    /**
     * Perform the photo search based on the selected tag(s) and mode.
     */
    private void performSearch() {
        String type1 = spinnerTag1.getSelectedItem().toString();
        String value1 = valueInput1.getText().toString().trim();
        String type2 = spinnerTag2.getSelectedItem().toString();
        String value2 = valueInput2.getText().toString().trim();
        boolean useAnd = andRadio.isChecked();

        if (value1.isEmpty() && value2.isEmpty()) {
            Toast.makeText(this, "Please enter at least one tag to search", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gather matching photos across all albums
        List<PhotoResult> results = new ArrayList<>();
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                // Check single-tag cases
                if (value2.isEmpty() && !value1.isEmpty()) {
                    if (photo.hasTag(type1, value1)) {
                        results.add(new PhotoResult(album.getName(), photo));
                    }
                } else if (value1.isEmpty() && !value2.isEmpty()) {
                    if (photo.hasTag(type2, value2)) {
                        results.add(new PhotoResult(album.getName(), photo));
                    }
                }
                // Check two-tag cases (AND vs OR)
                else if (!value1.isEmpty() && !value2.isEmpty()) {
                    boolean match1 = photo.hasTag(type1, value1);
                    boolean match2 = photo.hasTag(type2, value2);
                    if (useAnd) {
                        if (match1 && match2) {
                            results.add(new PhotoResult(album.getName(), photo));
                        }
                    } else {
                        if (match1 || match2) {
                            results.add(new PhotoResult(album.getName(), photo));
                        }
                    }
                }
            }
        }

        // Display results in the ListView
        SearchResultAdapter adapter = new SearchResultAdapter(this, results);
        resultsListView.setAdapter(adapter);
        if (results.isEmpty()) {
            Toast.makeText(this, "No photos found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, results.size() + " photos found", Toast.LENGTH_SHORT).show();
        }

        // Allow clicking a result to view the photo and its tags in a dialog
        resultsListView.setOnItemClickListener((parent, view, position, id) -> {
            PhotoResult selected = results.get(position);
            showPhotoDialog(selected);
        });
    }

    /**
     * Show an alert dialog with the selected photo (full image) and its tags.
     */
    private void showPhotoDialog(PhotoResult result) {
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(Uri.parse(result.photo.getFilePath()));
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(10, 10, 10, 10);

        // Build a string of all tags to display
        StringBuilder tagInfo = new StringBuilder();
        for (Tag tag : result.photo.getTags()) {
            tagInfo.append(tag.toString()).append("\n");
        }
        TextView tagText = new TextView(this);
        tagText.setText(tagInfo.toString().trim());
        tagText.setPadding(10, 10, 10, 10);

        // Vertical layout for image and tag text
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(imageView);
        layout.addView(tagText);

        new AlertDialog.Builder(this)
                .setTitle("Photo: " + result.getPhotoFileName())
                .setView(layout)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Helper class to hold a photo and its album name for search results.
     */
    private static class PhotoResult {
        String albumName;
        Photo photo;
        PhotoResult(String albumName, Photo photo) {
            this.albumName = albumName;
            this.photo = photo;
        }
        String getPhotoFileName() {
            // Extract just the file name from the photo's file path
            String path = photo.getFilePath();
            int idx = path.lastIndexOf('/');
            return (idx >= 0 && idx < path.length() - 1) ? path.substring(idx + 1) : path;
        }
    }

    /**
     * Custom ArrayAdapter for displaying search results (photo thumbnail and info).
     */
    private class SearchResultAdapter extends ArrayAdapter<PhotoResult> {
        public SearchResultAdapter(SearchActivity context, List<PhotoResult> results) {
            super(context, 0, results);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PhotoResult result = getItem(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_search_photo, parent, false);
            }
            ImageView image = convertView.findViewById(R.id.photoImage);
            TextView infoText = convertView.findViewById(R.id.searchText);
            // Set thumbnail image
            image.setImageURI(Uri.parse(result.photo.getFilePath()));
            // Build info text: Album name, file name, and tags
            StringBuilder infoBuilder = new StringBuilder();
            infoBuilder.append("Album: ").append(result.albumName);
            String fileName = Uri.parse(result.photo.getFilePath()).getLastPathSegment();
            if (fileName != null) {
                infoBuilder.append("\n").append(fileName);
            }
            for (Tag tag : result.photo.getTags()) {
                infoBuilder.append("\n").append(tag.toString());
            }
            infoText.setText(infoBuilder.toString());
            return convertView;
        }
    }
}
