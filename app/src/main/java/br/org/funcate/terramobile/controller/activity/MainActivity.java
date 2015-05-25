/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.org.funcate.terramobile.controller.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.FragmentDetailActivity;
import br.org.funcate.dynamicforms.TagsManager;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.dynamicforms.util.PositionUtilities;
import br.org.funcate.dynamicforms.util.Utilities;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.configuration.ViewContextParameters;
import br.org.funcate.terramobile.model.exception.DownloadException;
import br.org.funcate.terramobile.util.ResourceUtil;


/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class MainActivity extends FragmentActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private TreeView treeView;

    private ViewContextParameters parameters=new ViewContextParameters();

    // this members is used in dynamic form process.
    private static final String USE_MAPCENTER_POSITION = "USE_MAPCENTER_POSITION";
    private static final int FORM_RETURN_CODE = 669;
    private double latitude;
    private double longitude;
    private double elevation;
    private double[] gpsLocation;
    private final int RETURNCODE_DETAILACTIVITY = 665;
    // --------------------------------------------

    // Progress Dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        treeView = new TreeView(MainActivity.this);

        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // set a custom shadow that overlays the action_bar content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);

        int ActionBarTitleID = getResources().getSystem().getIdentifier("action_bar_title", "id", "android");
        TextView ActionBarTextView = (TextView) this.findViewById(ActionBarTitleID);
        ActionBarTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.title_text_size));

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // ActionBarDrawerToggle ties together the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  //host Activity
                mDrawerLayout,         //DrawerLayout object
                R.drawable.ic_drawer,  //nav drawer image to replace 'Up' caret
                R.string.drawer_open,  //"open drawer" description for accessibility
                R.string.drawer_close  //"close drawer" description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            insertMapView();
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
        System.exit(0);
    }

    public ViewContextParameters getParameters(){
        return parameters;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ExpandableListView mDrawerList=treeView.getUIComponent();
        if(mDrawerList==null) return false;
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * This method is called when item from action bar is selected.
     * @param item, the menu item component
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.download_geo_package:
                ConnectivityManager cm = (ConnectivityManager)this.getSystemService(this.CONNECTIVITY_SERVICE);

                int wifi = ConnectivityManager.TYPE_WIFI;
                int mobile = ConnectivityManager.TYPE_MOBILE;

                if (cm.getNetworkInfo(mobile).isConnected() ||
                        cm.getNetworkInfo(wifi).isConnected()) {
                    File appPath = ResourceUtil.getDirectory(getResources().getString(R.string.app_workspace_dir));
                    String tempURL = getResources().getString(R.string.gpkg_url);
                    String destinationFilePath = appPath.getPath();
                    new DownloadTask(destinationFilePath, true).execute(tempURL);
                }
                else{
                    Toast.makeText(this, "Conecte-se à internet", Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.acquire_new_point:
                startForm();
                return true;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void startForm() {
        // TODO: this name provided from the configuration of the "collect layer" in database.
        String sectionName = "terramobile";
        String selectedItemName = "crop features";

        checkPositionCoordinates();

        // insert note and then work on it
        try {
            JSONObject sectionObject = TagsManager.getInstance(MainActivity.this).getSectionByName(sectionName);
            String sectionObjectString = sectionObject.toString();

            long noteId = 123543;// fake number. remove this in future.

            // launch form activity
           /* Intent formIntent = new Intent(MainActivity.this, FormActivity.class);
            formIntent.putExtra(LibraryConstants.DATABASE_ID, noteId);
            formIntent.putExtra(LibraryConstants.PREFS_KEY_FORM_NAME, sectionName);
            formIntent.putExtra(LibraryConstants.LATITUDE, latitude);
            formIntent.putExtra(LibraryConstants.LONGITUDE, longitude);
            formIntent.putExtra(LibraryConstants.ELEVATION, elevation);
            startActivityForResult(formIntent, FORM_RETURN_CODE);*/

            Intent formIntent = new Intent(MainActivity.this, FragmentDetailActivity.class);
            formIntent.putExtra(LibraryConstants.DATABASE_ID, noteId);
            formIntent.putExtra(FormUtilities.ATTR_FORMNAME, selectedItemName);
            formIntent.putExtra(FormUtilities.ATTR_SECTIONOBJECTSTR, sectionObjectString);
            formIntent.putExtra(LibraryConstants.LONGITUDE, longitude);
            formIntent.putExtra(LibraryConstants.LATITUDE, latitude);
            startActivityForResult(formIntent, RETURNCODE_DETAILACTIVITY);


        } catch (Exception e) {
            Utilities.messageDialog(MainActivity.this, "falhou ao abrir formulário de coleta de dados." + e.getMessage(), null);
        }
    }

    private void checkPositionCoordinates() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useMapCenterPosition = preferences.getBoolean(USE_MAPCENTER_POSITION, false);
        if (useMapCenterPosition || gpsLocation == null) {
            double[] mapCenter = PositionUtilities.getMapCenterFromPreferences(preferences, true, true);
            latitude = mapCenter[1];
            longitude = mapCenter[0];
            elevation = 0.0;
        } else {
            latitude = gpsLocation[1];
            longitude = gpsLocation[0];
            elevation = gpsLocation[2];
        }
    }

    private void insertMapView() {
        // update the action_bar content by replacing fragments
        Fragment fragment = new MapFragment();
/*        Bundle args = new Bundle();
        args.putStringArrayList(MapFragment.mLayers);
        fragment.setArguments(args);*/
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    protected void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.downloding));
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    class DownloadTask extends AsyncTask<String, String, Boolean> {

        private String unzipDestinationFilePath;
        private String downloadDestinationFilePath;

        private DownloadException exception;

        private boolean overwrite;

        public DownloadTask(String unzipDestinationFilePath, boolean overwrite) {
            this.unzipDestinationFilePath = unzipDestinationFilePath;
            this.downloadDestinationFilePath = unzipDestinationFilePath + "/" + getResources().getString(R.string.destination_file_path);
            this.overwrite = overwrite;
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        protected Boolean doInBackground(String... urlToDownload) {
            if (urlToDownload[0].isEmpty()) {
                exception = new DownloadException("Missing URL to be downloaded.");
                return false;
            }

            if (downloadDestinationFilePath.isEmpty()) {
                exception = new DownloadException("Missing destination path to download to.");
                return false;
            }

            try {
                try {
                    File file = new File(downloadDestinationFilePath);

                    if (!file.exists()) {
                        file.createNewFile();
                    } else {
                        if (overwrite) {
                            file.delete();
                        } else {
                            return true;
                        }
                    }
                    URL url = new URL(urlToDownload[0]);

                    URLConnection urlConnection = url.openConnection();
                    urlConnection.connect();

                    int totalSize = urlConnection.getContentLength();

                    InputStream inputStream = new BufferedInputStream(url.openStream());

                    OutputStream fileOutput = new FileOutputStream(file);

                    byte buffer[] = new byte[1024];

                    int bufferLength;

                    long total = 0;

//                    if(android.os.Debug.isDebuggerConnected()) android.os.Debug.waitForDebugger(); Para debugar é preciso colocar um breakpoint nessa linha

                    while ((bufferLength = inputStream.read(buffer)) != -1) {
                        total += bufferLength;
                        publishProgress("" + (int) ((total * 100) / totalSize), getResources().getString(R.string.downloding));

                        fileOutput.write(buffer, 0, bufferLength);
                    }
                    fileOutput.flush();

                    fileOutput.close();

                    this.unzip(new File(downloadDestinationFilePath), new File(unzipDestinationFilePath));

                    return true;

                } catch (IOException e) {
                    throw new DownloadException("Error downloading file: " + urlToDownload[0], e);
                }

            } catch (DownloadException e) {
                exception = e;
            }
            if(progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            return false;
        }

        private long countZipFiles(File zipFile){
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(
                        new BufferedInputStream(new FileInputStream(zipFile)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            long totalFiles = 0;
            try {
                while (zis.getNextEntry() != null) {
                    totalFiles++;
                }
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  totalFiles;
        }

        public void unzip(File zipFile, File targetDirectory) throws IOException {
            ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(new FileInputStream(zipFile)));
            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                int numFiles = 0;
                long totalFiles = countZipFiles(zipFile);

                while ((ze = zis.getNextEntry()) != null) {
                    numFiles++;

                    File file = new File(targetDirectory, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs())
                        throw new FileNotFoundException("Failed to ensure directory: " +
                                dir.getAbsolutePath());
                    if (ze.isDirectory())
                        continue;
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        long total = 0;
                        long totalZipSize = ze.getCompressedSize();
                        while ((count = zis.read(buffer)) != -1) {
                            total += count;
                            publishProgress("" + (int) ((total * 100) / totalZipSize), getResources().getString(R.string.decompressing)+"\n"+getResources().getString(R.string.file) + " " + numFiles + "/" + totalFiles);
                                    fout.write(buffer, 0, count);
                        }
                    } finally {
                        fout.close();
                    }
                }
            } finally {
                zis.close();
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            treeView.refreshTreeView();
            if(progressDialog != null && progressDialog.isShowing()) {
                if (aBoolean) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.download_success, Toast.LENGTH_LONG).show();
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(MainActivity.this, R.string.download_failed, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if(progressDialog != null && progressDialog.isShowing()) {
                progressDialog.setProgress(Integer.parseInt(values[0]));
                progressDialog.setMessage(values[1]);
            }
        }

        public DownloadException getException() {
            return exception;
        }
    }
}