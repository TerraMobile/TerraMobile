/*
 * Geopaparazzi - Digital field mapping on Android based devices
 * Copyright (C) 2010  HydroloGIS (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.org.funcate.dynamicforms.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.Serializable;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.R;
import br.org.funcate.dynamicforms.camera.CameraActivity;
import br.org.funcate.dynamicforms.exceptions.CollectFormException;

import static br.org.funcate.dynamicforms.util.Utilities.messageDialog;

/**
 * Singleton that takes care of resources management.
 * <p/>
 * <p>It creates a folder structure with possible database and log file names.</p>
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ResourcesManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String PATH_MAPS = "maps"; //$NON-NLS-1$

    private static final String PATH_TEMP = "temp"; //$NON-NLS-1$

    /**
     * The nomedia file defining folders that should not be searched for media.
     */
    public static final String NO_MEDIA = ".nomedia"; //$NON-NLS-1$

    /**
     * The support folder for geopap. If not there, it is created.
     * <p/>
     * <p>It has the name of the application and resides in the sdcard.</p>
     * <p>It contains for example the json tags file and temporary files if necessary.
     */
    private File applicationSupportFolder;

    /**
     * The database file for geopap.
     */
    private File databaseFile;

    /**
     * The base path to reference of the temp images folder.
     */
    private String workingAppPath;

    /**
     * The basemaps folder.
     */
    private File mapsDir;

    /**
     * The temporary folder.
     */
    private File tempDir;

    private static ResourcesManager resourcesManager;

    /**
     * The name of the application.
     */
    private String applicationLabel;

    private static boolean useInternalMemory = true;

    private File sdcardDir;

    /**
     * @param useInternalMemory if <code>true</code>, internal memory is used.
     */
    public static void setUseInternalMemory(boolean useInternalMemory) {
        ResourcesManager.useInternalMemory = useInternalMemory;
    }

    /**
     * The getter for the {@link ResourcesManager} singleton.
     * <p/>
     * <p>This is a singleton but might require to be recreated
     * in every moment of the application. This is due to the fact
     * that when the application looses focus (for example because of
     * an incoming call, and therefore at a random moment, if the memory
     * is too low, the parent activity could have been killed by
     * the system in background. In which case we need to recreate it.)
     *
     * @param context the context to refer to.
     * @return the {@link ResourcesManager} instance.
     * @throws Exception if something goes wrong.
     */
    public synchronized static ResourcesManager getInstance(Context context) throws Exception {
        if (resourcesManager == null) {
            resourcesManager = new ResourcesManager(context);
        }
        return resourcesManager;
    }

    /**
     * Reset the {@link ResourcesManager}.
     */
    public static void resetManager() {
        resourcesManager = null;
    }

    /**
     * Getter for the app name.
     *
     * @return the name of the app.
     */
    public String getApplicationName() {
        return applicationLabel;
    }

