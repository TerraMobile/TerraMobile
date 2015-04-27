package br.org.funcate.terramobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.augtech.geoapi.geopackage.GeoPackage;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleInvalidationHandler;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import br.org.funcate.jgpkg.service.GeoPackageService;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.configuration.ViewContextParameters;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.exception.FileException;
import br.org.funcate.terramobile.model.service.FileService;
import br.org.funcate.terramobile.model.task.DownloadTask;
import br.org.funcate.terramobile.model.tilesource.MapTileGeoPackageProvider;

@SuppressWarnings("unchecked")
public class MenuAdapter extends BaseExpandableListAdapter {

	public ArrayList<String> groupItem;
	public ArrayList<Object> ChildItem = new ArrayList<Object>();
	public LayoutInflater mInflater;
	public Activity activity;
	private final Context context;

	public MenuAdapter(Context context, ArrayList<String> grList, ArrayList<Object> childItem) {
		this.context = context;
		groupItem = grList;
		this.ChildItem = childItem;
    }

	public void setInflater(LayoutInflater mInflater, Activity act) {
		this.mInflater = mInflater;
		activity = act;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return null;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0;
	}

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        /*ExpandableListView ExpListView = (ExpandableListView) ((MainActivity) this.context).findViewById(R.id.left_drawer);
        ExpListView.setOnGroupClickListener(this);*/

        if (convertView == null) {
            convertView = new TextView(context);
        }
        convertView.setClickable(false);

