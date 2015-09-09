package br.org.funcate.terramobile.controller.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.augtech.geoapi.geopackage.DateUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.FragmentDetailActivity;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.SettingsService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.Util;

/**
 * Created by bogo on 31/07/15.
 */
public class MainController {

    private MainActivity mainActivity;
    private MenuMapController menuMapController;
    private GPSOverlayController gpsOverlayController;

    public MainController(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        this.menuMapController = new MenuMapController(mainActivity);
        this.gpsOverlayController = new GPSOverlayController(mainActivity);
    }

    public String getServerURL()
    {
        return getSettingValue("terramobile_url");
    }

    public String getUsername()
    {
        return getSettingValue("username");
    }

    public String getPassword()
    {
        return getSettingValue("password");
    }

    public String getCurrentProject()
    {
        return getSettingValue("current_project");
    }

    private String getSettingValue(String key)
    {
        try {

            Setting setting = SettingsService.get(mainActivity, key, ApplicationDatabase.DATABASE_NAME);

            if(setting!=null)
            {
                return setting.getValue();
            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        }
        return null;
    }

    public MenuMapController getMenuMapController() {
        return menuMapController;
    }

    public void setMenuMapController(MenuMapController menuMapController) {
        this.menuMapController = menuMapController;
    }

    public MapFragment getMapFragment() {
        FragmentManager fm = this.mainActivity.getSupportFragmentManager();
        MapFragment fragment = (MapFragment)fm.findFragmentById(R.id.content_frame);
        return fragment;
    }

    public GPSOverlayController getGpsOverlayController() {
        return this.gpsOverlayController;
    }

}
