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
package br.org.funcate.dynamicforms;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.dynamicforms.util.Utilities;
import br.org.funcate.dynamicforms.views.GPictureView;

import static br.org.funcate.dynamicforms.FormUtilities.TAG_KEY;
import static br.org.funcate.dynamicforms.FormUtilities.TAG_TYPE;
import static br.org.funcate.dynamicforms.FormUtilities.TAG_VALUE;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_BOOLEAN;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_CONNECTEDSTRINGCOMBO;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_DATE;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_DOUBLE;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_INTEGER;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_LABEL;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_LABELWITHLINE;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_PICTURES;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_STRING;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_STRINGAREA;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_STRINGCOMBO;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_STRINGMULTIPLECHOICE;
import static br.org.funcate.dynamicforms.FormUtilities.TYPE_TIME;

/**
 * Fragment detail view activity.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FragmentDetailActivity extends FragmentActivity {
    private String formName;
    private JSONObject sectionObject;
    private long pointId;
    private String workingDirectory;
    private Bundle existingFeatureData;
    // --------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // don't permit rotation
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        String tags="";
        String defaultSectionName = FormUtilities.DEFAULT_SESSION_NAME;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Double latitude=0.0;
        Double longitude=0.0;
        if (extras != null) {
            pointId = extras.getLong(LibraryConstants.SELECTED_POINT_ID);
            formName = extras.getString(FormUtilities.ATTR_FORMNAME);
            tags = extras.getString(FormUtilities.ATTR_JSON_TAGS);
            if(extras.containsKey(FormUtilities.TYPE_LATITUDE)) {
                latitude = extras.getDouble(FormUtilities.TYPE_LATITUDE);
                longitude = extras.getDouble(FormUtilities.TYPE_LONGITUDE);
            }
            // here are the attribute values from feature to populate form in edit operation
            if(extras.containsKey(FormUtilities.ATTR_DATA_VALUES)) {
                existingFeatureData = extras.getBundle(FormUtilities.ATTR_DATA_VALUES);
            }
            workingDirectory = extras.getString(FormUtilities.MAIN_APP_WORKING_DIRECTORY);
        }

        try {
            sectionObject = TagsManager.getInstance(tags).getSectionByName(defaultSectionName);

            if(!sectionObject.has(FormUtilities.GEOJSON_TAG_GEOM)) {
                JSONObject geojson = new JSONObject();
                geojson.put(FormUtilities.GEOJSON_TAG_TYPE,FormUtilities.GEOJSON_TYPE_POINT);
                JSONArray coords = new JSONArray("["+longitude+","+latitude+"]");
                geojson.put(FormUtilities.GEOJSON_TAG_COORDINATES, coords);
                sectionObject.put(FormUtilities.GEOJSON_TAG_GEOM, geojson);

            }else{
                JSONObject geojson = sectionObject.getJSONObject(FormUtilities.GEOJSON_TAG_GEOM);
                JSONArray coords = geojson.getJSONArray(FormUtilities.GEOJSON_TAG_COORDINATES);
                coords.put(0, longitude);
                coords.put(1,latitude);
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Incorrect form configuration.", Toast.LENGTH_LONG).show();
            System.out.println("Failure on load JSON form from database.");
            this.finish();
        }

        setContentView(R.layout.details_activity_layout);
    }

    /**
     * @return the data of the feature provided from editing process
     */
    public Bundle getFeatureData() { return this.existingFeatureData; }

    /**
     * @return the form name.
     */
    public String getFormName() {
        return formName;
    }

    /**
     * @return the section object.
     */
    public JSONObject getSectionObject() {
        return sectionObject;
    }

    /**
     * Access the point identified.
     * @return the point identified.
     */
    public long getNoteId() {
        return pointId;
    }

    /**
     * Save action.
     *
     * @param view parent.
     */
    public void saveClicked(View view) {
        try {
            saveAction();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Utilities.messageDialog(this, e.getLocalizedMessage(), null);
        }
    }

    /**
     * Cancel action.
     *
     * @param view parent.
     */
    public void cancelClicked(View view) {
        finish();
    }

    private void saveAction() throws Exception {

        FragmentDetail detailFragment = (FragmentDetail) getSupportFragmentManager().findFragmentById(R.id.detailFragment);
        if (detailFragment != null) {
            detailFragment.storeFormItems(false);
        }

        // extract and check constraints
        List<String> availableFormNames = TagsManager.getFormNames4Section(sectionObject);
        Bundle formData = null;

        for (String formNameIt : availableFormNames) {
            JSONObject formObject = TagsManager.getForm4Name(formNameIt, sectionObject);
            JSONArray formItemsArray = TagsManager.getFormItems(formObject);

            int length = formItemsArray.length();
            formData = new Bundle(length);
            ArrayList<String> keys = new ArrayList<String>(length);
            ArrayList<String> types = new ArrayList<String>(length);

            String key = "";
            String value = "";
            String type = "";
            boolean insertKey;// This control flag is used to ignore some types because are simply ignored or are manipulated in other time.

            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = formItemsArray.getJSONObject(i);

                if (jsonObject.has(TAG_KEY))
                    key = jsonObject.getString(TAG_KEY).trim();

                if (jsonObject.has(TAG_VALUE)) {
                    value = jsonObject.getString(TAG_VALUE).trim();
                }

                if (jsonObject.has(TAG_TYPE)) {
                    type = jsonObject.getString(TAG_TYPE).trim();
                }

                if(!key.equals("") && !value.equals("") && !type.equals("")) {

                    insertKey=true;

                    if (type.equals(TYPE_STRING)) {
                        formData.putString(key,value);
                    } else if (type.equals(TYPE_STRINGAREA)) {
                        formData.putString(key,value);
                    } else if (type.equals(TYPE_DOUBLE)) {
                        formData.putDouble(key, new Double(value));
                    } else if (type.equals(TYPE_INTEGER)) {
                        formData.putInt(key, new Integer(value));
                    } else if (type.equals(TYPE_DATE)) {
                        formData.putString(key, value);
                    } else if (type.equals(TYPE_TIME)) {
                        formData.putString(key, value);
                    } else if (type.equals(TYPE_LABEL)) {
                        insertKey=false;
                        //formData.putString(key, value);
                    } else if (type.equals(TYPE_LABELWITHLINE)) {
                        insertKey=false;
                        //formData.putString(key, value);
                    } else if (type.equals(TYPE_BOOLEAN)) {
                        formData.putBoolean(key, new Boolean(value));
                    } else if (type.equals(TYPE_STRINGCOMBO)) {
                        formData.putString(key,value);
                    } else if (type.equals(TYPE_CONNECTEDSTRINGCOMBO)) {
                        formData.putString(key,value);
                    } else if (type.equals(TYPE_STRINGMULTIPLECHOICE)) {
                        insertKey=false;
                        //formData.putString(key,value);
                    } else if (type.equals(TYPE_PICTURES)) {
                        insertKey=false;
                        // Using the new key to represent a list of image paths. It is manipulated on posterior time.
                        //formData.putString(key,value);
                        decodeFromJson(value, formData);
                    }

                    if(insertKey) {
                        keys.add(key);
                        types.add(type);
                    }

                }
            }
            formData.putStringArrayList(LibraryConstants.FORM_TYPES, types);
            formData.putStringArrayList(LibraryConstants.FORM_KEYS, keys);
        }

        JSONObject geojsonGeometry = sectionObject.getJSONObject(FormUtilities.GEOJSON_TAG_GEOM);
        if(geojsonGeometry.has(FormUtilities.GEOJSON_TAG_TYPE)) {
            String type = geojsonGeometry.getString(FormUtilities.GEOJSON_TAG_TYPE);
            JSONArray jsonCoords = geojsonGeometry.getJSONArray(FormUtilities.GEOJSON_TAG_COORDINATES);

            formData.putString(FormUtilities.GEOJSON_TAG_TYPE, type);
            int coordLen = jsonCoords.length();
            double[] coords = new double[coordLen];

            for (int i = 0; i < coordLen; i++) {
                coords[i]=((Double)jsonCoords.get(i)).doubleValue();
            }
            formData.putDoubleArray(FormUtilities.GEOJSON_TYPE_POINT,coords);
        }

        if(pointId>=0){
            formData.putLong(FormUtilities.GEOM_ID, pointId);
        }

        Intent intent = getIntent();
        intent.putExtra(LibraryConstants.PREFS_KEY_FORM, formData);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void decodeFromJson(String json, Bundle formData){
        JSONObject jsonObject=null;
        try {
            jsonObject = new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayList<String> listInsertedPaths = null;
        ArrayList<String> listDatabaseIds = null;
        try {
            String insertedPaths = null;
            String databaseIds = null;
            insertedPaths = jsonObject.getString(FormUtilities.TAG_ADDED_IMG);
            databaseIds = jsonObject.getString(FormUtilities.TAG_DATABASE_IMG);

            if(insertedPaths!=null && !insertedPaths.isEmpty()) {
                String[] paths = insertedPaths.split(FormUtilities.SEMICOLON);
                int len = paths.length;
                int i = 0;
                listInsertedPaths = new ArrayList<String>(len);
                while (i<len) {
                    listInsertedPaths.add(paths[i]);
                    i++;
                }
            }

            if(databaseIds!=null && !databaseIds.isEmpty()) {
                String[] ids = databaseIds.split(FormUtilities.SEMICOLON);
                int len = ids.length;
                int i = 0;
                listDatabaseIds = new ArrayList<String>(len);
                while (i<len) {
                    listDatabaseIds.add(ids[i]);
                    i++;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(listInsertedPaths!=null && !listInsertedPaths.isEmpty()) formData.putStringArrayList(FormUtilities.INSERTED_IMAGE_PATHS, listInsertedPaths);
        if(listDatabaseIds!=null && !listDatabaseIds.isEmpty()) formData.putStringArrayList(FormUtilities.DATABASE_IMAGE_IDS, listDatabaseIds);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // force to exit through the exit button, in order to avoid losing info
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == GPictureView.PICTURE_VIEW_RESULT) {
            FragmentDetail detailFragment = (FragmentDetail) getSupportFragmentManager().findFragmentById(R.id.detailFragment);
            if (detailFragment != null) {
                detailFragment.onActivityResult(requestCode, Activity.RESULT_OK, data);
            }
        }
    }
}