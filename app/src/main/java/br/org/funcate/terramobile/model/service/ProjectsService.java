package br.org.funcate.terramobile.model.service;

import android.content.Context;

import com.augtech.geoapi.geometry.BoundingBoxImpl;

import org.opengis.geometry.BoundingBox;
import org.osmdroid.util.GeoPoint;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.ProjectDatabase;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.ProjectException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by bogo on 31/07/15.
 */
public class ProjectsService {

    private ProjectsService()
    {

    }

    /**
     * This method retrieve from project the initial default bbox center.
     * @param context
     * @param databasePath
     */
   public static BoundingBox getProjectDefaultBoundingBox(Context context, String databasePath) throws InvalidAppConfigException, ProjectException {
       try {
           Setting xmin = SettingsService.get(context,"default_xmin",databasePath);
           Setting ymin = SettingsService.get(context,"default_ymin",databasePath);
           Setting xmax = SettingsService.get(context,"default_xmax",databasePath);
           Setting ymax = SettingsService.get(context,"default_ymax",databasePath);

           if(xmin!=null && ymin!=null && xmax!=null && ymax!=null)
           {
               BoundingBox bbox = new BoundingBoxImpl(new Double(xmin.getValue()), new Double(xmax.getValue()), new Double(ymin.getValue()), new Double(ymax.getValue()));
               return bbox;
           }

       } catch (SettingsException e) {
            throw new ProjectException(ResourceHelper.getStringResource(R.string.failed_getting_project_setting),e);
       }
       return null;

   }

}
