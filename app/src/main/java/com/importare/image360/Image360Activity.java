package com.importare.image360;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.vr.ndk.base.AndroidCompat;
import com.google.vr.sdk.widgets.pano.VrPanoramaEventListener;
import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import com.google.vr.sdk.widgets.pano.VrPanoramaView.Options;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Image360Activity extends AppCompatActivity {

    private static final String TAG = Image360Activity.class.getSimpleName();
    private VrPanoramaView panoWidgetView;
    public boolean loadImageSuccessful;
    private Uri fileUri;
    /** Configuration information for the panorama. **/
    private Options panoOptions = new Options();
    private ImageLoaderTask backgroundImageLoaderTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image360);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        AndroidCompat.setVrModeEnabled(this,true);

        panoWidgetView = (VrPanoramaView) findViewById(R.id.pano_view);

        panoWidgetView.setFullscreenButtonEnabled(false);
        panoWidgetView.setInfoButtonEnabled(false);
        panoWidgetView.setEventListener(new VrPanoramaEventListener(){
            @Override
            public void onDisplayModeChanged(int newDisplayMode){
                Log.d("activity360", "displayMode cambiado a " + newDisplayMode);
                super.onDisplayModeChanged(newDisplayMode);
                irFullScreen();
            }
        });


        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new image.
        handleIntent(intent);
    }

    private void irFullScreen(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION //CAMBIO por petición cliente
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION //CAMBIO por petición cliente
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }


    private void handleIntent(Intent intent) {
        // Determine if the Intent contains a file to load.
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.i(TAG, "ACTION_VIEW Intent recieved");

            fileUri = intent.getData();
            if (fileUri == null) {
                Log.w(TAG, "No data uri specified. Use \"-d /path/filename\".");
            } else {
                Log.i(TAG, "Using file " + fileUri.toString());
            }

            panoOptions.inputType = intent.getIntExtra("inputType", Options.TYPE_MONO);
            Log.i(TAG, "Options.inputType = " + panoOptions.inputType);
        } else {
            Log.i(TAG, "Intent is not ACTION_VIEW. Using default pano image.");
            fileUri = null;
            panoOptions.inputType = Options.TYPE_MONO;
        }

        // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
        // take 100s of milliseconds.
        if (backgroundImageLoaderTask != null) {
            // Cancel any task from a previous intent sent to this activity.
            backgroundImageLoaderTask.cancel(true);
        }
        backgroundImageLoaderTask = new ImageLoaderTask();
        backgroundImageLoaderTask.execute(Pair.create(fileUri, panoOptions));
    }


    @Override
    protected void onPause() {
        panoWidgetView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        panoWidgetView.resumeRendering();
        irFullScreen();
    }

    @Override
    protected void onDestroy() {
        // Destroy the widget and free memory.
        panoWidgetView.shutdown();

        // The background task has a 5 second timeout so it can potentially stay alive for 5 seconds
        // after the activity is destroyed unless it is explicitly cancelled.
        if (backgroundImageLoaderTask != null) {
            backgroundImageLoaderTask.cancel(true);
        }
        super.onDestroy();
    }


    class ImageLoaderTask extends AsyncTask<Pair<Uri, Options>, Void, Boolean> {

        /**
         * Reads the bitmap from disk in the background and waits until it's loaded by pano widget.
         */
        @Override
        protected Boolean doInBackground(Pair<Uri, Options>... fileInformation) {
            Options panoOptions = null;  // It's safe to use null VrPanoramaView.Options.
            InputStream istr = null;
            if (fileInformation == null || fileInformation.length < 1
                    || fileInformation[0] == null || fileInformation[0].first == null) {
                //AssetManager assetManager = getAssets();
                //istr = assetManager.open("andes.jpg");
                istr = Image360Activity.this.getResources().openRawResource(R.drawable.quirofano);
                panoOptions = new Options();
                panoOptions.inputType = Options.TYPE_STEREO_OVER_UNDER;
            } else {
                try {
                    istr = new FileInputStream(new File(fileInformation[0].first.getPath()));
                    panoOptions = fileInformation[0].second;
                } catch (IOException e) {
                    Log.e(TAG, "Could not load file: " + e);
                    return false;
                }
            }

            panoWidgetView.loadImageFromBitmap(BitmapFactory.decodeStream(istr), panoOptions);
            try {
                istr.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close input stream: " + e);
            }

            return true;
        }
    }

    private class ActivityEventListener extends VrPanoramaEventListener {
        /**
         * Called by pano widget on the UI thread when it's done loading the image.
         */
        @Override
        public void onLoadSuccess() {
            loadImageSuccessful = true;
        }

        /**
         * Called by pano widget on the UI thread on any asynchronous error.
         */
        @Override
        public void onLoadError(String errorMessage) {
            loadImageSuccessful = false;
            Toast.makeText(
                    Image360Activity.this, "Error loading pano: " + errorMessage, Toast.LENGTH_LONG)
                    .show();
            Log.e(TAG, "Error loading pano: " + errorMessage);
        }
    }

}
