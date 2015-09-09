package br.org.funcate.terramobile.controller.activity.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.service.SettingsService;
import br.org.funcate.terramobile.util.GlobalParameters;
import br.org.funcate.terramobile.util.Message;

/**
 * Created by Andre Carvalho on 02/09/15.
 */
public class GPSSettingController {

    private Context context;// SettingActivity
    private final String keyLocation = "gps_location_state";
    private final String keyCenter = "gps_center_state";

    public GPSSettingController(Context context)
    {
        this.context = context;
    }

    public void saveGPSLocation(boolean enable)
    {
        try {
            String value = Boolean.toString(enable);
            Setting setting = SettingsService.get(context, keyLocation, ApplicationDatabase.DATABASE_NAME);
            if(setting==null)
            {
                setting = new Setting(keyLocation, value);
                SettingsService.insert(context, setting, ApplicationDatabase.DATABASE_NAME);
            }else {
                setting.setValue(value);
                SettingsService.update(context, setting, ApplicationDatabase.DATABASE_NAME);
            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage((Activity)context, R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage((Activity)context, R.string.error, e.getMessage());
        } catch (DAOException e) {
            e.printStackTrace();
            Message.showErrorMessage((Activity) context, R.string.error, e.getMessage());
        }
    }

    public void saveKeepCenter(boolean enable)
    {
        try {
            String value = Boolean.toString(enable);
            Setting setting = SettingsService.get(context, keyCenter, ApplicationDatabase.DATABASE_NAME);
            if(setting==null)
            {
                setting = new Setting(keyCenter, value);
                SettingsService.insert(context, setting, ApplicationDatabase.DATABASE_NAME);
            }else {
                setting.setValue(value);
                SettingsService.update(context, setting, ApplicationDatabase.DATABASE_NAME);
            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage((Activity)context, R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage((Activity)context, R.string.error, e.getMessage());
        } catch (DAOException e) {
            e.printStackTrace();
            Message.showErrorMessage((Activity) context, R.string.error, e.getMessage());
        }
    }

    public Boolean getGPSLocationState()
    {
        return getSettingValue(keyLocation);
    }

    public Boolean getGPSCenterState()
    {
        return getSettingValue(keyCenter);
    }

    /**
     * Used when one option is selected into UI related with GPS Tracker.
     * @param state
     */
    public void applyGPSLocation(boolean state)
    {
        Intent in = new Intent(GlobalParameters.ACTION_BROADCAST_MAIN_ACTIVITY);
        in.putExtra(GlobalParameters.STATE_GPS_LOCATION, state);
        context.sendBroadcast(in);
    }

    public void applyKeepCenter(boolean state)
    {
        Intent in = new Intent(GlobalParameters.ACTION_BROADCAST_MAIN_ACTIVITY);
        in.putExtra(GlobalParameters.STATE_GPS_CENTER, state);
        context.sendBroadcast(in);
    }

    private Boolean getSettingValue(String key)
    {
        Boolean stateValue=null;
        try {

            Setting setting = SettingsService.get(context, key, ApplicationDatabase.DATABASE_NAME);

            if(setting!=null)
            {
                String value = setting.getValue();
                stateValue = Boolean.parseBoolean(value);
            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage((Activity)context, R.string.error, e.getMessage());
        } catch (SettingsException es) {
            es.printStackTrace();
            Message.showErrorMessage((Activity)context, R.string.error, es.getMessage());
        }
        return stateValue;
    }
}
