package br.org.funcate.terramobile.model.service;

import android.content.Context;

import org.opengis.geometry.BoundingBox;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.ProjectDatabase;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
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
}
