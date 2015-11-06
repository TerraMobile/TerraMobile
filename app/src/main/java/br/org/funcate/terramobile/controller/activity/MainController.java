package br.org.funcate.terramobile.controller.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;

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
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.ProjectException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.LayersService;
import br.org.funcate.terramobile.model.service.ProjectsService;
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
    private TreeViewController treeViewController;
    private MarkerInfoWindowController markerInfoWindowController;

    private FeatureInfoPanelController featureInfoPanelController;

    private Project currentProject;

    public MainController(MainActivity mainActivity) throws InvalidAppConfigException {
        this.mainActivity = mainActivity;
        this.menuMapController = new MenuMapController(mainActivity, this);
        this.gpsOverlayController = new GPSOverlayController(mainActivity);
        this.markerInfoWindowController = new MarkerInfoWindowController(mainActivity);
        this.featureInfoPanelController = new FeatureInfoPanelController(mainActivity);
        treeViewController = new TreeViewController(this.mainActivity, this);
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

    private String getCurrentProjectPath()
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

    public TreeViewController getTreeViewController() {
        return treeViewController;
    }

    public void setTreeViewController(TreeViewController treeViewController) {
        this.treeViewController = treeViewController;
    }

    public boolean setCurrentProject(Project project) throws InvalidAppConfigException {

        DatabaseFactory.getDatabase(mainActivity, project.getFilePath());

        if(project==null)
        {
            clearCurrentProject();
            return true;
        }

        // remove GPS Overlay of the map
        boolean hasGPSEnabledOnMap = getGpsOverlayController().isOverlayAdded();
        if(hasGPSEnabledOnMap) getGpsOverlayController().removeGPSTrackerLayer();

        // remove all infoWindow
        markerInfoWindowController.closeAllInfoWindows();

        this.currentProject = project;

        getTreeViewController().refreshTreeView();

        mainActivity.invalidateOptionsMenu();

        try {
            Setting currentProjectSet = new Setting("current_project", project.getName());

            SettingsService.update(mainActivity, currentProjectSet, ApplicationDatabase.DATABASE_NAME);

            getMenuMapController().removeAllLayers(true);

            SettingsService.initProjectSettings(mainActivity, project);

            BoundingBox bb = ProjectsService.getProjectDefaultBoundingBox(mainActivity, project.getFilePath());

            if(bb==null)
            {
                //if bb == null include all layers bounding box
                bb = LayersService.getLayersMaxExtent(getTreeViewController().getAllLayers());
            }

            if(bb!=null)
            {
                getMenuMapController().panTo(bb);
            }


            if(hasGPSEnabledOnMap) getGpsOverlayController().addGPSTrackerLayer();

            getTreeViewController().enableInitialLayers();

        } catch (SettingsException e)
        {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
            clearCurrentProject();
            return false;
        } catch (ProjectException e)
        {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
            clearCurrentProject();
            return false;
        } catch (Exception e)
        {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, R.string.invalid_project);
            clearCurrentProject();
            return false;
        }
        return true;
    }

    private void clearCurrentProject()
    {
        try {
            this.currentProject = null;

            Setting currentProjectSet = new Setting("current_project", null);

            SettingsService.update(mainActivity, currentProjectSet, ApplicationDatabase.DATABASE_NAME);

            getTreeViewController().refreshTreeView();

            mainActivity.invalidateOptionsMenu();

        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        }
    }
    public void initMain()
    {
        try {

            SettingsService.initApplicationSettings(mainActivity);

        } catch (InvalidAppConfigException e) {

            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());

        } catch (SettingsException e) {

            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        }

        try{
            getTreeViewController().initTreeView();
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        }
    }


    public void loadCurrentProject() throws InvalidAppConfigException, DAOException {

        File directory = Util.getDirectory(mainActivity.getResources().getString(R.string.app_workspace_dir));

        String currentProjectPath = getCurrentProjectPath();

        String ext = mainActivity.getString(R.string.geopackage_extension);
        if(currentProjectPath != null) {
            ProjectDAO projectDAO = new ProjectDAO(DatabaseFactory.getDatabase(mainActivity, ApplicationDatabase.DATABASE_NAME));
            File currentProjectFile = Util.getGeoPackageByName(directory, ext, currentProjectPath);
            Project currentProject = projectDAO.getByName(currentProjectPath);
            if(currentProjectFile != null) {
                if(currentProject == null) {
                    Project project = new Project();
                    project.setId(null);
                    project.setName(currentProjectPath);
                    project.setFilePath(currentProjectFile.getPath());
                    projectDAO.insert(project);

                    currentProject = projectDAO.getByName(currentProjectPath);
                }
                else
                {
                    setCurrentProject(currentProject);
                }
            }
            else{
                if(currentProject != null){
                    if(projectDAO.remove(currentProject.getId())){
                        Log.i("Remove project", "Project removed");
                    }
                    else
                        Log.e("Remove project","Couldn't remove the project");
                }
            }
        }
    }

    public Project getCurrentProject()
    {
        return currentProject;
    }

    public MarkerInfoWindowController getMarkerInfoWindowController() {
        return markerInfoWindowController;
    }

    public void setMarkerInfoWindowController(MarkerInfoWindowController markerInfoWindowController) {
        this.markerInfoWindowController = markerInfoWindowController;
    }


    public FeatureInfoPanelController getFeatureInfoPanelController() {
        return featureInfoPanelController;
    }

    public void setFeatureInfoPanelController(FeatureInfoPanelController featureInfoPanelController) {
        this.featureInfoPanelController = featureInfoPanelController;
    }

    /**
     * When MapView is initialized this method is called. Use this method to trigger any initialization feature
     */
    public void onMapViewInitialized()
    {
        getMapFragment().configureMapView();

        try {

            menuMapController.getMainController().loadCurrentProject();

        } catch (InvalidAppConfigException e) {

            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());

        } catch (DAOException e) {

            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        }

    }



}