        TextView text = null;
        text = (TextView) convertView;
        text.setText(groupItem.get(groupPosition));
        text.setTextColor(Color.WHITE);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.grptextsize));
        convertView.setTag(groupItem.get(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, final ViewGroup parent) {

        /*ExpandableListView ExpListView = (ExpandableListView) ((MainActivity) this.context).findViewById(R.id.left_drawer);
        ExpListView.setOnChildClickListener(this);*/

        TextView text = null;
        if (convertView == null) {
            convertView = new TextView(context);
        }
        text = (TextView) convertView;
        ArrayList<String> children = (ArrayList<String>) ChildItem.get(groupPosition);
        final String child = children.get(childPosition);
        text.setTextColor(Color.WHITE);
        text.setText("  "+child);
        text.setTag(child);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.childtextsize));

        text.setOnClickListener(new TreeViewEventController(groupPosition,childPosition));

        return convertView;
    }

    public class TreeViewEventController implements View.OnClickListener {

        private int grpID;
        private int chlID;
        private File appPath = getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        private String tempURL = context.getResources().getString(R.string.gpkg_url);

        public TreeViewEventController(int groupPosition, int childPosition){
            this.grpID=groupPosition;
            this.chlID=childPosition;
        }

        @Override
        public void onClick(View v) {

            storeSelectedItem(v);
            exec();
            Toast.makeText(context, groupItem.get(this.grpID) + ":" + v.getTag(),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        private void exec() {
            switch (this.grpID){
                case 0:{// Tools
                    switch (this.chlID){
                        case 0: {//Download GeoPackage
                            String fileName = appPath.getPath() + "/GPKG-TerraMobile-test.zip";

                            try {
                                DownloadTask task = new DownloadTask(tempURL, fileName, true);

                                boolean downloaded = task.execute().get();

                                if (!downloaded) {
                                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    break;
                                }

                                FileService.unzip(fileName, appPath.getPath() + "/");

                            } catch (InterruptedException e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (ExecutionException e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            } catch (FileException e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case 1:{//Create GeoPackage
                            GeoPackageService.createGPKG(context, appPath.getPath() + "/test.gpkg");
                            Toast.makeText(context, "GeoPackage file successfully created", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        case 2:{//Read Geometries
                            try {

                                GeoPackage gpkg = GeoPackageService.readGPKG(context,appPath.getPath()+"/test.gpkg");


                                List<SimpleFeature> features = GeoPackageService.getGeometries(gpkg, "municipios_2005");

                                Toast.makeText(context, ""+features.size()+" features on the file", Toast.LENGTH_SHORT).show();

                            } catch (Exception e) {
                                Toast.makeText(context, "Error reading gpkg file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case 3:{//Read Tiles
                            try {

                                String path = appPath.getPath();

                                GeoPackage gpkg = GeoPackageService.readGPKG(context, path+"/landsat2009_tiles.gpkg");
                                createGeoPackageTileSourceOverlay();

                            } catch (Exception e) {
                                Toast.makeText(context, "Error reading gpkg file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                        case 4:{// Insert
                            try {
                                //GeoPackageService.insertDataGPKG(thisActivity,"/GeoPackageTest/test.gpkg");
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                                Toast.makeText(context, "Error insert GML on device: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        }
                    }
                }
                case 1:{// Layers
                    break;
                }
            }
        }

        private void createGeoPackageTileSourceOverlay()
        {

            MapView mapView = (MapView) ((MainActivity) context).findViewById(R.id.mapview);
            mapView.setMaxZoomLevel(20);
            mapView.setBuiltInZoomControls(true);
            mapView.setMultiTouchControls(true);


            System.out.println("Overlay size:" + mapView.getOverlayManager().size());

/*        OnlineTileSourceBase mapQuestTileSource = TileSourceFactory.MAPQUESTOSM;
        String tileSourcePath = mapQuestTileSource.OSMDROID_PATH.getAbsolutePath() + "/";*/

            final MapTileProviderBasic tileProvider = new MapTileProviderBasic(context);

            final ITileSource tileSource = new XYTileSource("Mapnik", ResourceProxy.string.mapnik, 1, 18, 256, ".png", new String[] {"http://tile.openstreetmap.org/"});

            MapTileModuleProviderBase moduleProvider = new MapTileGeoPackageProvider(tileSource);
            SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(context);
            MapTileProviderArray tileProviderArray = new MapTileProviderArray(tileSource, simpleReceiver, new MapTileModuleProviderBase[] { moduleProvider });

/*        tileProvider.setTileSource(tileSource);*/
            final TilesOverlay tilesOverlay = new TilesOverlay(tileProviderArray, context);
            tilesOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            mapView.getOverlays().add(tilesOverlay);
            //mapView.getOverlayManager().overlaysReversed();
            //mapView.getTileProvider().clearTileCache();
            tileProvider.setTileRequestCompleteHandler(new SimpleInvalidationHandler(mapView));
            mapView.setTileSource(tileSource);
            mapView.setUseDataConnection(false); //  letting osmdroid know you would use it in offline mode, keeps the mapView from loading online tiles using network connection.*/
            mapView.invalidate();
        }

        private void storeSelectedItem(View v){
            ViewContextParameters par = ((MainActivity) context).getParameters();
            TextView text = null;
            text = (TextView) v;

            try {
                if (v.isSelected()) {
                    par.removeLayerName((String) text.getTag());
                    text.setSelected(false);
                    text.setBackgroundColor(Color.BLACK);
                    text.setTextColor(Color.WHITE);
                } else {
                    par.addLayerName((String) text.getTag());
                    text.setSelected(true);
                    text.setBackgroundColor(Color.WHITE);
                    text.setTextColor(Color.BLACK);
                }
            }catch (Exception e){
                Toast.makeText(context, e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Get a directory on extenal storage (SD card etc), ensuring it exists
     *
     * @return a new File representing the chosen directory
     */
    public static File getDirectory(String directory) {
        if (directory==null) return null;
        String path = Environment.getExternalStorageDirectory().toString();
        path += directory.startsWith("/") ? "" : "/";
        path += directory.endsWith("/") ? directory : directory + "/";
        File file = new File(path);
        file.mkdirs();
        return file;
    }

    @Override
	public int getChildrenCount(int groupPosition) {
		return ((ArrayList<String>) ChildItem.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return null;
	}

	@Override
	public int getGroupCount() {
		return groupItem.size();
	}

	@Override
	public void onGroupCollapsed(int groupPosition) {

        String s=this.context.getString(R.string.app_name);
        ((MainActivity) this.context).setTitle(s.subSequence(0,s.length()));
        super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {

        String s=groupItem.get(groupPosition);
        ((MainActivity) this.context).setTitle(s.subSequence(0,s.length()));
		super.onGroupExpanded(groupPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
}
