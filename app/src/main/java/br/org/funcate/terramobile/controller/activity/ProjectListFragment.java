package br.org.funcate.terramobile.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.tasks.DownloadTask;
import br.org.funcate.terramobile.controller.activity.tasks.ProjectListTask;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.model.domain.Setting;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.SettingsException;
import br.org.funcate.terramobile.model.service.SettingsService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.Util;
import br.org.funcate.terramobile.view.ProjectListAdapter;

/**
 * DialogFragment to shows a list of packages availables to download
 *
 * Created by marcelo on 5/25/15.
 */
public class ProjectListFragment extends DialogFragment{
    private ListView lVProject;
    private ProjectListAdapter projectListAdapter;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.fragment_project_list, null);

        lVProject = (ListView)v.findViewById(R.id.lVProject);

        return new AlertDialog.Builder(
                getActivity()).
                setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                }).
                setView(v).
                setCancelable(true).
                setTitle(Util.isConnected(getActivity()) ? getActivity().getString(R.string.available_projects) : getActivity().getString(R.string.available_projects) + " (" + getActivity().getString(R.string.offline) + ")").
                create();
    }

    @Override
    public void onStart() {
        super.onStart();
        String terramobileUrl = null;

        terramobileUrl = ((MainActivity)getActivity()).getMainController().getServerURL();

        if(terramobileUrl != null)
        {
            new ProjectListTask((MainActivity)getActivity()).execute(terramobileUrl + "listprojects");
        }


    }

    public void setListItems(ArrayList<Project> arrayList) {
        projectListAdapter = new ProjectListAdapter(getActivity(), R.id.tVProjectName, arrayList);
        lVProject.setAdapter(projectListAdapter);
    }

    public ProjectListAdapter getProjectListAdapter() {
        return projectListAdapter;
    }

    public DownloadTask getDownloadTask() {
        return ((ProjectListAdapter)lVProject.getAdapter()).getDownloadTask();
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        if (getDialog() == null) {  // Returns mDialog
            // Tells DialogFragment to not use the fragment as a dialog, and so won't try to use mDialog
            setShowsDialog(false);
        }
        super.onActivityCreated(arg0);  // Will now complete and not crash
    }

}