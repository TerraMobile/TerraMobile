package br.org.funcate.terramobile.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.tasks.DownloadTask;
import br.org.funcate.terramobile.controller.activity.tasks.ProjectListTask;
import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceUtil;
import br.org.funcate.terramobile.view.ProjectListAdapter;

/**
 * DialogFragment to shows a list of packages availables to download
 *
 * Created by marcelo on 5/25/15.
 */
public class ProjectListFragment extends DialogFragment{
    private ListView lVProject;

    private ProjectListAdapter projectListAdapter;

    private Settings settings;

    private DownloadTask downloadTask;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.fragment_project_list, null);

        lVProject = (ListView)v.findViewById(R.id.lVProject);
        SettingsDAO settingsDAO = new SettingsDAO(getActivity());
        this.settings = settingsDAO.getById(1);
        if(this.settings != null)
            new ProjectListTask((MainActivity)this.getActivity()).execute(this.settings.getUrl() + "/getlistfiles/userName");
        else
            Message.showErrorMessage(getActivity(), R.string.error, R.string.not_logged);
        lVProject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File appPath = ResourceUtil.getDirectory(getResources().getString(R.string.app_workspace_dir));
                String destinationFilePath = appPath.getPath();
                String prjName = lVProject.getItemAtPosition(position).toString();
                if(settings != null)
                    downloadTask = (DownloadTask) new DownloadTask(destinationFilePath+"/"+prjName, destinationFilePath, true, (MainActivity) getActivity()).execute(settings.getUrl()+"/getprojects/userName/"+prjName);
                else
                    Message.showErrorMessage(getActivity(), R.string.error, R.string.not_logged);
            }
        });

        return new AlertDialog.Builder(
                getActivity()).
                setNegativeButton(R.string.ok,new DialogInterface.OnClickListener() {
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
        this.projectListAdapter = new ProjectListAdapter(getActivity(), R.id.tVProjectName, arrayList);
        lVProject.setAdapter(projectListAdapter);
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }
}