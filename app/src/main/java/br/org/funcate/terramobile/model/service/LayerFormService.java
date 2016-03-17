package br.org.funcate.terramobile.model.service;

import android.content.Context;

import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.LayerFormDAO;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by andre on 17/03/16.
 */
public class LayerFormService {

    private LayerFormService() {

    }

    public static boolean deleteReferenceByLayer(Context context, String databasePath, GpkgLayer layer) throws InvalidAppConfigException, StyleException {
        try {
            LayerFormDAO dao = new LayerFormDAO(DatabaseFactory.getDatabase(context, databasePath));

            return dao.delete(layer.getName());

        } catch (DAOException e) {
            throw new StyleException(e.getMessage(), e);
        }
    }
}
