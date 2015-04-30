package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.widget.ExpandableListView;
import android.widget.Toast;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Iterator;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.tilesource.AppGeoPackageService;
import br.org.funcate.terramobile.util.ResourceUtil;
import br.org.funcate.terramobile.view.TerraMobileMenuGroupItem;
import br.org.funcate.terramobile.view.TerraMobileMenuItem;
import br.org.funcate.terramobile.view.TerraMobileMenuLayerItem;
import br.org.funcate.terramobile.view.MenuAdapter;
import br.org.funcate.terramobile.view.TerraMobileMenuToolItem;

/**
 * Created by Andre Carvalho on 28/04/15.
 */
public class TreeView {

    private ExpandableListView mDrawerList;
    private ArrayList<TerraMobileMenuGroupItem> groupItem = new ArrayList<TerraMobileMenuGroupItem>();
    private ArrayList<Object> childItem = new ArrayList<Object>();
    private Context context;
    private Resources resources;

    public TreeView(Context context){
        this.context=context;
        this.resources=context.getResources();
        setGroupData();
        setChildGroupData();
        initTreeView();
    }

    private void initTreeView() {

        try {
            mDrawerList = (ExpandableListView) ((MainActivity) this.context).findViewById(R.id.expandable_tree_view);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if(mDrawerList==null) return;

        MenuAdapter mMenuAdapter = new MenuAdapter(this.context, groupItem, childItem);
        mDrawerList.setAdapter(mMenuAdapter);

    }

    private void setGroupData() {
        String[] grp= ResourceUtil.getStringArrayResource(this.resources, R.array.menu_groups);
        int l=grp.length;
        TerraMobileMenuGroupItem grpItem;
        for (int i = 0; i < l; i++) {
            grpItem=new TerraMobileMenuGroupItem(grp[i]);
            groupItem.add(grpItem);
        }
    }

    private void setChildGroupData() {

        ArrayList<TerraMobileMenuItem> childTools = new ArrayList<TerraMobileMenuItem>();
        ArrayList<TerraMobileMenuItem> childBaseLayers = new ArrayList<TerraMobileMenuItem>();
        ArrayList<TerraMobileMenuItem> childCollectLayers = new ArrayList<TerraMobileMenuItem>();
        ArrayList<TerraMobileMenuItem> childOnlineLayers = new ArrayList<TerraMobileMenuItem>();
        /**
         * Add menu items from strings resource file.
         */
        String[] items=ResourceUtil.getStringArrayResource(this.resources,R.array.menu_items);
        int len=items.length;
        TerraMobileMenuToolItem toolItem;
        for (int i = 0; i < len; i++) {
            toolItem=new TerraMobileMenuToolItem(items[i]);
            childTools.add(toolItem);
        }
        childItem.add(childTools);

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
            TerraMobileMenuLayerItem m = new TerraMobileMenuLayerItem(l);

            switch (l.getLayerType()){
                case GpkgLayer.FEATURES:{
                    childCollectLayers.add(m);
                    break;
                }
                case GpkgLayer.TILES:{
                    childBaseLayers.add(m);
                    break;
                }
                case GpkgLayer.ONLINE:{
                    // TODO: this type layer not implemented yet.
                    childOnlineLayers.add(m);
                    break;
                }
                case GpkgLayer.INVALID:{

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

    private TerraMobileMenuLayerItem getNotFoundMenuLayerItem() {
        GpkgLayer lnf = new GpkgLayer();
        lnf.setGeoPackage(null);
        lnf.setLayerName(this.context.getResources().getString(R.string.data_not_found));
        lnf.setLayerType(GpkgLayer.INVALID);
        TerraMobileMenuLayerItem mnf = new TerraMobileMenuLayerItem(lnf);
        return mnf;
    }

    private void populateDefaultNotFoundLayer() {

        ArrayList<TerraMobileMenuItem> childBaseLayers = new ArrayList<TerraMobileMenuItem>();
        ArrayList<TerraMobileMenuItem> childCollectLayers = new ArrayList<TerraMobileMenuItem>();
        ArrayList<TerraMobileMenuItem> childOnlineLayers = new ArrayList<TerraMobileMenuItem>();

        childBaseLayers.add(getNotFoundMenuLayerItem());
        childCollectLayers.add(getNotFoundMenuLayerItem());
        childOnlineLayers.add(getNotFoundMenuLayerItem());

        childItem.add(childBaseLayers);
        childItem.add(childCollectLayers);
        childItem.add(childOnlineLayers);
    }
}
