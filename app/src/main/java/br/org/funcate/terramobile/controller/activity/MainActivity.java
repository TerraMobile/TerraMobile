package br.org.funcate.terramobile.controller.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.io.File;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.settings.SettingsActivity;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.ProjectException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.service.LayersService;
import br.org.funcate.terramobile.model.service.ProjectsService;
import br.org.funcate.terramobile.model.service.SettingsService;
import br.org.funcate.terramobile.util.GlobalParameters;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

public class MainActivity extends FragmentActivity {
    private ActionBarDrawerToggle mDrawerToggle;

    private ActionBar actionBar;

    private CharSequence mTitle;
    // Progress bar
    private ProgressDialog progressDialog;

    private ProjectListFragment projectListFragment;

    private MainController mainController;

    private MarkerInfoWindowController markerInfoWindowController;

    private FeatureInfoPanelController featureInfoPanelController;

    private BroadcastReceiver mMainActivityReceiver;

    /**
     * Temporary variable to test GPKG
     */
    public boolean useNewOverlaySFS = false;


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.markerInfoWindowController.makeSomeProcessWithResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getActionBar();

        markerInfoWindowController=new MarkerInfoWindowController(this);

        featureInfoPanelController = new FeatureInfoPanelController(this);

        mMainActivityReceiver = new MainActivityReceiver();

        IntentFilter filter = new IntentFilter(GlobalParameters.ACTION_BROADCAST_MAIN_ACTIVITY);
        this.registerReceiver(mMainActivityReceiver, filter);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        ResourceHelper.setResources(getResources());

        try
        {
            mainController = new MainController(this);

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(this, R.string.error, e.getMessage());
        }


        mTitle = getTitle();
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the action_bar content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        int ActionBarTitleID = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        TextView ActionBarTextView = (TextView) this.findViewById(ActionBarTitleID);
        ActionBarTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.title_text_size));

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
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            insertMapView();
        }

        mainController.initMain();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.mMainActivityReceiver);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        System.exit(0);
        return;
    }

    @Override
    public void onPause() {
        System.out.println("MainActivity - onPause");
        super.onPause();
        // disableGPSTrackerLayer unregister location events listener too.
        if(getMainController().getGpsOverlayController().isOverlayAdded()) {
            getMainController().getGpsOverlayController().disableGPSTrackerLayer();
        }
    }

    @Override
    public void onResume() {
        System.out.println("MainActivity - onResume");
        super.onResume();
        // enableGPSTrackerLayer register location events listener too.
        if(getMainController().getGpsOverlayController().isOverlayAdded()) {
            getMainController().getGpsOverlayController().enableGPSTrackerLayer();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);

        MenuItem menuItem = menu.findItem(R.id.project);
        menuItem.setTitle(mainController.getCurrentProject() != null ? mainController.getCurrentProject().toString() : "Project");

        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ExpandableListView mDrawerList=mainController.getTreeViewController().getUIComponent();
        if(mDrawerList==null) return false;

        MenuItem menuItem = menu.findItem(R.id.project);
        menuItem.setTitle(mainController.getCurrentProject() != null ? mainController.getCurrentProject().toString() : "Project");

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
            case R.id.tooglesfsbboxquery:
                this.useNewOverlaySFS=!this.useNewOverlaySFS;
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
        MapFragment fragment = new MapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        fragment.setMenuMapController(mainController.getMenuMapController());
        mainController.getMenuMapController().setMapFragment(fragment);
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

    public ProjectListFragment getProjectListFragment() {
        return projectListFragment;
    }

    public MainController getMainController() {
        return mainController;
    }

    public MarkerInfoWindowController getMarkerInfoWindowController() { return markerInfoWindowController; }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public FeatureInfoPanelController getFeatureInfoPanelController() {
        return featureInfoPanelController;
    }


    private class MainActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.hasExtra(GlobalParameters.STATE_GPS_LOCATION)) {
                Boolean showGPSLocation = intent.getBooleanExtra(GlobalParameters.STATE_GPS_LOCATION, false);
                if (showGPSLocation) {
                    getMainController().getGpsOverlayController().addGPSTrackerLayer();
                } else {
                    getMainController().getGpsOverlayController().removeGPSTrackerLayer();
                }
            }
            if(intent.hasExtra(GlobalParameters.STATE_GPS_CENTER)) {
                Boolean showGPSLocationOnCenter = intent.getBooleanExtra(GlobalParameters.STATE_GPS_CENTER, false);
                getMainController().getGpsOverlayController().setKeepOnCenter(showGPSLocationOnCenter);
            }
        }
    }
}