package br.org.funcate.terramobile.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

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
        convertView.setTag(layer);

        return convertView;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        CheckBox checkBox = (CheckBox)view.findViewById(R.id.cbUploadLayer);
        checkBox.setChecked(true);

    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
      return false;
    }


}