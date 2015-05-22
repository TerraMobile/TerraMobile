package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Iterator;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.gpkg.objects.AppLayer;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.tilesource.AppGeoPackageService;
import br.org.funcate.terramobile.util.ResourceUtil;
import br.org.funcate.terramobile.view.MenuAdapter;

/**
 * Created by Andre Carvalho on 28/04/15.
 */
public class TreeView {

    private ExpandableListView mDrawerList;
    private ArrayList<GpkgLayer> groupItem = new ArrayList<GpkgLayer>();
    private ArrayList<ArrayList<GpkgLayer>> childItem = new ArrayList<ArrayList<GpkgLayer>>();
    private Context context;
    private Resources resources;
    private MenuAdapter mMenuAdapter;

    public TreeView(Context context){
        this.context=context;
        this.resources=context.getResources();
        initTreeView();
    }

    private void initTreeView() {
        setGroupData();
        setChildGroupData();
        try {
            mDrawerList = (ExpandableListView) ((MainActivity) this.context).findViewById(R.id.expandable_tree_view);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if(mDrawerList==null) return;

        mMenuAdapter = new MenuAdapter(this.context, groupItem, childItem);
        mDrawerList.setAdapter(mMenuAdapter);

    }

    public void refreshTreeView(){
        groupItem.clear();
        childItem.clear();
        initTreeView();
        mMenuAdapter.notifyDataSetChanged();
    }

    private void setGroupData() {
        String[] grp= ResourceUtil.getStringArrayResource(this.resources, R.array.menu_groups);
//        int l=grp.length;
        GpkgLayer grpItem;
//        for (int i = 0; i < l; i++) {
        grpItem=new GpkgLayer();
        grpItem.setLayerName(grp[0]);
        grpItem.setLayerType(AppLayer.TILES);
        grpItem.setGeoPackage(null);
        groupItem.add(grpItem);

        grpItem=new GpkgLayer();
        grpItem.setLayerName(grp[1]);
        grpItem.setLayerType(AppLayer.EDITABLE);
        grpItem.setGeoPackage(null);
        groupItem.add(grpItem);

        grpItem=new GpkgLayer();
        grpItem.setLayerName(grp[2]);
        grpItem.setLayerType(AppLayer.FEATURES);
        grpItem.setGeoPackage(null);
        groupItem.add(grpItem);
//        }
    }

    private void setChildGroupData() {

//        ArrayList<TerraMobileMenuItem> childTools = new ArrayList<TerraMobileMenuItem>();
        ArrayList<GpkgLayer> childBaseLayers = new ArrayList<GpkgLayer>();
        ArrayList<GpkgLayer> childCollectLayers = new ArrayList<GpkgLayer>();
        ArrayList<GpkgLayer> childOnlineLayers = new ArrayList<GpkgLayer>();
        /**
         * Add menu items from strings resource file.
         */
//        String[] items=ResourceUtil.getStringArrayResource(this.resources,R.array.menu_items);
//        int len=items.length;
//        TerraMobileMenuToolItem toolItem;
//        for (int i = 0; i < len; i++) {
//            toolItem=new TerraMobileMenuToolItem(items[i], i);
//            childTools.add(toolItem);
//        }
//        childItem.add(childTools);

        // get list layers from GeoPackage
        AppGeoPackageService appGeoPackageService = new AppGeoPackageService(this.context);
        ArrayList<GpkgLayer> layers = null;
        try {
            layers = appGeoPackageService.getLayers();
        } catch (Exception e) {
            System.out.print("Fail on load layers :" + e.getMessage());
            populateDefaultNotFoundLayer();
        }

        if (null == layers) {
            populateDefaultNotFoundLayer();
            return;
        }

        Iterator<GpkgLayer> layersIterator = layers.iterator();
        while (layersIterator.hasNext()) {

            GpkgLayer l = layersIterator.next();

            switch (l.getLayerType()){
                case FEATURES:{
                    childCollectLayers.add(l);
                    break;
                }
                case TILES:{
                    childBaseLayers.add(l);
                    break;
                }
                case EDITABLE:{
                    childOnlineLayers.add(l);
                }
                case ONLINE:{
                    // TODO: this type layer not implemented yet.
                    childOnlineLayers.add(l);
                    break;
                }
                case INVALID:{

                    break;
                }
                default:{// default is GpkgLayer.INVALID ??

                }
            }
        }

        if(childCollectLayers.isEmpty())
            childCollectLayers.add(getNotFoundMenuLayerItem());
        if(childBaseLayers.isEmpty())
            childBaseLayers.add(getNotFoundMenuLayerItem());
        if(childOnlineLayers.isEmpty())
            childOnlineLayers.add(getNotFoundMenuLayerItem());

        childItem.add(childBaseLayers);
        childItem.add(childCollectLayers);
        childItem.add(childOnlineLayers);
    }

    public ExpandableListView getUIComponent(){
        return mDrawerList;
    }

    private GpkgLayer getNotFoundMenuLayerItem() {
        GpkgLayer lnf = new GpkgLayer();
        lnf.setGeoPackage(null);
        lnf.setLayerName(this.context.getResources().getString(R.string.data_not_found));
        lnf.setLayerType(AppLayer.INVALID);
        return lnf;
    }

    private void populateDefaultNotFoundLayer() {

        ArrayList<GpkgLayer> childBaseLayers = new ArrayList<GpkgLayer>();
        ArrayList<GpkgLayer> childCollectLayers = new ArrayList<GpkgLayer>();
        ArrayList<GpkgLayer> childOnlineLayers = new ArrayList<GpkgLayer>();

        childBaseLayers.add(getNotFoundMenuLayerItem());
        childCollectLayers.add(getNotFoundMenuLayerItem());
        childOnlineLayers.add(getNotFoundMenuLayerItem());

        childItem.add(childBaseLayers);
        childItem.add(childCollectLayers);
        childItem.add(childOnlineLayers);
    }
}
