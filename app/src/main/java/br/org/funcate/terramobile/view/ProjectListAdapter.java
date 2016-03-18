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
import br.org.funcate.terramobile.controller.activity.UploadProjectFragment;
import br.org.funcate.terramobile.controller.activity.tasks.DownloadTask;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.ProjectException;
import br.org.funcate.terramobile.model.service.ProjectsService;
import br.org.funcate.terramobile.util.Message;
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
        ImageView iVUploaded = (ImageView) convertView.findViewById(R.id.iVUploaded);

        Project project = (Project) projectList.get(position);

        tVProject.setText(project.toString());

        RadioButton rBCurrentProject = (RadioButton) convertView.findViewById(R.id.rBCurrentProject);
        rBCurrentProject.setTag(project);
        iVDownloaded.setTag(project);
        iVUploaded.setTag(project);
       // iVMoveToSD.setTag(project);


        Project currentProject = ((MainActivity) context).getMainController().getCurrentProject();
        if (currentProject != null && currentProject.toString().equals(project.toString()))
            rBCurrentProject.setChecked(true);

        File directory = Util.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        File projectFile = Util.getGeoPackageByName(directory, context.getResources().getString(R.string.geopackage_extension), project.getName());

/*
        if(project.isDownloaded() != 0 && projectFile != null){
            rBCurrentProject.setEnabled(true);
            iVDownloaded.setImageResource(R.drawable.downloaded);
        }
*/
        //Download and upload icon rules
        //If project not downloaded yet

        try {

            if(project.isDownloaded()==0 && projectFile == null)
            {
                iVDownloaded.setImageResource(R.drawable.download_enabled);
                iVUploaded.setImageResource(R.drawable.upload_disabled);

                iVDownloaded.setOnClickListener(onDownloadIconClick);
            }
            else
            {
                //If project already downloaded
                //if project hasn't UUID
                if(project.getUUID()==null||project.getUUID().isEmpty())
                {
                    iVUploaded.setImageResource(R.drawable.invalid_project);
                    iVDownloaded.setVisibility(View.INVISIBLE);

                }
                else
                {
                    //Has UUID, but is not on the server anymore
                    if(project.isOnTheAppOnly())
                    {
                        iVDownloaded.setImageResource(R.drawable.download_disabled);
                        iVUploaded.setImageResource(R.drawable.upload_disabled);
                    }
                    else
                    {
                        //Project is on the server and in the client and has no modification
                        if(!ProjectsService.isProjectModified(context, project))
                        {
                            iVDownloaded.setImageResource(R.drawable.download_enabled);
                            iVUploaded.setImageResource(R.drawable.upload_disabled);

                            iVDownloaded.setOnClickListener(onDownloadIconClick);
                        }
                        else
                        {
                            iVDownloaded.setImageResource(R.drawable.download_enabled_warning);
                            iVUploaded.setImageResource(R.drawable.upload_enabled);

                            iVDownloaded.setOnClickListener(onDownloadIconClick);
                            iVUploaded.setOnClickListener(onUploadIconClick);
                        }


                    }
                }

            }

        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(((MainActivity) context), R.string.error, e.getMessage());
        } catch (ProjectException e) {
            e.printStackTrace();
            Message.showErrorMessage(((MainActivity) context), R.string.error, e.getMessage());
        }



        rBCurrentProject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearProjectSelection((ListView) parent);
                selectProject((RadioButton) v);
            }
        });

        return convertView;
    }

