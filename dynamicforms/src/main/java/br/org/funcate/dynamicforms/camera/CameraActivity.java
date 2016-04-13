package br.org.funcate.dynamicforms.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.R;
import br.org.funcate.dynamicforms.sensors.OrientationSensor;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.dynamicforms.util.Utilities;


public class CameraActivity extends Activity {

    private static final int ACTION_TAKE_PHOTO = 1;

    private static final String BITMAP_STORAGE_KEY = "view_bitmap";
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;

    private static final String JPEG_FILE_PREFIX = "TM_PHOTO_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private String workingDirectory;
    private OrientationSensor orientationSensor;


    public String getWorkingDirectory() {
        return workingDirectory;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && workingDirectory!=null) {

            storageDir = new File(workingDirectory);

            if (!storageDir.exists()) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();

        if(albumF==null) {
            Runnable runnable = new Runnable() {
                public void run() {
                    finish();
                }
            };
            Utilities.messageDialog(this, getString(R.string.cantcreate_img_folder), runnable);
            return null;
        }

        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();

        if(f!=null) {
            mCurrentPhotoPath = f.getAbsolutePath();
        }

        return f;
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f;
        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, actionCode);
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        try {
            if (extras != null) {

                workingDirectory = extras.getString(FormUtilities.MAIN_APP_WORKING_DIRECTORY);

            } else {
                throw new RuntimeException("Read data failure from who called.");
            }

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            orientationSensor = new OrientationSensor(sensorManager, null);
            orientationSensor.register(this, SensorManager.SENSOR_DELAY_NORMAL);

            if(isIntentAvailable(getApplicationContext(), MediaStore.ACTION_IMAGE_CAPTURE)) {
                dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
            }else{
                throw new RuntimeException("Image Capture not available in your Android.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Intent intent = getIntent();

        if(requestCode==ACTION_TAKE_PHOTO && resultCode == RESULT_OK) {

            intent.putExtra(LibraryConstants.OBJECT_EXISTS, true);
            intent.putExtra(FormUtilities.PHOTO_COMPLETE_PATH, mCurrentPhotoPath);
            double azimuth = orientationSensor.getAzimuthDegrees();
            intent.putExtra(LibraryConstants.AZIMUTH, azimuth);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }

    }
    @Override
    public void finish() {
        orientationSensor.unregister();
        super.finish();
    }


    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
    }

    /**
     * Indicates whether the specified action can be used as an intent. This
     * method queries the package manager for installed packages that can
     * respond to an intent with the specified action. If no suitable package is
     * found, this method returns false.
     * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
     *
     * @param context The application's environment.
     * @param action The Intent action to check for availability.
     *
     * @return True if an Intent with the specified action can be sent and
     *         responded to, false otherwise.
     */
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}