/*    private ResourcesManager(Context context) throws Exception {
        Context appContext = context.getApplicationContext();
        ApplicationInfo appInfo = appContext.getApplicationInfo();

        String packageName = appInfo.packageName;
        int lastDot = packageName.lastIndexOf('.');
        applicationLabel = packageName.replace('.', '_');
        if (lastDot != -1) {
            applicationLabel = packageName.substring(lastDot + 1, packageName.length());
        }
        applicationLabel = applicationLabel.toLowerCase();

        *//*
         * take care to create all the folders needed
         *
         * The default structure is:
         *
         * sdcard
         *    |
         *    |-- applicationname.gpap -> main database
         *    |-- applicationSupportfolder
         *    |          |
         *    |          |--- temp/ -> temporary files
         *    |          `--- tags.json
         *    |
         *    `-- mapsdir
         *//*
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        String cantCreateSdcardmsg = appContext.getResources().getString(R.string.cantcreate_sdcard);
        String customFolderPath = preferences.getString(PREFS_KEY_CUSTOM_EXTERNALSTORAGE, "asdasdpoipoi");
        customFolderPath = customFolderPath.trim();
        File customFolderFile = new File(customFolderPath);
        if (customFolderFile.exists() && customFolderFile.isDirectory() && customFolderFile.canWrite()) {
            // we can write to the user set folder, let's use it
            sdcardDir = customFolderFile;
            applicationSupportFolder = new File(sdcardDir, applicationLabel);
        } else {
            // checks
            // the folder doesn't exist for some reason, fallback on default
            String state = Environment.getExternalStorageState();
*//*            if (GPLog.LOG_HEAVY) {
                Log.i("RESOURCESMANAGER", state);
            }*//*
            boolean mExternalStorageAvailable;
            boolean mExternalStorageWriteable;
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }

            if (mExternalStorageAvailable && mExternalStorageWriteable) {
                if (customFolderPath.equals("internal")) {
                    *//*
                     * the user folder doesn't exist, but is "internal":
                     * - use internal app memory
                     * - set sdcard anyways to the external folder for maps use
                     *//*
                    useInternalMemory = true;
                    applicationSupportFolder = appContext.getDir(applicationLabel, Context.MODE_PRIVATE);
                    sdcardDir = Environment.getExternalStorageDirectory();
                } else {
                    sdcardDir = Environment.getExternalStorageDirectory();
                    applicationSupportFolder = new File(sdcardDir, applicationLabel);
                }
            } else if (useInternalMemory) {
                *//*
                 * no external storage available:
                 * - use internal memory
                 * - set sdcard for maps inside the space
                 *//*
                applicationSupportFolder = appContext.getDir(applicationLabel, Context.MODE_PRIVATE);
                sdcardDir = applicationSupportFolder;
            } else {
                String msgFormat = Utilities.format(cantCreateSdcardmsg, "sdcard/" + applicationLabel);
                throw new IOException(msgFormat);
            }


        }


        // if (GPLog.LOG_HEAVY) {
        Log.i("RESOURCESMANAGER", "Application support folder: " + applicationSupportFolder);
        // }

        String applicationDirPath = applicationSupportFolder.getAbsolutePath();
        if (!applicationSupportFolder.exists()) {
            if (!applicationSupportFolder.mkdirs()) {
                String msgFormat = Utilities.format(cantCreateSdcardmsg, applicationDirPath);
                throw new IOException(msgFormat);
            }
        }
*//*        if (GPLog.LOG_HEAVY) {
            Log.i("RESOURCESMANAGER", "Application support folder exists: " + applicationSupportFolder.exists());
        }*//*

        *//*
         * get the database file
         *//*
        String databasePath = preferences.getString(PREFS_KEY_DATABASE_TO_LOAD, "asdasdpoipoi");
        databaseFile = new File(databasePath);
        if (databaseFile.getParentFile() == null || !databaseFile.getParentFile().exists()) {
            // fallback on the default
            String databaseName = applicationLabel + LibraryConstants.GEOPAPARAZZI_DB_EXTENSION;
            databaseFile = new File(sdcardDir, databaseName);
        }


        String mapsFolderPath = preferences.getString(PREFS_KEY_CUSTOM_MAPSFOLDER, "asdasdpoipoi");
        mapsFolderPath = mapsFolderPath.trim();
        File customMapsDir = new File(mapsFolderPath);
        if (customMapsDir.exists()) {
            mapsDir = customMapsDir;
        } else {
            mapsDir = new File(sdcardDir, PATH_MAPS);
        }
        if (!mapsDir.exists())
            if (!mapsDir.mkdir()) {
                String msgFormat = Utilities.format(cantCreateSdcardmsg, mapsDir.getAbsolutePath());
                messageDialog(appContext, msgFormat, null);
                mapsDir = sdcardDir;
            }

        tempDir = new File(applicationSupportFolder, PATH_TEMP);
        if (!tempDir.exists())
            if (!tempDir.mkdir()) {
                String msgFormat = Utilities.format(cantCreateSdcardmsg, tempDir.getAbsolutePath());
                messageDialog(appContext, msgFormat, null);
                tempDir = sdcardDir;
            }

        Editor editor = preferences.edit();
        editor.putString(LibraryConstants.PREFS_KEY_CUSTOM_EXTERNALSTORAGE, sdcardDir.getAbsolutePath());
        editor.putString(LibraryConstants.PREFS_KEY_CUSTOM_MAPSFOLDER, mapsDir.getAbsolutePath());
        editor.commit();
    }*/

    private ResourcesManager(Context context) throws Exception {
        Context appContext = context.getApplicationContext();
        String cantCreateSdcardmsg = appContext.getResources().getString(R.string.cantcreate_sdcard);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        workingAppPath = ((CameraActivity) context).getWorkingDirectory();
        if(workingAppPath==null || ("").equals(workingAppPath)) {
            throw new CollectFormException(context.getString(R.string.tmp_folder_not_found));
        }

        //applicationSupportFolder = appContext.getDir(workingAppPath, Context.MODE_PRIVATE);
        applicationSupportFolder = new File(workingAppPath);
        sdcardDir = applicationSupportFolder;

        tempDir = new File(applicationSupportFolder, PATH_TEMP);
        if (!tempDir.exists()) {
            if (!tempDir.mkdir()) {
                String msgFormat = Utilities.format(cantCreateSdcardmsg, tempDir.getAbsolutePath());
                messageDialog(appContext, msgFormat, null);
                tempDir = sdcardDir;
            }
        }

        Editor editor = preferences.edit();
        editor.putString(LibraryConstants.PREFS_KEY_CUSTOM_EXTERNALSTORAGE, tempDir.getAbsolutePath());
        editor.commit();
    }

    /**
     * Get the file to the main application support folder.
     *
     * @return the {@link File} to the app folder.
     */
    public File getApplicationSupporterDir() {
        return applicationSupportFolder;
    }

    /**
     * Get the sdcard dir or <code>null</code>.
     *
     * @return the sdcard folder file.
     */
    public File getSdcardDir() {
        return sdcardDir;
    }

    /**
     * Get the file to a default database location for the app.
     * <p/>
     * <p>This path is generated with default values and can be
     * exploited. It doesn't assure that in the location there really is a db.
     *
     * @return the {@link File} to the database.
     */
    public File getDatabaseFile() {
        return databaseFile;
    }

    /**
     * Get the default maps folder.
     *
     * @return the default maps folder.
     */
    public File getMapsDir() {
        return mapsDir;
    }

    /**
     * Get the temporary folder.
     *
     * @return the temp folder.
     */
    public File getTempDir() {
        return tempDir;
    }

    public String getWorkingDirectory() {
        return workingAppPath;
    }
}
