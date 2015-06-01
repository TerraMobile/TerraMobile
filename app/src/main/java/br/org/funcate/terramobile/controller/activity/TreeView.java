package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.tilesource.AppGeoPackageService;
import br.org.funcate.terramobile.util.DevUtil;
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
    private GpkgLayer selectedEditableLayer;

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
        selectedEditableLayer=null;
    }

    public void refreshTreeView(){
        groupItem.clear();
        childItem.clear();
        initTreeView();
        mMenuAdapter.notifyDataSetChanged();
    }

    private void setGroupData() {
        String[] grp= ResourceUtil.getStringArrayResource(this.resources, R.array.menu_groups);
        GpkgLayer grpItem;
        grpItem=new GpkgLayer();
        grpItem.setName(grp[0]);
        grpItem.setType(GpkgLayer.Type.TILES);
        grpItem.setGeoPackage(null);
        groupItem.add(grpItem);

        grpItem=new GpkgLayer();
        grpItem.setName(grp[1]);
        grpItem.setType(GpkgLayer.Type.EDITABLE);
        grpItem.setGeoPackage(null);
        groupItem.add(grpItem);

        grpItem=new GpkgLayer();
        grpItem.setName(grp[2]);
        grpItem.setType(GpkgLayer.Type.FEATURES);
        grpItem.setGeoPackage(null);
        groupItem.add(grpItem);
    }

    private void setChildGroupData() {
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
        ArrayList<GpkgLayer> layers = null;
        try {
            layers = AppGeoPackageService.getLayers(this.context);
        } catch (InvalidGeopackageException e) {
            System.out.print("Fail on load layers :" + e.getMessage());
        } catch (QueryException e) {
            System.out.print("Fail on load layers :" + e.getMessage());
        }

        if (null == layers || layers.size()==0) {
            populateDefaultNotFoundLayer();
            return;
        }

        Iterator<GpkgLayer> layersIterator = layers.iterator();
        while (layersIterator.hasNext()) {

            GpkgLayer l = layersIterator.next();

            GpkgLayer.Type type=l.getType();

            if(null==type) continue;

            switch (l.getType()){
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
                    break;
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
        lnf.setName(this.context.getResources().getString(R.string.data_not_found));
        lnf.setType(GpkgLayer.Type.INVALID);
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
    public GpkgLayer getLayerByName(String layerName) throws TerraMobileException {
        if(DevUtil.isNull(layerName))
        {
            throw new TerraMobileException("Invalid layer name.");
        }

        for(int i = 0; i < childItem.size(); i++) {
            ArrayList<GpkgLayer> layers = childItem.get(i);
            for (int j = 0; j < layers.size(); j++) {
                if (layerName.equalsIgnoreCase(layers.get(j).getName())) {
                    return layers.get(j);
                }
            }
        }
        throw new TerraMobileException("Requested layer not found");
    }

    public GpkgLayer getSelectedEditableLayer() {
        return selectedEditableLayer;
    }

    public void setSelectedEditableLayer(GpkgLayer selectedEditableLayer) {
        this.selectedEditableLayer = selectedEditableLayer;
    }
}
