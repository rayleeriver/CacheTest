package com.rayleeriver.cachetest;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final int MAXFILES=500;
    private Bitmap sampleImage;
    List<String> writtenImageFiles = new ArrayList<String>();

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = maxMemory / 8;

    LruCache<String, Bitmap> cachedImageFiles = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSampleImage();
        try {
            writeImagesToLocalStorage();
            readImagesFromLocalStorage();
            deleteImagesFromLocalStorage();

            writeImagesToNewCache();
            readImagesFromCache();
            deleteImagesFromLocalStorage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readImagesFromCache() {
        Date start=new Date();
        for (String filename : writtenImageFiles) {
            cachedImageFiles.get(filename);
        }
        Log.d("debug", "time taken to read " + writtenImageFiles.size() + " files from cache: " + ((new Date()).getTime() - start.getTime()));
    }

    private void writeImagesToNewCache() throws IOException {
        Date start = new Date();
        for (int i = 0; i < MAXFILES; i++) {
            String name = "file_" + i;
            FileOutputStream fos = openFileOutput(name, Context.MODE_PRIVATE);
            sampleImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            cachedImageFiles.put(name, sampleImage);
            writtenImageFiles.add(name);
            fos.close();
        }
        Log.d("debug", "time taken to write to new cache: " + ((new Date()).getTime() - start.getTime()));
    }

    private void readImagesFromLocalStorage() throws FileNotFoundException {
        Date start=new Date();
        for (String filename : writtenImageFiles) {
            FileInputStream fis = openFileInput(filename);
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
        }
        Log.d("debug", "time taken to read: " + ((new Date()).getTime() - start.getTime()));
    }

    private void deleteImagesFromLocalStorage() throws IOException {
        Date start = new Date();
        for (String filename : writtenImageFiles) {
            if (!deleteFile(filename)) {
                throw new IOException("Can't find file " + filename);
            }
        }
        writtenImageFiles.clear();
        Log.d("debug", "time taken to delete: " + ((new Date()).getTime() - start.getTime()));
    }

    private void writeImagesToLocalStorage() throws IOException {
        Date start = new Date();
        for (int i = 0; i < MAXFILES; i++) {
            FileOutputStream fos = openFileOutput("file_" + i, Context.MODE_PRIVATE);
            sampleImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            writtenImageFiles.add("file_" + i);
            fos.close();
        }
        Log.d("debug", "time taken to write: " + ((new Date()).getTime() - start.getTime()));
    }

    private void loadSampleImage() {
        sampleImage = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_btn_speak_now);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
