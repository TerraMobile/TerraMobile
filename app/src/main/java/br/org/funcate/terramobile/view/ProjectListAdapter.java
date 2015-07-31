package br.org.funcate.terramobile.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.controller.activity.tasks.DownloadTask;
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.model.Settings;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.model.db.dao.SettingsDAO;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

/**
 * Created by marcelo on 6/10/15.
 */
public class ProjectListAdapter extends ArrayAdapter<Project> implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    private Context context;

    private ArrayList projectList;

    private DownloadTask downloadTask;

    public ProjectListAdapter(Context context, int textViewResourceId, ArrayList<Project> projectList) {
        super(context, textViewResourceId, projectList);
        this.context = context;
        this.projectList = projectList;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.project_item, null);

        ((ListView)parent).setOnItemClickListener(this);
        ((ListView)parent).setOnItemLongClickListener(this);

        TextView tVProject = (TextView) convertView.findViewById(R.id.tVProjectName);
        ImageView iVDownloaded = (ImageView) convertView.findViewById(R.id.iVDownloaded);
//        ImageView iVUpdated = (ImageView)convertView.findViewById(R.id.iVUpdated);

        Project project = (Project) projectList.get(position);

        tVProject.setText(project.toString());

        RadioButton rBCurrentProject = (RadioButton) convertView.findViewById(R.id.rBCurrentProject);
        rBCurrentProject.setTag(project);

        Project currentProject = ((MainActivity) context).getProject();
        if (currentProject != null && currentProject.toString().equals(project.toString()))
            rBCurrentProject.setChecked(true);

        File directory = Util.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        File projectFile = Util.getGeoPackageByName(directory, context.getResources().getString(R.string.geopackage_extension), project.getName());
        if(project.isDownloaded() != 0 && projectFile != null){
            rBCurrentProject.setEnabled(true);
            iVDownloaded.setImageResource(R.drawable.downloaded);
        }

        rBCurrentProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int count = 0; count < parent.getChildCount(); count++) {
                    View childView = parent.getChildAt(count);
                    RadioButton radioButton = (RadioButton) childView.findViewById(R.id.rBCurrentProject);
                    radioButton.setChecked(false);
                }
                RadioButton rBNewCurrentProject = (RadioButton) v;
                if (!rBNewCurrentProject.isChecked()) {
                    rBNewCurrentProject.setChecked(true);

                    Project newCurrentProject = (Project) rBNewCurrentProject.getTag();
                    try {
                        ((MainActivity) context).setProject(newCurrentProject);
                    } catch (InvalidAppConfigException e) {
                        e.printStackTrace();
                        Message.showErrorMessage(((MainActivity) context), R.string.error, e.getMessage());
                    }
                }
            }
        });

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (Util.isConnected(context)) {
            final Project project = (Project) parent.getItemAtPosition(position);
            final String fileName = project.getName();

            File tempPath = Util.getDirectory(context.getString(R.string.app_workspace_temp_dir));
            File projectPath = Util.getDirectory(context.getString(R.string.app_workspace_dir));

            final String tempFilePath = tempPath.getPath() + "/" + fileName;
            final String projectFilePath = projectPath.getPath();

            SettingsDAO settingsDAO = new SettingsDAO(context);
            final Settings settings = settingsDAO.getById(1);

            if (Util.getGeoPackageByName(projectPath, context.getResources().getString(R.string.geopackage_extension), project.getName()) != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        context);
                builder.setTitle(context.getString(R.string.project_remove_title));
                builder.setMessage(context.getString(R.string.project_download_confirm));
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (settings != null)
                                    downloadTask = (DownloadTask) new DownloadTask(tempFilePath, projectFilePath, fileName, (MainActivity) context).execute(settings.getUrl() + "/getprojects/userName/" + fileName);
//                              else
//                                   Message.showErrorMessage(context, R.string.error, R.string.not_logged);
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
                    downloadTask = (DownloadTask) new DownloadTask(tempFilePath, projectFilePath, fileName, (MainActivity) context).execute(settings.getUrl() + "/getprojects/userName/" + fileName);
//                else
//                    Message.showErrorMessage(context, R.string.error, R.string.not_logged);
            }
        } else
            Message.showErrorMessage((MainActivity) context, R.string.error, R.string.no_connection);
    }

    /**
     * TODO: REFACTOR
     * @param parent
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
        Project project = (Project) parent.getItemAtPosition(position);
        final String projectName = project.toString();
        final String fileName = project.getName();
        File directory = Util.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        File file = Util.getGeoPackageByName(directory, context.getString(R.string.geopackage_extension), fileName);
        if (file != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    context);
            builder.setTitle(context.getString(R.string.project_remove_title));
            builder.setMessage(context.getString(R.string.project_remove_confirm));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            File directory = Util.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
                            File file = Util.getGeoPackageByName(directory, context.getString(R.string.geopackage_extension), fileName);
                            ProjectDAO projectDAO = new ProjectDAO(context);
                            Project project = projectDAO.getByName(fileName);
                            if (project != null && file != null) {
                                if (projectDAO.remove(project.getId())) {
                                    if (file.delete()) {
                                        if (((MainActivity) context).getProject().toString().equals(projectName)) {
                                            try {
                                                if (Util.getGeoPackageFiles(directory, context.getString(R.string.geopackage_extension)).size() > 0)
                                                    ((MainActivity) context).setProject(projectDAO.getFirstProject());
                                                else {
                                                    ((MainActivity) context).setProject(null);
                                                }
                                            } catch (InvalidAppConfigException e) {
                                                e.printStackTrace();
                                                Message.showErrorMessage((MainActivity) context, R.string.error, e.getMessage());
                                            }
                                        }
                                        Message.showSuccessMessage((MainActivity) context, R.string.success, R.string.project_removed_successfully);
                                        if (!Util.isConnected(context)) {
                                            ProjectListAdapter.this.remove(project);
                                            projectList.remove(position);
                                        }
                                        notifyDataSetChanged();
                                    } else {
                                        Message.showSuccessMessage((MainActivity) context, R.string.success, R.string.error_removing_project);
                                        projectDAO.insert(project);
                                    }
                                } else
                                    Message.showSuccessMessage((MainActivity) context, R.string.success, R.string.error_removing_project);
                            } else
                                Message.showSuccessMessage((MainActivity) context, R.string.success, R.string.error_removing_project);

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

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

}