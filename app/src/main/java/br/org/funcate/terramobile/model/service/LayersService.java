package br.org.funcate.terramobile.model.service;

import android.content.Context;

import org.opengis.geometry.BoundingBox;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.ProjectDatabase;
import br.org.funcate.terramobile.model.db.dao.LayerSettingsDAO;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.ResourceHelper;

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
            if(box==null)
            {
                box = layers.get(i).getBox();
            }
            else
            {
                box.include(layers.get(i).getBox());
            }
        }
        return box;
    }

    public static void sortLayersByIndex(ArrayList<GpkgLayer> layers) {
        Collections.sort(layers, new Comparator<GpkgLayer>() {
            @Override
            public int compare(GpkgLayer gpkgLayer, GpkgLayer gpkgLayer2) {
                if (gpkgLayer.getIndexOverlay() > gpkgLayer2.getIndexOverlay()) {
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
        sortLayersByIndex(layers);
        return layers;
    }

    private static void loadLayersSettings(Context context, Project project, ArrayList<GpkgLayer> layers) throws SettingsException, InvalidAppConfigException {
        try {
            LayerSettingsDAO layerSettingsDAO = new LayerSettingsDAO(DatabaseFactory.getDatabase(context, project.getFilePath()));

            for(int i = 0; i < layers.size(); i++) {
                layerSettingsDAO.load(layers.get(i));
            }

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
        }

        return layers;
    }

}
