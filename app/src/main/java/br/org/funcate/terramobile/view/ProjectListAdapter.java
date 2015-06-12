package br.org.funcate.terramobile.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.util.ResourceUtil;

/**
 * Created by marcelo on 6/10/15.
 */
public class ProjectListAdapter extends ArrayAdapter<String> {
    private Context context;

    private ArrayList projectList;

    public ProjectListAdapter(Context context, int textViewResourceId, ArrayList<String> projectList) {
        super(context, textViewResourceId, projectList);
        this.context = context;
        this.projectList = projectList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.project_item, null);
        TextView tVProject = (TextView) convertView.findViewById(R.id.tVProjectName);
        ImageView iVDownloaded = (ImageView)convertView.findViewById(R.id.iVDownloaded);
//        ImageView iVUpdated = (ImageView)convertView.findViewById(R.id.iVUpdated);

        String fileName = projectList.get(position).toString();
        String projectName = fileName.substring(0, fileName.indexOf('.'));
        tVProject.setText(projectName);

        File directory = ResourceUtil.getDirectory(context.getResources().getString(R.string.app_workspace_dir));
        if(ResourceUtil.getGeoPackageByName(directory, context.getResources().getString(R.string.geopackage_extension), fileName) != null)
            iVDownloaded.setImageResource(android.R.drawable.ic_popup_disk_full);

        return convertView;
    }
}
