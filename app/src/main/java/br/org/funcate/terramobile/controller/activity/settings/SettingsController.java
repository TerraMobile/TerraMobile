package br.org.funcate.terramobile.controller.activity.settings;

import android.content.Context;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.service.SettingsService;
import br.org.funcate.terramobile.util.Message;

/**
 * Created by bogo on 31/07/15.
 */
public class SettingsController {

    SettingsActivity activity;

    public SettingsController(SettingsActivity activity)
    {
        this.activity = activity;
    }

    public String getServerURL()
    {
        try {

            Setting setting = SettingsService.get(((Context)activity), "terramobile_url", ApplicationDatabase.DATABASE_NAME);

            if(setting!=null)
            {
                return setting.getValue();
            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(activity, R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(activity, R.string.error, e.getMessage());
        }
        return null;
    }

}
