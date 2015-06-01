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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import br.org.funcate.dynamicforms.constraints.Constraints;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.dynamicforms.util.PositionUtilities;
import br.org.funcate.dynamicforms.util.Utilities;

import static br.org.funcate.dynamicforms.FormUtilities.TAG_IS_RENDER_LABEL;
import static br.org.funcate.dynamicforms.FormUtilities.TAG_KEY;
import static br.org.funcate.dynamicforms.FormUtilities.TAG_VALUE;

/**
 * Fragment detail view activity.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class FragmentDetailActivity extends FragmentActivity {
    private String formName;
    private String sectionObjectString;
    private JSONObject sectionObject;
    private String sectionName;
    private double longitude;
    private double latitude;
    private double elevation = -9999.0;
    private long noteId;

    // this members is used in dynamic form process.
    private static final String USE_MAPCENTER_POSITION = "USE_MAPCENTER_POSITION";
    private double[] gpsLocation;
    // --------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String defaultSectionName = "terramobile";
        try {
            sectionObject = TagsManager.getInstance(getApplicationContext()).getSectionByName(defaultSectionName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sectionObjectString = sectionObject.toString();

        // don't permit rotation
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (savedInstanceState != null) {
            formName = savedInstanceState.getString(FormUtilities.ATTR_FORMNAME);
        }
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            noteId = extras.getLong(LibraryConstants.DATABASE_ID);
            formName = extras.getString(FormUtilities.ATTR_FORMNAME);
            sectionObjectString = extras.getString(FormUtilities.ATTR_SECTIONOBJECTSTR);
            try {
                sectionObject = new JSONObject(sectionObjectString);
                sectionName = sectionObject.getString("sectionname");
            } catch (JSONException e) {
                //GPLog.error(this, null, e);
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            longitude = extras.getDouble(LibraryConstants.LONGITUDE);
            latitude = extras.getDouble(LibraryConstants.LATITUDE);
        }

        setContentView(R.layout.details_activity_layout);
    }

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
     * @return the sectionname.
     */
    public String getSectionName() {
        return sectionObjectString;
    }

    /**
     * @return the latitude.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @return the longitude.
     */
    public double getLongitude() {
        return longitude;
    }

    public long getNoteId() {
        return noteId;
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
        // if in landscape mode store last inserted info, since that fragment has not been stored
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            FragmentDetail detailFragment = (FragmentDetail) getSupportFragmentManager().findFragmentById(R.id.detailFragment);
            if (detailFragment != null) {
                detailFragment.storeFormItems(false);
            }
        }

        // extract and check constraints
        List<String> availableFormNames = TagsManager.getFormNames4Section(sectionObject);
        String renderingLabel = null;
        for (String formName : availableFormNames) {
            JSONObject formObject = TagsManager.getForm4Name(formName, sectionObject);

            JSONArray formItemsArray = TagsManager.getFormItems(formObject);

            int length = formItemsArray.length();
            String value = null;
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = formItemsArray.getJSONObject(i);

                String key = "-";
                if (jsonObject.has(TAG_KEY))
                    key = jsonObject.getString(TAG_KEY).trim();

                if (jsonObject.has(TAG_VALUE)) {
                    value = jsonObject.getString(TAG_VALUE).trim();
                }
                if (jsonObject.has(TAG_IS_RENDER_LABEL)) {
                    String isRenderingLabelStr = jsonObject.getString(TAG_IS_RENDER_LABEL).trim();
                    boolean isRenderingLabel = Boolean.parseBoolean(isRenderingLabelStr);
                    if (isRenderingLabel)
                        renderingLabel = value;
                }

                // inject latitude
                if (key.equals(LibraryConstants.LATITUDE)) {
                    String latitudeString = String.valueOf(latitude);
                    value = latitudeString;
                    jsonObject.put(TAG_VALUE, latitudeString);
                }
                // inject longitude
                if (key.equals(LibraryConstants.LONGITUDE)) {
                    String longitudeString = String.valueOf(longitude);
                    value = longitudeString;
                    jsonObject.put(TAG_VALUE, longitudeString);
                }

                Constraints constraints = FormUtilities.handleConstraints(jsonObject, null);
                if (value == null || !constraints.isValid(value)) {
                    String constraintDescription = constraints.getDescription();
                    String validfieldMsg = getString(R.string.form_field_check);
                    String msg = Utilities.format(validfieldMsg, key, formName, constraintDescription);
                    Utilities.messageDialog(this, msg, null);
                    return;
                }
            }
        }

        // finally store data
        long timestamp = System.currentTimeMillis();

        if (renderingLabel == null) {
            renderingLabel = sectionName;
        }
        String[] formDataArray = {//
                String.valueOf(noteId), //
                String.valueOf(longitude), //
                String.valueOf(latitude), //
                String.valueOf(elevation), //
                String.valueOf(timestamp), //
                renderingLabel, //
                "POI", //
                sectionObjectString};
        Intent intent = getIntent();
        intent.putExtra(LibraryConstants.PREFS_KEY_FORM, formDataArray);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // force to exit through the exit button, in order to avoid losing info
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
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
}