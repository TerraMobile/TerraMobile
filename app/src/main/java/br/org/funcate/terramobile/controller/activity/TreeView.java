package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.widget.ExpandableListView;
import android.widget.Toast;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.Iterator;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayers;
import br.org.funcate.terramobile.model.tilesource.AppGeoPackageService;
import br.org.funcate.terramobile.util.ResourceUtil;
import br.org.funcate.terramobile.view.MenuAdapter;

/**
 * Created by Andre Carvalho on 28/04/15.
 */
public class TreeView {

    private ExpandableListView mDrawerList;
    private ArrayList<String> groupItem = new ArrayList<String>();
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
        for (int i = 0; i < l; i++) {
            groupItem.add(grp[i]);
        }
    }

    private void setChildGroupData() {

        ArrayList<String> child = new ArrayList<String>();
        ArrayList<String> childBaseLayers = new ArrayList<String>();
        ArrayList<String> childCollectLayers = new ArrayList<String>();
        ArrayList<String> childOnlineLayers = new ArrayList<String>();
        /**
         * Add menu items from strings resource file.
         */
        String[] items=ResourceUtil.getStringArrayResource(this.resources,R.array.menu_items);
        int len=items.length;
        for (int i = 0; i < len; i++) {
            child.add(items[i]);
        }
        childItem.add(child);

        // get list layers from GeoPackage
        AppGeoPackageService metadata=new AppGeoPackageService(this.context);
        ArrayList<GpkgLayers> layers = null;
        try {
            layers = metadata.getLayers();
        } catch (Exception e) {
            System.out.print("Fail on load layers :" + e.getMessage());
            populateDefaultNotFoundLayer();
        }

        if (null == layers) {
            populateDefaultNotFoundLayer();
            return;
        }

        Iterator<GpkgLayers> layersIterator = layers.iterator();
        while (layersIterator.hasNext()) {

            GpkgLayers l = layersIterator.next();
            if("features".equals(l.getLayerType()))
                childCollectLayers.add(l.getLayerName());
            else if("tiles".equals(l.getLayerType()))
                childBaseLayers.add(l.getLayerName());
        }

        if(childCollectLayers.isEmpty())
            childCollectLayers.add(this.context.getResources().getString(R.string.data_not_found));
        if(childBaseLayers.isEmpty())
            childBaseLayers.add(this.context.getResources().getString(R.string.data_not_found));
        if(childOnlineLayers.isEmpty())
            childOnlineLayers.add(this.context.getResources().getString(R.string.data_not_found));

        childItem.add(childBaseLayers);
        childItem.add(childCollectLayers);
        childItem.add(childOnlineLayers);
    }

    public ExpandableListView getUIComponent(){
        return mDrawerList;
    }

    private void populateDefaultNotFoundLayer() {

        ArrayList<String> childBaseLayers = new ArrayList<String>();
        ArrayList<String> childCollectLayers = new ArrayList<String>();
        ArrayList<String> childOnlineLayers = new ArrayList<String>();

        childBaseLayers.add(this.context.getResources().getString(R.string.data_not_found));
        childCollectLayers.add(this.context.getResources().getString(R.string.data_not_found));
        childOnlineLayers.add(this.context.getResources().getString(R.string.data_not_found));

        childItem.add(childBaseLayers);
        childItem.add(childCollectLayers);
        childItem.add(childOnlineLayers);
    }
}
