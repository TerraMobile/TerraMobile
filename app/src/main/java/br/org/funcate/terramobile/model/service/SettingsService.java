package br.org.funcate.terramobile.model.service;

import android.content.Context;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.DatabaseHelper;
import br.org.funcate.terramobile.model.db.ProjectDatabase;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by bogo on 31/07/15.
 */
public class SettingsService {

    private SettingsService()
    {

    }

    public static void initApplicationSettings(Context context) throws InvalidAppConfigException, SettingsException {

        try {

            SettingsDAO dao = new SettingsDAO(DatabaseFactory.getDatabase(context, ApplicationDatabase.DATABASE_NAME));

            String serverUrl = ResourceHelper.getStringResource(R.string.terramobile_url);

            Setting terramobileUrl = new Setting("terramobile_url", serverUrl);

            Setting currentProject = new Setting("current_project", null);

            Setting username = new Setting("username", null);

            Setting password = new Setting("password", null);

            if(!settingExists(context, terramobileUrl, ApplicationDatabase.DATABASE_NAME))
            {
                insert(context, terramobileUrl, ApplicationDatabase.DATABASE_NAME);
            }

            if(!settingExists(context, currentProject, ApplicationDatabase.DATABASE_NAME))
            {
                insert(context, currentProject, ApplicationDatabase.DATABASE_NAME);
            }

            if(!settingExists(context, username, ApplicationDatabase.DATABASE_NAME))
            {
                insert(context, username, ApplicationDatabase.DATABASE_NAME);
            }

            if(!settingExists(context, password, ApplicationDatabase.DATABASE_NAME))
            {
                insert(context, password, ApplicationDatabase.DATABASE_NAME);
            }
        } catch (DAOException e) {
            e.printStackTrace();
            throw new SettingsException(ResourceHelper.getStringResource(R.string.settings_init_exception));
        }

    }

    public static void initProjectSettings(Context context, Project project) throws InvalidAppConfigException, SettingsException {

        ProjectDatabase database = (ProjectDatabase) DatabaseFactory.getDatabase(context, project.getFilePath());

    }

    public static boolean insert(Context context, Setting setting, String databasePath) throws DAOException, InvalidAppConfigException
    {

        SettingsDAO dao = new SettingsDAO(DatabaseFactory.getDatabase(context, databasePath));

        return dao.insert(setting);
    }

    private static boolean settingExists(Context context, Setting setting, String databasePath) throws DAOException, InvalidAppConfigException
    {
        SettingsDAO dao = new SettingsDAO(DatabaseFactory.getDatabase(context, databasePath));

        return dao.get(setting)!=null;
    }

    public static Setting get(Context context, String key, String databasePath) throws InvalidAppConfigException, SettingsException {

        Setting setting = new Setting(key, null);
        try {
            SettingsDAO dao = new SettingsDAO(DatabaseFactory.getDatabase(context, databasePath));

            setting = dao.get(setting);
        } catch (DAOException e) {
            throw new SettingsException(e.getMessage());
        }

        return setting;
    }

    public static boolean update(Context context, Setting setting, String databasePath) throws SettingsException, InvalidAppConfigException {

        boolean success = false;
        try {
            SettingsDAO dao = new SettingsDAO(DatabaseFactory.getDatabase(context, databasePath));
            success = dao.update(setting);
        } catch (DAOException e) {
            throw new SettingsException(e.getMessage());
        }
        return success;
    }

}
