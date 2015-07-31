package br.org.funcate.terramobile.model.service;

import android.content.Context;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
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

    public static void initSettings(Context context) throws InvalidAppConfigException, SettingsException {
        SettingsDAO dao = new SettingsDAO(context);

        String serverUrl = ResourceHelper.getStringResource(R.string.terramobile_url);

        Setting terramobileUrl = new Setting("terramobile_url", serverUrl);
        Setting currentProject = new Setting("current_project", null);
        Setting username = new Setting("username", null);
        Setting password = new Setting("password", null);

        try {

            if(!settingExists(context, terramobileUrl))
            {
                insert(context, terramobileUrl);
            }

            if(!settingExists(context, currentProject))
            {
                insert(context, currentProject);
            }

            if(!settingExists(context, username))
            {
                insert(context, username);
            }

            if(!settingExists(context, password))
            {
                insert(context, password);
            }
        } catch (DAOException e) {
            e.printStackTrace();
            throw new SettingsException(ResourceHelper.getStringResource(R.string.settings_init_exception));
        }


    }

    public static boolean insert(Context context, Setting setting) throws DAOException, InvalidAppConfigException
    {

        SettingsDAO dao = new SettingsDAO(context);

        return dao.insert(setting);
    }

    private static boolean settingExists(Context context, Setting setting) throws DAOException, InvalidAppConfigException
    {
        SettingsDAO dao = new SettingsDAO(context);

        return dao.get(setting)!=null;
    }

    public static Setting get(Context context, String key) throws InvalidAppConfigException, SettingsException {
        SettingsDAO dao = new SettingsDAO(context);

        Setting setting = new Setting(key, null);

        try {
            setting = dao.get(setting);
        } catch (DAOException e) {
            throw new SettingsException(e.getMessage());
        }

        return setting;
    }

    public static boolean update(Context context, Setting setting) throws SettingsException, InvalidAppConfigException
    {

        SettingsDAO dao = new SettingsDAO(context);

        boolean success = false;
        try {
            success = dao.update(setting);
        } catch (DAOException e) {
            throw new SettingsException(e.getMessage());
        }
        return success;
    }
}
