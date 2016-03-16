package br.org.funcate.terramobile.controller.activity.settings;

import android.app.Fragment;
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
public class ServerURLController {

    Fragment fragment;

    public ServerURLController(Fragment fragment)
    {
        this.fragment = fragment;
    }

    public void save(String serverURL)
    {
        try {
            Setting serverURLSet = new Setting("terramobile_url" , serverURL);

            SettingsService.update((Context)fragment.getActivity(), serverURLSet, ApplicationDatabase.DATABASE_NAME);

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(fragment.getActivity(), R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(fragment.getActivity(), R.string.error, e.getMessage());
        }
    }

    public void getServer(String serverURL)
    {
        try {
            Setting serverURLSet = new Setting("terramobile_url" , serverURL);

            SettingsService.update((Context)fragment.getActivity(), serverURLSet, ApplicationDatabase.DATABASE_NAME);

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(fragment.getActivity(), R.string.error, e.getMessage());
        } catch (SettingsException e) {
            e.printStackTrace();
            Message.showErrorMessage(fragment.getActivity(), R.string.error, e.getMessage());
        }
    }

}
