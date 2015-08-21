package br.org.funcate.terramobile.controller.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.opengis.geometry.BoundingBox;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import br.org.funcate.mobile.sld.SLDParser;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.configuration.ViewContextParameters;
import br.org.funcate.terramobile.controller.activity.settings.SettingsActivity;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.ProjectException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.service.LayersService;
import br.org.funcate.terramobile.model.service.ProjectsService;
import br.org.funcate.terramobile.model.service.SettingsService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

public class MainActivity extends FragmentActivity {
    private ActionBarDrawerToggle mDrawerToggle;

    private ActionBar actionBar;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private TreeView treeView;

    private ViewContextParameters parameters=new ViewContextParameters();

    private Project mProject;

    // Progress bar
    private ProgressDialog progressDialog;

    private ProjectListFragment projectListFragment;

    private MainController mainController;

    private MarkerInfoWindowController markerInfoWindowController;


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.markerInfoWindowController.makeSomeProcessWithResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getActionBar();

        mainController = new MainController(this);

        markerInfoWindowController=new MarkerInfoWindowController(this);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        ResourceHelper.setResources(getResources());

        File directory = Util.getDirectory(this.getResources().getString(R.string.app_workspace_dir));

        try {

            SettingsService.initApplicationSettings(this);

            String currentProjectPath = mainController.getCurrentProject();

            String ext = this.getString(R.string.geopackage_extension);
            if(currentProjectPath != null) {
                ProjectDAO projectDAO = new ProjectDAO(DatabaseFactory.getDatabase(this, ApplicationDatabase.DATABASE_NAME));
                mProject = projectDAO.getByName(currentProjectPath);
                File currentProject = Util.getGeoPackageByName(directory, ext, currentProjectPath);
                if(currentProject != null) {
                    if(mProject == null) {
                        Project project = new Project();
                        project.setId(null);
                        project.setName(currentProjectPath);
                        project.setFilePath(currentProject.getPath());
                        projectDAO.insert(project);

                        mProject = projectDAO.getByName(currentProjectPath);
                    }
                }
                else{
                    if(mProject != null){
                        if(projectDAO.remove(mProject.getId())){
                            Log.i("Remove project","Project removed");
                        }
                        else
                            Log.e("Remove project","Couldn't remove the project");
                    }
                }
            }

        } catch (InvalidAppConfigException e) {

            Message.showErrorMessage(this, R.string.error, e.getMessage());

        } catch (SettingsException e) {

            Message.showErrorMessage(this, R.string.error, e.getMessage());

        } catch (DAOException e) {
            Message.showErrorMessage(this, R.string.error, e.getMessage());
        }


        try {
            treeView = new TreeView(this);
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(this, R.string.error, e.getMessage());
        }

        mTitle = mDrawerTitle = getTitle();
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the action_bar content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        int ActionBarTitleID = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        TextView ActionBarTextView = (TextView) this.findViewById(ActionBarTitleID);
        ActionBarTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                getResources().getDimension(R.dimen.title_text_size));

        actionBar.setDisplayHomeAsUpEnabled(true);
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
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(mDrawerTitle);
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
        return;
    }

    public ViewContextParameters getParameters(){
        return parameters;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        MenuItem menuItem = menu.findItem(R.id.project);
        menuItem.setTitle(mProject != null ? mProject.toString() : "Project");

        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ExpandableListView mDrawerList=treeView.getUIComponent();
        if(mDrawerList==null) return false;

        MenuItem menuItem = menu.findItem(R.id.project);
        menuItem.setTitle(mProject != null?mProject.toString():"Project");

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
        if (mDrawerToggle.onOptionsItemSelected(item))
            return true;
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.project:
                projectListFragment = new ProjectListFragment();
                projectListFragment.show(getFragmentManager(), "packageList");
                return true;
            case R.id.acquire_new_point:
                this.markerInfoWindowController.startActivityForm();
                break;
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.exit:
                this.finish();
                System.exit(0);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
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
        actionBar.setTitle(mTitle);
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

    /**
     * Shows a progress bar with the download progress
     */
    public void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setButton(DialogInterface.BUTTON_NEUTRAL, MainActivity.this.getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.getProjectListFragment().getDownloadTask().cancel(true);
            }
        });
        progressDialog.show();
    }

    public void showLoadingDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public ProgressDialog getProgressDialog() {
        return progressDialog;
    }

    public TreeView getTreeView() {
        return treeView;
    }

    public ProjectListFragment getProjectListFragment() {
        return projectListFragment;
    }

    public Project getProject() {
        return mProject;
    }

    public boolean setProject(Project project) throws InvalidAppConfigException {
        if(project==null)
        {
            clearCurrentProject();
            return true;
        }
        this.mProject = project;

        treeView.refreshTreeView();

        invalidateOptionsMenu();

        try {
            Setting currentProjectSet = new Setting("current_project", project.getName());

            SettingsService.update(this, currentProjectSet, ApplicationDatabase.DATABASE_NAME);

            getMainController().getMenuMapController().removeAllLayers(true);

            SettingsService.initProjectSettings(this, project);

            BoundingBox bb = ProjectsService.getProjectDefaultBoundingBox(this, project.getFilePath());

            if(bb==null)
            {
                //if bb == null include all layers bounding box
                bb = LayersService.getLayersMaxExtent(getTreeView().getLayers());
            }
            mainController.getMenuMapController().panTo(bb);

        } catch (SettingsException e)
        {
            e.printStackTrace();
            Message.showErrorMessage(this, R.string.error, e.getMessage());
            clearCurrentProject();
            return false;
        } catch (ProjectException e)
        {
            e.printStackTrace();
            Message.showErrorMessage(this, R.string.error, e.getMessage());
            clearCurrentProject();
            return false;
        } catch (Exception e)
        {
            e.printStackTrace();
            Message.showErrorMessage(this, R.string.error, R.string.invalid_project);
            clearCurrentProject();
            return false;
        }
        return true;
    }

    private void clearCurrentProject()
    {
        try {
            this.mProject = null;

            Setting currentProjectSet = new Setting("current_project", null);

            SettingsService.update(this, currentProjectSet, ApplicationDatabase.DATABASE_NAME);

            treeView.refreshTreeView();

            invalidateOptionsMenu();

        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(this, R.string.error, e.getMessage());
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(this, R.string.error, e.getMessage());
        }
    }

    public MainController getMainController() {
        return mainController;
    }

    public MarkerInfoWindowController getMarkerInfoWindowController() { return markerInfoWindowController; }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


}