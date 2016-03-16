package br.org.funcate.terramobile.model.service;

import android.content.Context;

import com.augtech.geoapi.geometry.BoundingBoxImpl;

import org.opengis.geometry.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
     * @param projectPath
     */
   public static BoundingBox getProjectDefaultBoundingBox(Context context, String projectPath) throws InvalidAppConfigException, ProjectException {
       try {
           Setting xmin = SettingsService.get(context,"default_xmin",projectPath);
           Setting ymin = SettingsService.get(context,"default_ymin",projectPath);
           Setting xmax = SettingsService.get(context,"default_xmax",projectPath);
           Setting ymax = SettingsService.get(context,"default_ymax",projectPath);

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

    /**
     * This method retrieve the project description from the project GPKG file.
     * @param context
     * @param projectPath
     */
    public static String getDescription(Context context, String projectPath) throws InvalidAppConfigException, ProjectException {
        String descStr = "";
        try {
            Setting descriptionSetting = SettingsService.get(context,"description",projectPath);

                if(descriptionSetting!=null)
                {
                    descStr = descriptionSetting.getValue();
                }

            } catch (SettingsException e) {
            throw new ProjectException(ResourceHelper.getStringResource(R.string.failed_getting_project_setting),e);
        }
        return descStr;

    }

    /**
     * This method retrieve the project description from the project GPKG file.
     * @param context
     * @param projectPath
     */
    public static Date getCreationDate(Context context, String projectPath) throws InvalidAppConfigException, ProjectException {
        Date creationDate = null;
        try {
            Setting dateSetting = SettingsService.get(context,"creation_date",projectPath);

            if(dateSetting!=null)
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat();
                creationDate = dateFormat.parse(dateSetting.getValue());
            }

        } catch (SettingsException e) {
            throw new ProjectException(ResourceHelper.getStringResource(R.string.failed_getting_project_setting),e);
        } catch (ParseException e) {
            throw new ProjectException(ResourceHelper.getStringResource(R.string.failed_getting_project_creation_date),e);
        }
        return creationDate;

    }

    public static String getUUID(Context context, String projectPath) throws InvalidAppConfigException, ProjectException {
        String UUID = "";
        try {
            Setting descriptionSetting = SettingsService.get(context,"project_id",projectPath);

            if(descriptionSetting!=null)
            {
                UUID = descriptionSetting.getValue();
            }

        } catch (SettingsException e) {
            throw new ProjectException(ResourceHelper.getStringResource(R.string.failed_getting_project_setting),e);
        }
        return UUID;

    }

    public static String getStatus(Context context, String projectPath) throws InvalidAppConfigException, ProjectException {
        String status = "";
        try {
            Setting descriptionSetting = SettingsService.get(context,"project_status",projectPath);

            if(descriptionSetting!=null)
            {
                status = descriptionSetting.getValue();
            }

        } catch (SettingsException e) {
            throw new ProjectException(ResourceHelper.getStringResource(R.string.failed_getting_project_setting),e);
        }
        return status;

    }




}
