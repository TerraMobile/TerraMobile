package br.org.funcate.terramobile.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.util.ResourceUtil;

/**
 * Created by marcelo on 6/10/15.
 */
public class ProjectListAdapter extends ArrayAdapter<Project> {
    private Context context;

    private ArrayList projectList;

    public ProjectListAdapter(Context context, int textViewResourceId, ArrayList<Project> projectList) {
        super(context, textViewResourceId, projectList);
        this.context = context;
        this.projectList = projectList;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.project_item, null);
        TextView tVProject = (TextView) convertView.findViewById(R.id.tVProjectName);
        ImageView iVDownloaded = (ImageView) convertView.findViewById(R.id.iVDownloaded);
//        ImageView iVUpdated = (ImageView)convertView.findViewById(R.id.iVUpdated);

        Project project = (Project) projectList.get(position);
        final String projectName = project.toString().endsWith(context.getString(R.string.geopackage_extension)) ? project.toString().substring(0, project.toString().indexOf('.')) : project.toString();

        tVProject.setText(projectName);

        RadioButton rBCurrentProject = (RadioButton) convertView.findViewById(R.id.rBCurrentProject);
        rBCurrentProject.setTag(project);

        Project currentProject = ((MainActivity) context).getProject();
        if (currentProject != null && currentProject.toString().equals(projectName)) {
            rBCurrentProject.setChecked(true);
            rBCurrentProject.setEnabled(true);
        }

        File directory = ResourceUtil.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        if (ResourceUtil.getGeoPackageByName(directory, context.getResources().getString(R.string.geopackage_extension), project.toString()) != null) {
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
                    newCurrentProject.setName(projectName);
                    ((MainActivity) context).setProject(newCurrentProject);
                }
            }
        });

        return convertView;
    }
}
