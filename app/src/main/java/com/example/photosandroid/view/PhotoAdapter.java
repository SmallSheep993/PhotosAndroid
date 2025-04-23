package com.example.photosandroid.view;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.view.*;
import android.widget.*;

import com.example.photosandroid.R;
import com.example.photosandroid.model.Album;
import com.example.photosandroid.model.FileStorage;
import com.example.photosandroid.model.Photo;
import com.example.photosandroid.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends ArrayAdapter<Photo> {
    private final Context context;
    private final List<Photo> photos;
    private final Album currentAlbum; // Needed for actual photo removal
    private final Runnable saveCallback;

    public PhotoAdapter(Context context, List<Photo> photos, Album currentAlbum, Runnable saveCallback) {
        super(context, 0, photos);
        this.context = context;
        this.photos = photos;
        this.currentAlbum = currentAlbum;
        this.saveCallback = saveCallback;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Photo photo = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        }

        ImageView photoImage = convertView.findViewById(R.id.photoImage);
        TextView photoText = convertView.findViewById(R.id.photoText);
        Button addTagButton = convertView.findViewById(R.id.addTagButton);
        Button deleteTagButton = convertView.findViewById(R.id.deleteTagButton);
        Button moveButton = convertView.findViewById(R.id.movePhotoButton);
        ImageButton deletePhotoButton = convertView.findViewById(R.id.deletePhotoButton);

        photoImage.setImageURI(Uri.parse(photo.getFilePath()));

        StringBuilder tagBuilder = new StringBuilder("image: " + position);
        for (Tag tag : photo.getTags()) {
            tagBuilder.append("\n").append(tag.toString());
        }
        photoText.setText(tagBuilder.toString());

        // ➕ Add Tag
        addTagButton.setOnClickListener(v -> {
            LinearLayout layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            EditText personInput = new EditText(context);
            personInput.setHint("Person");
            layout.addView(personInput);
            EditText locationInput = new EditText(context);
            locationInput.setHint("Location");
            layout.addView(locationInput);

            new AlertDialog.Builder(context)
                    .setTitle("Add Tags")
                    .setView(layout)
                    .setPositiveButton("Add", (dialog, which) -> {
                        boolean added = false;
                        String person = personInput.getText().toString().trim();
                        String location = locationInput.getText().toString().trim();

                        if (!person.isEmpty()) added |= photo.addTag("person", person);
                        if (!location.isEmpty()) added |= photo.addTag("location", location);

                        if (added) {
                            Toast.makeText(context, "Tag(s) added", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                            saveCallback.run();
                        } else {
                            Toast.makeText(context, "Nothing added", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Delete Tag
        deleteTagButton.setOnClickListener(v -> {
            List<Tag> tags = photo.getTags();
            if (tags.isEmpty()) {
                Toast.makeText(context, "No tags to delete", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] tagStrings = new String[tags.size()];
            for (int i = 0; i < tags.size(); i++) {
                tagStrings[i] = tags.get(i).toString();
            }

            new AlertDialog.Builder(context)
                    .setTitle("Delete Tag")
                    .setItems(tagStrings, (dialog, which) -> {
                        Tag tag = tags.get(which);
                        photo.removeTag(tag.getName(), tag.getValue());
                        notifyDataSetChanged();
                        saveCallback.run();
                        Toast.makeText(context, "Tag deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Move Photo to another album
        moveButton.setOnClickListener(v -> {
            ArrayList<Album> albums = FileStorage.loadAlbums(context);
            String[] albumNames = new String[albums.size()];
            for (int i = 0; i < albums.size(); i++) {
                albumNames[i] = albums.get(i).getName();
            }

            new AlertDialog.Builder(context)
                    .setTitle("Move Photo To...")
                    .setItems(albumNames, (dialog, which) -> {
                        Album targetAlbum = albums.get(which);

                        if (!targetAlbum.containsPhoto(photo)) {
                            targetAlbum.addPhoto(photo);

                            // Remove from current album (runtime)
                            currentAlbum.removePhoto(photo);
                            photos.remove(photo);
                            notifyDataSetChanged();
                            Toast.makeText(context, "Photo moved to " + targetAlbum.getName(), Toast.LENGTH_SHORT).show();

                            // Update albums list with updated album objects
                            for (int i = 0; i < albums.size(); i++) {
                                Album a = albums.get(i);
                                if (a.getName().equals(currentAlbum.getName())) {
                                    albums.set(i, currentAlbum);
                                } else if (a.getName().equals(targetAlbum.getName())) {
                                    albums.set(i, targetAlbum);
                                }
                            }

                            // Save updated album list
                            FileStorage.saveAlbums(context, albums);

                            // Notify parent activity to save if needed
                            saveCallback.run();
                        } else {
                            Toast.makeText(context, "Photo already exists in selected album", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });


        // Delete photo from current album
        deletePhotoButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Photo")
                    .setMessage("Are you sure you want to delete this photo?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        currentAlbum.removePhoto(photo);
                        photos.remove(photo);
                        notifyDataSetChanged();
                        saveCallback.run();
                        Toast.makeText(context, "Photo deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return convertView;
    }
}
