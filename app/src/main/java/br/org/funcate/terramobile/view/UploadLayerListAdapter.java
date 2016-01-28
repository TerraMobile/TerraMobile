package br.org.funcate.terramobile.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.Util;

/**
 * Created by marcelo on 6/10/15.
 */
public class UploadLayerListAdapter extends ArrayAdapter<GpkgLayer> implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
    private Context context;

    private ArrayList layersList;

    public UploadLayerListAdapter(Context context, int textViewResourceId, ArrayList<GpkgLayer> layersList) {
        super(context, textViewResourceId, layersList);
        this.context = context;
        this.layersList = layersList;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.upload_layer_item, null);

        ((ListView)parent).setOnItemClickListener(this);
        ((ListView)parent).setOnItemLongClickListener(this);

        GpkgLayer layer = (GpkgLayer) layersList.get(position);

        TextView tvUploadLayerName = (TextView) convertView.findViewById(R.id.upLoadLayerName);
        CheckBox cbUploadLayer = (CheckBox) convertView.findViewById(R.id.cbUploadLayer);

        tvUploadLayerName.setText(layer.getName());
        cbUploadLayer.setTag(layer);

        return convertView;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        selectProject.onClick(view);
/*
        this.clearProjectSelection((ListView)parent);
        RadioButton button = (RadioButton)view.findViewById(R.id.rBCurrentProject);
        this.selectProject(button);
*/

        CheckBox checkBox = (CheckBox)view.findViewById(R.id.cbUploadLayer);
        checkBox.setEnabled(true);

    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
      return false;
    }

}