package br.org.funcate.terramobile.model.service;

import android.content.Context;

import com.augtech.geoapi.geometry.BoundingBoxImpl;

import org.opengis.geometry.BoundingBox;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.LayerSettingsDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by bogo on 31/07/15.
 */
public class LayersService {

    private LayersService()
    {

    }

    public static BoundingBox getLayersMaxExtent(ArrayList<GpkgLayer> layers)
    {
        BoundingBox box=null;
        for (int i = 0; i < layers.size(); i++) {
            if(!checkBoundingBox(layers.get(i).getBox()))
            {
                continue;
            }

            if(box==null)
            {
                box = new BoundingBoxImpl(layers.get(i).getBox().getMinX(), layers.get(i).getBox().getMaxX(), layers.get(i).getBox().getMinY(), layers.get(i).getBox().getMaxY());
            }
            else
            {
                box.include(layers.get(i).getBox());
            }
        }
        return box;
    }

    public static void sortLayersByPosition(ArrayList<GpkgLayer> layers) {
        Collections.sort(layers, new Comparator<GpkgLayer>() {
            @Override
            public int compare(GpkgLayer gpkgLayer, GpkgLayer gpkgLayer2) {
                if (gpkgLayer.getPosition() > gpkgLayer2.getPosition()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    public static void sortOverlayByGPKGLayer(List<Overlay> overlays, ArrayList<GpkgLayer> gpkgLayers)
    {
        overlays.clear();
        for (int i = gpkgLayers.size()-1; i >= 0; i--) {
            if(gpkgLayers.get(i).getOsmOverLayer()!=null)
            {
                overlays.add(gpkgLayers.get(i).getOsmOverLayer());
            }
        }
    }

    public static ArrayList<GpkgLayer> composeLinearLayerList(ArrayList<ArrayList<GpkgLayer>> gpkgLayers)
    {
        ArrayList<GpkgLayer> layers = new ArrayList<GpkgLayer>();
        for (int i = 0; i < gpkgLayers.size(); i++) {
            layers.addAll(gpkgLayers.get(i));
        }
        //sortLayersByIndex(layers);
        sortLayersByPosition(layers);
        return layers;
    }

    private static void loadLayersSettings(Context context, Project project, ArrayList<GpkgLayer> layers) throws SettingsException, InvalidAppConfigException {
        try {
            LayerSettingsDAO layerSettingsDAO = new LayerSettingsDAO(DatabaseFactory.getDatabase(context, project.getFilePath()));

            for(int i = 0; i < layers.size(); i++) {

                layerSettingsDAO.load(layers.get(i));

            }

            enableOnlyOneGatheringLayer(context, project, layers);

        }  catch (DAOException e) {
            throw new SettingsException(e.getMessage(), e);
        }
    }

    public static ArrayList<GpkgLayer> getLayers(Context context) throws SettingsException, InvalidAppConfigException, QueryException, InvalidGeopackageException {
        Project prj=((MainActivity) context).getMainController().getCurrentProject();
        ArrayList<GpkgLayer> layers = new ArrayList<GpkgLayer>();
        if(prj!=null)
        {
            layers = AppGeoPackageService.getLayers(prj, context);
            loadLayersSettings(context, prj, layers);
            sortLayersByPosition(layers);
        }

        return layers;
    }

    private static boolean checkBoundingBox(BoundingBox bb)
    {
        if(bb==null) return false;
        if((bb.getMinX()==0.)||
                (bb.getMaxX()==0.)||
                (bb.getMinY()==0.)||
                (bb.getMaxY()==0.))
        {
            return false;
        }
        return true;
    }

    public static void updateLayerSettings(Context context, Project project, ArrayList<GpkgLayer> layers) throws InvalidAppConfigException, SettingsException {
        try {
            LayerSettingsDAO layerSettingsDAO = new LayerSettingsDAO(DatabaseFactory.getDatabase(context, project.getFilePath()));

            if(layerSettingsDAO.deleteAll())
            {
                layerSettingsDAO.insertAll(layers);
            }
        }  catch (DAOException e) {
            throw new SettingsException(e.getMessage(), e);
        }
    }

    public static ArrayList<GpkgLayer>  getEditableLayers(ArrayList<GpkgLayer> allLayers)
    {
        ArrayList<GpkgLayer> editableLayers = new ArrayList<GpkgLayer>();

        for (int i = 0; i < allLayers.size(); i++) {
            if (allLayers.get(i).getType()== GpkgLayer.Type.EDITABLE)
            {
                editableLayers.add(allLayers.get(i));
            }
        }
        return editableLayers;

    }

    public static boolean deleteReferenceByLayer(Context context, String databasePath, GpkgLayer layer) throws InvalidAppConfigException, StyleException {
        try {
            LayerSettingsDAO dao = new LayerSettingsDAO(DatabaseFactory.getDatabase(context, databasePath));

            return dao.delete(layer.getName());

        } catch (DAOException e) {
            throw new StyleException(e.getMessage(), e);
        }
    }

    private static void getLayerSettings(Context context, Project project, String layerName) throws SettingsException, InvalidAppConfigException {
        try {
            LayerSettingsDAO layerSettingsDAO = new LayerSettingsDAO(DatabaseFactory.getDatabase(context, project.getFilePath()));

           HashMap<String, String> layerSettings = layerSettingsDAO.get(layerName);

        }  catch (DAOException e) {
            throw new SettingsException(e.getMessage(), e);
        }
    }

    public static boolean checkForModifiedLayer(Context context, Project project) throws SettingsException, InvalidAppConfigException {
        try {
            LayerSettingsDAO layerSettingsDAO = new LayerSettingsDAO(DatabaseFactory.getDatabase(context, project.getFilePath()));

            boolean modifiedLayer= layerSettingsDAO.hasModifiedLayer();

            return modifiedLayer;

        }  catch (DAOException e) {
            throw new SettingsException(e.getMessage(), e);
        }

    }

    public static void enableOnlyOneGatheringLayer(Context context, Project project, ArrayList<GpkgLayer> layers) throws SettingsException, InvalidAppConfigException {
        boolean foundEnabled = false;
        for (GpkgLayer layer:layers) {
            if(layer.isEditable())
            {
                if(layer.isEnabled())
                {
                    if(!foundEnabled)
                    {
                        foundEnabled = true;
                    }
                    else
                    {
                        layer.setEnabled(false);
                    }

                }
            }
        }
        updateLayerSettings(context, project, layers);
    }
}