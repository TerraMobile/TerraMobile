package br.org.funcate.terramobile.controller.activity;

import org.opengis.geometry.BoundingBox;

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
public class MainController {

    private MainActivity mainActivity;

    private MenuMapController menuMapController;

    public MainController(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
        menuMapController = new MenuMapController(mainActivity);
    }

    public String getServerURL()
    {
        return getSettingValue("terramobile_url");
    }

    public String getUsername()
    {
        return getSettingValue("username");
    }

    public String getPassword()
    {
        return getSettingValue("password");
    }

    public String getCurrentProject()
    {
        return getSettingValue("current_project");
    }


    private String getSettingValue(String key)
    {
        try {

            Setting setting = SettingsService.get(mainActivity, key, ApplicationDatabase.DATABASE_NAME);

            if(setting!=null)
            {
                return setting.getValue();
            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        }
        return null;
    }

    public MenuMapController getMenuMapController() {
        return menuMapController;
    }

    public void setMenuMapController(MenuMapController menuMapController) {
        this.menuMapController = menuMapController;
    }

}
