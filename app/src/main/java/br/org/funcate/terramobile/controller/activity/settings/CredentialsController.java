package br.org.funcate.terramobile.controller.activity.settings;

import android.app.Fragment;
import android.content.Context;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.service.SettingsService;
import br.org.funcate.terramobile.util.Message;

/**
 * Created by bogo on 31/07/15.
 */
public class CredentialsController {

    Fragment credentialFragment;

    public CredentialsController(Fragment credentialFragment)
    {
        this.credentialFragment = credentialFragment;
    }

    public void save(String username, String password)
    {
        try {
            Setting userSet = new Setting("username" , username);
            Setting passSet = new Setting("password" , password);

            SettingsService.update(credentialFragment.getActivity(), userSet);
            SettingsService.update(credentialFragment.getActivity(), passSet);

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(credentialFragment.getActivity(), R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(credentialFragment.getActivity(), R.string.error, e.getMessage());
        }
    }

    public String getUsername()
    {
        return getSettingValue("username");
    }

    public String getPassword()
    {
        return getSettingValue("password");
    }

    private String getSettingValue(String key)
    {
        try {

            Setting setting = SettingsService.get((Context)credentialFragment.getActivity(), key);

            if(setting!=null)
            {
                return setting.getValue();
            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(credentialFragment.getActivity(), R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(credentialFragment.getActivity(), R.string.error, e.getMessage());
        }
        return null;
    }

}
