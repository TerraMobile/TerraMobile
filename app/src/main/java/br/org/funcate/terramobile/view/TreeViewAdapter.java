package br.org.funcate.terramobile.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.configuration.ViewContextParameters;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.controller.activity.MenuMapController;
import br.org.funcate.terramobile.controller.activity.TreeView;
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.util.Message;

public class TreeViewAdapter extends BaseExpandableListAdapter implements View.OnClickListener {

    public ArrayList<GpkgLayer> groupItem;
    public ArrayList<ArrayList<GpkgLayer>> ChildItem;
    private final Context context;
    private MenuMapController menuMapController;
    private ArrayList<RadioButton> baseLayerRBList;
    private ArrayList<RadioButton> editableLayerRBList;

    public TreeViewAdapter(Context context, ArrayList<GpkgLayer> grpList, ArrayList<ArrayList<GpkgLayer>> childItem) {
        this.context = context;
        this.groupItem = grpList;
        this.ChildItem = childItem;
        this.menuMapController = new MenuMapController(this.context);
        baseLayerRBList = new ArrayList<RadioButton>();
        editableLayerRBList = new ArrayList<RadioButton>();
    }

    @Override
    public void onClick(View v) {
        exec(v);
        return;
    }

    private void exec(View v) {
        GpkgLayer child = (GpkgLayer) v.getTag();
        try{
            switch (child.getType()){
                case TILES:{// base
                    for (int count = 0; count < baseLayerRBList.size(); count++) {
                        RadioButton radioButton = baseLayerRBList.get(count);
                        radioButton.setChecked(false);
                    }
                    RadioButton rBChildBaseLayer = (RadioButton) v;
                    if (!rBChildBaseLayer.isChecked()) {
                        if(menuMapController.getBaseLayer() != null)
                            this.menuMapController.removeBaseLayer();
                        this.menuMapController.addBaseLayer(child);
                        rBChildBaseLayer.setChecked(true);
                    }
                    break;
                }
                case FEATURES:{// collect

                    break;
                }
                case EDITABLE:{// editable
                    TreeView treeView=((MainActivity) this.context).getTreeView();
                    treeView.setSelectedEditableLayer(child);
                    for (int count = 0; count < editableLayerRBList.size(); count++) {
                        RadioButton radioButton = editableLayerRBList.get(count);
                        radioButton.setChecked(false);
                    }
                    RadioButton rBChildGatheringLayer = (RadioButton) v;
                    if (!rBChildGatheringLayer.isChecked()) {
//                        menuMapController.removeEditableLayer();
//                        menuMapController.addEditableLayer(child);
                        rBChildGatheringLayer.setChecked(true);
                    }
                    break;
                }
                case ONLINE:{// online

                    break;
                }
            }
        } catch (Exception e) {
            Message.showErrorMessage(((MainActivity)context), R.string.error,"Failed on change the layer");
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        try {
            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.group_item_layers, null);
            }
            convertView.setClickable(false);

            TextView grpLabel = (TextView)convertView.findViewById(R.id.tVGrpLayer);
            grpLabel.setText(groupItem.get(groupPosition).getName());
            grpLabel.setTextColor(Color.BLACK);
            grpLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.grp_text_size));
            convertView.setTag(groupItem.get(groupPosition));
        }catch (Exception e){
            String s= e.getMessage();
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ArrayList<GpkgLayer> children = ChildItem.get(groupPosition);
        final GpkgLayer child = children.get(childPosition);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView childLabel;
        CheckBox checkBox;
        RadioButton radioButton;
        switch (child.getType()) {
            case TILES:
                convertView = layoutInflater.inflate(R.layout.child_item_base_layers, null);
                radioButton = (RadioButton)convertView.findViewById(R.id.rBChildBaseLayer);
                radioButton.setTextColor(Color.BLACK);
                radioButton.setText(child.getName());
                radioButton.setTag(child);
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        context.getResources().getDimension(R.dimen.child_text_size));
                radioButton.setOnClickListener(this);
                baseLayerRBList.add(radioButton);
                break;
            case FEATURES:
                convertView = layoutInflater.inflate(R.layout.child_item_layers, null);
                checkBox = (CheckBox)convertView.findViewById(R.id.cBChildLayer);
                checkBox.setTextColor(Color.BLACK);
                checkBox.setText(child.getName());
                checkBox.setTag(child);
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        context.getResources().getDimension(R.dimen.child_text_size));
                checkBox.setOnClickListener(this);
                break;
            case EDITABLE:
                convertView = layoutInflater.inflate(R.layout.child_item_gathering_layers, null);
                radioButton = (RadioButton)convertView.findViewById(R.id.rBChildGatheringLayer);
                radioButton.setTextColor(Color.BLACK);
                radioButton.setText(child.getName());
                radioButton.setTag(child);
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        context.getResources().getDimension(R.dimen.child_text_size));
                radioButton.setOnClickListener(this);
                editableLayerRBList.add(radioButton);
                break;
            case INVALID:
                convertView = layoutInflater.inflate(R.layout.child_item_invalid_layers, null);
                childLabel = (TextView)convertView.findViewById(R.id.tVChildInvalidLayer);
                childLabel.setTextColor(Color.BLACK);
                childLabel.setText(child.getName());
                childLabel.setTag(child);
                childLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        context.getResources().getDimension(R.dimen.child_text_size));
                break;
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (ChildItem.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return groupItem.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
/*        String s=this.context.getString(R.string.app_name);
        ((MainActivity) this.context).setTitle(s.subSequence(0,s.length()));*/
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
/*        String s=groupItem.get(groupPosition);
        ((MainActivity) this.context).setTitle(s.subSequence(0,s.length()));*/
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}