/*    private View.OnClickListener onMoveToSDCard = new View.OnClickListener()
    {
        public void onClick(View v){
            if (Util.isConnected(context)) {
                final Project project = (Project) v.getTag();
                String fileInput = project.getFilePath();
                final String fileName = project.getName();

                String defaultAppDirectory = context.getResources().getString(R.string.app_workspace_dir);
                File appPublicDirectory = Util.getPublicDirectory(defaultAppDirectory);
                String fileOutput = appPublicDirectory.getAbsolutePath() + File.separator + fileName;
                Util.copyFile(fileInput, fileOutput);
                Util.applyAllPermission(appPublicDirectory);
                Util.applyAllPermission(new File(fileOutput));
                Util.startSync(appPublicDirectory.getParentFile(), context);
            }
        }
    };*/

    private View.OnClickListener onDownloadIconClick = new View.OnClickListener()
    {
        public void onClick(View v) {
            if (Util.isConnected(context)) {
                final Project project = (Project) v.getTag();
                final String fileName = project.getName();

                File tempPath = Util.getDirectory(context.getString(R.string.app_workspace_temp_dir));
                File projectPath = Util.getDirectory(context.getString(R.string.app_workspace_dir));

                final String tempFilePath = tempPath.getPath() + "/" + fileName;
                final String projectFilePath = projectPath.getPath();

                final String serverURL  = ((MainActivity) context).getMainController().getServerURL();

                if(serverURL==null)
                {
                    //Error Message already sent
                    return;
                }

                if (Util.getGeoPackageByName(projectPath, context.getResources().getString(R.string.geopackage_extension), project.getName()) != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            context);
                    builder.setTitle(context.getString(R.string.project_remove_title));
                    builder.setMessage(context.getString(R.string.project_download_confirm));
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    downloadTask = (DownloadTask) new DownloadTask(tempFilePath, projectFilePath, fileName, project.getUUID(), project.getStatus(), (MainActivity) context).execute(serverURL + "downloadproject");
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
                    downloadTask = (DownloadTask) new DownloadTask(tempFilePath, projectFilePath, fileName, project.getUUID(), project.getStatus(), (MainActivity) context).execute(serverURL + "downloadproject");
//                else
//                    Message.showErrorMessage(context, R.string.error, R.string.not_logged);
                }
            } else
                Message.showErrorMessage((MainActivity) context, R.string.error, R.string.no_connection);

        }
    };

    private View.OnClickListener onUploadIconClick = new View.OnClickListener() {
        public void onClick(View v) {
            if (Util.isConnected(context)) {
                final Project project = (Project) v.getTag();

                UploadProjectFragment uploadFragment = UploadProjectFragment.newInstance();
                uploadFragment.setProject(project);
                uploadFragment.show(((MainActivity) context).getFragmentManager(), "packageList");

            } else {
                Message.showErrorMessage((MainActivity) context, R.string.error, R.string.no_connection);
            }

        }
    };

    private void selectProject(RadioButton radioButton)
    {
        if (!radioButton.isChecked()) {
            radioButton.setChecked(true);
            boolean success = false;
            Project newCurrentProject = (Project) radioButton.getTag();

            if(newCurrentProject.isDownloaded()==1)
            {

                try {
                    success = ((MainActivity) context).getMainController().setCurrentProject(newCurrentProject);
                } catch (InvalidAppConfigException e)
                {
                    e.printStackTrace();
                    Message.showErrorMessage(((MainActivity) context), R.string.error, e.getMessage());
                }


            }
            else
            {
                Message.showErrorMessage((MainActivity) context, R.string.error, R.string.download_project_first);
            }
            if(!success)
            {
                clearProjectSelection((ListView)radioButton.getParent().getParent());
            }

        }
    }

    private void clearProjectSelection(ListView listView)
    {
        for (int count = 0; count < listView.getChildCount(); count++) {
            View childView = listView.getChildAt(count);
            RadioButton radioButton = (RadioButton) childView.findViewById(R.id.rBCurrentProject);
            radioButton.setChecked(false);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        selectProject.onClick(view);
        this.clearProjectSelection((ListView)parent);
        RadioButton button = (RadioButton)view.findViewById(R.id.rBCurrentProject);
        this.selectProject(button);

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
                            try {
                                File directory = Util.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
                                File file = Util.getGeoPackageByName(directory, context.getString(R.string.geopackage_extension), fileName);
                                ProjectDAO projectDAO = null;

                                projectDAO = new ProjectDAO(DatabaseFactory.getDatabase(context, ApplicationDatabase.DATABASE_NAME));

                                Project project = projectDAO.getByName(fileName);
                                if (project != null && file != null) {
                                    if (projectDAO.remove(project.getId())) {
                                        if (file.delete()) {
                                            if (((MainActivity) context).getMainController().getCurrentProject().toString().equals(projectName)) {
                                                try {
                                                    if (Util.getGeoPackageFiles(directory, context.getString(R.string.geopackage_extension)).size() > 0)
                                                        ((MainActivity) context).getMainController().setCurrentProject(projectDAO.getFirstProject());
                                                    else {
                                                        ((MainActivity) context).getMainController().setCurrentProject(null);
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

                            } catch (InvalidAppConfigException e) {
                                Message.showErrorMessage((MainActivity)context, R.string.error, e.getMessage());
                            } catch (DAOException e) {
                                Message.showErrorMessage((MainActivity)context, R.string.error, e.getMessage());
                            }
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