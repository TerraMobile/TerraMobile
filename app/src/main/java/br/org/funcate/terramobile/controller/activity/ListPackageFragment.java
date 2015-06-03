package br.org.funcate.terramobile.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.tasks.DownloadTask;
import br.org.funcate.terramobile.controller.activity.tasks.PackageListTask;
import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceUtil;

/**
 * DialogFragment to shows a list of packages availables to download
 *
 * Created by marcelo on 5/25/15.
 */
public class ListPackageFragment extends DialogFragment{
    private ListView lVPackage;

    private ArrayAdapter<String> arrayAdapter;

    private Settings settings;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.fragment_list_package, null);

        this.lVPackage = (ListView)v.findViewById(R.id.lVPackage);

        this.arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);

        SettingsDAO settingsDAO = new SettingsDAO(getActivity());
        this.settings = settingsDAO.getById(1);
        if(this.settings != null)
            new PackageListTask((MainActivity)this.getActivity()).execute(this.settings.getUrl() + "/getlistfiles/userName");
        else
            Message.showErrorMessage(getActivity(), R.string.error, R.string.not_logged);
        lVPackage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File appPath = ResourceUtil.getDirectory(getResources().getString(R.string.app_workspace_dir));
                String destinationFilePath = appPath.getPath();
                String prjName = lVPackage.getItemAtPosition(position).toString(); // Package name to concatenate with the server url
                if(settings != null)
                    new DownloadTask(destinationFilePath+"/"+prjName, destinationFilePath, true, (MainActivity) getActivity()).execute(settings.getUrl()+"/getprojects/userName/"+prjName);
                else
                    Message.showErrorMessage(getActivity(), R.string.error, R.string.not_logged);
            }
        });

        return new AlertDialog.Builder(
                getActivity()).
                setNegativeButton(R.string.btnCancel,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (DialogInterface dialog,int which){
                        dismiss();
                    }
                }).
                setView(v).
                setCancelable(true).
                setTitle(R.string.available_projects).
                create();
    }

    public void setListItems(ArrayList<String> arrayList) {
        arrayAdapter.addAll(arrayList);
        lVPackage.setAdapter(arrayAdapter);
    }
}