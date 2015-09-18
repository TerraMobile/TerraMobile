package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.content.res.Resources;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.util.DevUtil;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.view.TreeViewAdapter;

/**
 * Created by Andre Carvalho on 28/04/15.
 */
public class TreeView {

    private ExpandableListView mDrawerList;
    private ArrayList<GpkgLayer> groupItem = new ArrayList<GpkgLayer>();
    private ArrayList<ArrayList<GpkgLayer>> childItem = new ArrayList<ArrayList<GpkgLayer>>();
    private Context context;
    private TreeViewAdapter treeViewAdapter;
    private GpkgLayer selectedEditableLayer;

    public TreeView(Context context) throws InvalidAppConfigException {
        this.context=context;
        initTreeView();
    }

    private void initTreeView() throws InvalidAppConfigException {
        setGroupData();
        setChildGroupData();
        try {
            mDrawerList = (ExpandableListView) ((MainActivity) this.context).findViewById(R.id.expandable_tree_view);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if(mDrawerList==null) return;

        treeViewAdapter = new TreeViewAdapter(this.context, groupItem, childItem);
        mDrawerList.setAdapter(treeViewAdapter);
        selectedEditableLayer=null;
    }

    public void refreshTreeView() throws InvalidAppConfigException {
        groupItem.clear();
        childItem.clear();
        initTreeView();
        treeViewAdapter.notifyDataSetChanged();
    }

    private void setGroupData() throws InvalidAppConfigException {
        String[] grp= ResourceHelper.getStringArrayResource(R.array.menu_groups);
        GpkgLayer grpItem;
        grpItem=new GpkgLayer();
        grpItem.setName(grp[0]);
        grpItem.setType(GpkgLayer.Type.FEATURES);
        grpItem.setGeoPackage(null);
        groupItem.add(grpItem);

        grpItem=new GpkgLayer();
        grpItem.setName(grp[1]);
        grpItem.setType(GpkgLayer.Type.EDITABLE);
        grpItem.setGeoPackage(null);
        groupItem.add(grpItem);
    }

    private void setChildGroupData() {
        ArrayList<GpkgLayer> childLayers = new ArrayList<GpkgLayer>();
        ArrayList<GpkgLayer> childEditableLayers = new ArrayList<GpkgLayer>();
        ArrayList<GpkgLayer> childOnlineLayers = new ArrayList<GpkgLayer>();

        // get list layers from GeoPackage
        ArrayList<GpkgLayer> layers = null;
        try {
            layers = AppGeoPackageService.getLayers(this.context);
        } catch (InvalidGeopackageException e) {
            Message.showErrorMessage((MainActivity) this.context, R.string.failure_title_msg, e.getMessage());
        } catch (QueryException e) {
            Message.showErrorMessage((MainActivity)this.context, R.string.failure_title_msg, e.getMessage());
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
                    childLayers.add(l);
                    break;
                }
                case TILES:{
                    childLayers.add(l);
                    break;
                }
                case EDITABLE:{
                    childEditableLayers.add(l);
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

        if(childLayers.isEmpty())
            childLayers.add(getNotFoundMenuLayerItem());
        if(childEditableLayers.isEmpty())
            childEditableLayers.add(getNotFoundMenuLayerItem());

        childItem.add(childLayers);
        childItem.add(childEditableLayers);
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

        ArrayList<GpkgLayer> childLayers = new ArrayList<GpkgLayer>();
        ArrayList<GpkgLayer> childEditableLayers = new ArrayList<GpkgLayer>();
        ArrayList<GpkgLayer> childOnlineLayers = new ArrayList<GpkgLayer>();

        childLayers.add(getNotFoundMenuLayerItem());
        childEditableLayers.add(getNotFoundMenuLayerItem());
        childOnlineLayers.add(getNotFoundMenuLayerItem());

        childItem.add(childLayers);
        childItem.add(childEditableLayers);
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

    /**
     * Return all layers, from all groups (Base, Gathering and Overlays
     * @return
     */
    public ArrayList<GpkgLayer> getLayers() {
        ArrayList<GpkgLayer> layers = new ArrayList<GpkgLayer>();
        for (int i = 0; i < childItem.size(); i++) {
            layers.addAll(childItem.get(i));
        }

        return layers;
    }

}
