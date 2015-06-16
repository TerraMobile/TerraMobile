package br.org.funcate.terramobile.controller.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
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
import android.widget.Toast;

import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.overlays.FolderOverlay;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import java.io.File;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.configuration.ViewContextParameters;
import br.org.funcate.terramobile.controller.activity.settings.SettingsActivity;
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.geomsource.SFSLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.tilesource.AppGeoPackageService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceUtil;

public class MainActivity extends FragmentActivity {
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private ActionBar actionBar;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private TreeView treeView;

    private ViewContextParameters parameters=new ViewContextParameters();

    private Project mProject;
    private Settings settings;

    private ProjectDAO projectDAO;
    private SettingsDAO settingsDAO;

    private static int FORM_COLLECT_DATA = 222;

    // Progress bar
    private ProgressDialog progressDialog;

    private ProjectListFragment projectListFragment;


/*    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == FORM_COLLECT_DATA) {
            Bundle extras = data.getBundleExtra(LibraryConstants.PREFS_KEY_FORM);
            try {
                AppGeoPackageService.storeData(this,extras);
            }catch (TerraMobileException tme) {
                //Message.showMessage(this, R.drawable.error, getResources().getString(R.string.error), tme.getMessage());
                Message.showErrorMessage(this, R.string.error, R.string.missing_form_data);
            }catch (QueryException qe) {
                //Message.showMessage(this, R.drawable.error, getResources().getString(R.string.error), qe.getMessage());
                Message.showErrorMessage(this, R.string.error, R.string.error_while_storing_form_data);
            }
        }else {
            Message.showErrorMessage(this, R.string.error, R.string.cancel_form_data);
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        actionBar = getActionBar();

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        settingsDAO = new SettingsDAO(this);
        if(settingsDAO.getById(1) == null){
            settings = new Settings();
            settings.setId(1);
            settings.setUrl("http://192.168.3.103:8080/TerraMobileServer/tmserver/projectservices"); // URL for test
            settingsDAO.insert(settings);
        }
        else{
            settings = settingsDAO.getById(1);
        }

        File directory = ResourceUtil.getDirectory(this.getResources().getString(R.string.app_workspace_dir));

        String fileName = settings.getCurrentProject()+getResources().getString(R.string.geopackage_extension);
        if(settings.getCurrentProject() != null) {
            projectDAO = new ProjectDAO(this);
            mProject = projectDAO.getByName(settings.getCurrentProject());
            if(ResourceUtil.getGeoPackageByName(directory, getResources().getString(R.string.geopackage_extension), fileName) != null) {
                if(mProject == null) {
                    Project project = new Project();
                    project.setId(null);
                    project.setName(fileName);
                    project.setFilePath(directory.getPath());
                    projectDAO.insert(project);

                    mProject = projectDAO.getByName(settings.getCurrentProject());
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

        treeView = new TreeView(this);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        menuItem.setTitle(mProject != null ? mProject.getName() : "Project");

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
                ConnectivityManager cm = (ConnectivityManager)this.getSystemService(CONNECTIVITY_SERVICE);
                int wifi = ConnectivityManager.TYPE_WIFI;
                int mobile = ConnectivityManager.TYPE_MOBILE;
                if (cm.getNetworkInfo(mobile).isConnected() || cm.getNetworkInfo(wifi).isConnected()) {
                    projectListFragment = new ProjectListFragment();
                    projectListFragment.show(getFragmentManager(), "packageList");
                }
                else{
                    Message.showErrorMessage(this, R.string.error, R.string.no_connection);
                }
                return true;
            case R.id.acquire_new_point:
                FragmentManager fm = getSupportFragmentManager();
                MapFragment fragment = (MapFragment)fm.findFragmentById(R.id.content_frame);
                // add an bookmark on map and show the related form
                fragment.startForm();
                break;
            case R.id.test_vector_data:
                testVectorData();
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

    public MapFragment getMapFragment()
    {
        FragmentManager fm = getSupportFragmentManager();
        MapFragment fragment = (MapFragment)fm.findFragmentById(R.id.content_frame);
        return fragment;
    }


/*    private void startForm(GeoPoint point) {
        // This id is provided from the selected point, if one it is selected otherwise -1 is default.
        long selectedPointID = -1;

        try {
            Intent formIntent = new Intent(MainActivity.this, FragmentDetailActivity.class);
            formIntent.putExtra(LibraryConstants.SELECTED_POINT_ID, selectedPointID);
            // The form name attribute, provided by JSON, shall be the same name of the editable layer.
            formIntent.putExtra(FormUtilities.ATTR_FORMNAME, treeView.getSelectedEditableLayer().getName());
            formIntent.putExtra(FormUtilities.ATTR_JSON_TAGS, treeView.getSelectedEditableLayer().getJSON());
            formIntent.putExtra(FormUtilities.TYPE_LATITUDE, point.getLatitude());
            formIntent.putExtra(FormUtilities.TYPE_LONGITUDE, point.getLongitude());
            startActivityForResult(formIntent, FORM_COLLECT_DATA);

        } catch (Exception e) {
            Message.showErrorMessage(MainActivity.this, R.string.failure_title_msg, R.string.error_start_form);

        }
    }*/

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

    public void setProject(Project project) {
        this.mProject = project;
        settings.setCurrentProject(project!=null?project.toString():null);
        settingsDAO.update(settings);
        treeView.refreshTreeView();
        invalidateOptionsMenu();
    }

    private void testVectorData()
    {

        GpkgLayer layer = null;
        SFSLayer l = null;
        try {
            layer = treeView.getLayerByName("inpe_area_de_estudo_canasat_2000");
            l = AppGeoPackageService.getFeatures(layer);


        } catch (TerraMobileException e) {
            e.printStackTrace();
        }

        MapView map = (MapView) findViewById(R.id.mapview);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);

        Style defaultStyle = new Style(null, 0x901010AA, 1.0f, 0x20AA1010);

        KmlDocument kmlDocument = new KmlDocument();
        Overlay overlay = l.buildOverlay(map, defaultStyle, null, kmlDocument);

        map.getOverlays().add(overlay);

        map.invalidate();

       // map.zoomToBoundingBox(l.getBoundingBox());

/*        String url = "http://mapsengine.google.com/map/kml?mid=z6IJfj90QEd4.kUUY9FoHFRdE";

        KmlDocument kmlDocument = new KmlDocument();

        kmlDocument.parseKMLUrl(url);

        FolderOverlay kmlOverlay = (FolderOverlay)kmlDocument.mKmlRoot.buildOverlay(map, null, null, kmlDocument);

        map.getOverlays().add(kmlOverlay);

        map.invalidate();

        map.zoomToBoundingBox(kmlDocument.mKmlRoot.getBoundingBox());*/

    }
}