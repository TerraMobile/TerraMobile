package br.org.funcate.terramobile.model.db.dao;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.DatabaseHelper;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by andre on 17/03/16.
 */
public class LayerFormDAO {
    private DatabaseHelper database;
    public static final String TABLE_NAME="TM_LAYER_FORM";

    public LayerFormDAO(DatabaseHelper database) throws InvalidAppConfigException, DAOException {
        if(database!=null)
        {
            this.database = database;
        }
        else
        {
            throw new DAOException(ResourceHelper.getStringResource(R.string.invalid_database_exception));
        }
    }

    public boolean delete(String layerName) throws InvalidAppConfigException, DAOException {
        try {
            SQLiteDatabase db = database.getWritableDatabase();
            if (db != null) {

                String[] args = {layerName};
                if (db.delete(TABLE_NAME, "gpkg_layer_identify=?", args) != -1) {
                    db.close();
                    return true;
                }
                db.close();
            }
            return false;
        } catch (SQLiteException e) {
            e.printStackTrace();
            throw new DAOException(ResourceHelper.getStringResource(R.string.layer_form_delete_exception),e);
        }
    }
}
