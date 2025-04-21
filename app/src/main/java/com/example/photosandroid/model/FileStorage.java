package com.example.photosandroid.model;

import android.content.Context;
import java.io.*;
import java.util.ArrayList;

public class FileStorage {
    private static final String FILENAME = "albums.dat";

    public static void saveAlbums(Context context, ArrayList<Album> albums) {
        try {
            FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(albums);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Album> loadAlbums(Context context) {
        ArrayList<Album> albums = new ArrayList<>();
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            albums = (ArrayList<Album>) ois.readObject();
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            // first time app runs — ignore
        } catch (Exception e) {
            e.printStackTrace();
        }
        return albums;
    }
}
