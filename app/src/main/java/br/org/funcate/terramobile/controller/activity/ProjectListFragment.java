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
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
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
//        else
//            Message.showErrorMessage(getActivity(), R.string.error, R.string.not_logged);

        lVProject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File projectPath = ResourceUtil.getDirectory(getResources().getString(R.string.app_workspace_dir));
                final String destinationFilePath = projectPath.getPath();

                Project project = (Project) lVProject.getItemAtPosition(position);
                final String prjName = project.toString();

                if (ResourceUtil.getGeoPackageByName(projectPath, getActivity().getResources().getString(R.string.geopackage_extension), project.toString()) != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            getActivity());
                    builder.setTitle(getActivity().getString(R.string.project_remove_title));
                    builder.setMessage(getActivity().getString(R.string.project_download_confirm));
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if(settings != null)
                                        downloadTask = (DownloadTask) new DownloadTask(destinationFilePath+"/"+prjName, destinationFilePath, (MainActivity) getActivity()).execute(settings.getUrl()+"/getprojects/userName/"+prjName);
//                                    else
//                                        Message.showErrorMessage(getActivity(), R.string.error, R.string.not_logged);
                                }
                            });
                    builder.setNegativeButton(R.string.no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else {
                    if (settings != null)
                        downloadTask = (DownloadTask) new DownloadTask(destinationFilePath + "/" + prjName, destinationFilePath, (MainActivity) getActivity()).execute(settings.getUrl() + "/getprojects/userName/" + prjName);
//                else
//                    Message.showErrorMessage(getActivity(), R.string.error, R.string.not_logged);
                }
            }
        });
        lVProject.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                String prjName = lVProject.getItemAtPosition(position).toString();
                File directory = ResourceUtil.getDirectory(getActivity().getResources().getString(R.string.app_workspace_dir));
                File file = ResourceUtil.getGeoPackageByName(directory, getResources().getString(R.string.geopackage_extension), prjName);
                if(file != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            getActivity());
                    builder.setTitle(getActivity().getString(R.string.project_remove_title));
                    builder.setMessage(getActivity().getString(R.string.project_remove_confirm));
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String fileName = lVProject.getItemAtPosition(position).toString();
                                    String projectName = fileName.substring(0, fileName.indexOf('.'));
                                    File directory = ResourceUtil.getDirectory(getActivity().getResources().getString(R.string.app_workspace_dir));
                                    File file = ResourceUtil.getGeoPackageByName(directory, getResources().getString(R.string.geopackage_extension), fileName);
                                    ProjectDAO projectDAO = new ProjectDAO(getActivity());
                                    Project project = projectDAO.getByName(projectName);
                                    if (project != null && file != null) {
                                        if (projectDAO.remove(project.getId())) {
                                            if (file.delete()) {
                                                if(((MainActivity)getActivity()).getProject().toString().equals(projectName)){
                                                    if(ResourceUtil.getGeoPackageFiles(directory, getResources().getString(R.string.geopackage_extension)).size() > 0)
                                                        ((MainActivity) getActivity()).setProject(projectDAO.getFirstProject());
                                                    else
                                                        ((MainActivity) getActivity()).setProject(null);
                                                }
                                                Message.showSuccessMessage(getActivity(), R.string.success, R.string.project_removed_successfully);
                                                ((ProjectListAdapter) lVProject.getAdapter()).notifyDataSetChanged();
                                            } else {
                                                Message.showSuccessMessage(getActivity(), R.string.success, R.string.error_removing_project);
                                                projectDAO.insert(project);
                                            }
                                        } else
                                            Message.showSuccessMessage(getActivity(), R.string.success, R.string.error_removing_project);
                                    } else
                                        Message.showSuccessMessage(getActivity(), R.string.success, R.string.error_removing_project);

                                }
                            });
                    builder.setNegativeButton(R.string.no,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                return false;
            }
        });

        return new AlertDialog.Builder(
                getActivity()).
                setNegativeButton(R.string.close,new DialogInterface.OnClickListener() {
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

    public void setListItems(ArrayList<Project> arrayList) {
        ProjectListAdapter projectListAdapter = new ProjectListAdapter(getActivity(), R.id.tVProjectName, arrayList);
        lVProject.setAdapter(projectListAdapter);
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }
}