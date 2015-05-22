package br.org.funcate.terramobile.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

@SuppressWarnings("unchecked")
public class MenuAdapter extends BaseExpandableListAdapter {

	public ArrayList<GpkgLayer> groupItem;
	public ArrayList<ArrayList<GpkgLayer>> ChildItem;
	private final Context context;

	public MenuAdapter(Context context, ArrayList<GpkgLayer> grpList, ArrayList<ArrayList<GpkgLayer>> childItem) {
		this.context = context;
		this.groupItem = grpList;
		this.ChildItem = childItem;
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
            grpLabel.setText(groupItem.get(groupPosition).getLayerName());
            grpLabel.setTextColor(Color.WHITE);
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
                             boolean isLastChild, View convertView, final ViewGroup parent) {

        ArrayList<GpkgLayer> children = ChildItem.get(groupPosition);
        final GpkgLayer child = children.get(childPosition);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView childLabel;
        if (convertView == null) {
            switch (child.getLayerType()) {
                case TILES:
                    convertView = layoutInflater.inflate(R.layout.child_item_base_layers, null);
                    childLabel = (TextView)convertView.findViewById(R.id.tVChildBaseLayer);
                    childLabel.setTextColor(Color.WHITE);
                    childLabel.setText(child.getLayerName());
                    childLabel.setTag(child);
                    childLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            context.getResources().getDimension(R.dimen.child_text_size));
                    break;
                case EDITABLE:
                    convertView = layoutInflater.inflate(R.layout.child_item_overlapping_layers, null);
                    childLabel = (TextView)convertView.findViewById(R.id.tVChildOverlappingLayer);
                    childLabel.setTextColor(Color.WHITE);
                    childLabel.setText(child.getLayerName());
                    childLabel.setTag(child);
                    childLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            context.getResources().getDimension(R.dimen.child_text_size));
                    break;
                case FEATURES:
                    convertView = layoutInflater.inflate(R.layout.child_item_acquisition_layers, null);
                    childLabel = (TextView)convertView.findViewById(R.id.tVChildAcquisitionLayer);
                    childLabel.setTextColor(Color.WHITE);
                    childLabel.setText(child.getLayerName());
                    childLabel.setTag(child);
                    childLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            context.getResources().getDimension(R.dimen.child_text_size));
                    break;
                case INVALID:
                    convertView = layoutInflater.inflate(R.layout.child_item_invalid_layers, null);
                    childLabel = (TextView)convertView.findViewById(R.id.tVChildInvalidLayer);
                    childLabel.setTextColor(Color.WHITE);
                    childLabel.setText(child.getLayerName());
                    childLabel.setTag(child);
                    childLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            context.getResources().getDimension(R.dimen.child_text_size));
                    break;
            }
        }

//        try {
//            switch (child.getType()) {
//                case TerraMobileMenuItem.TOOL_ITEM: {// Tools
//                    text.setOnClickListener(new MenuToolController(this.context, (TerraMobileMenuToolItem) child));
//                    break;
//                }
//                case TerraMobileMenuItem.LAYER_ITEM: {// Layers
//                    text.setOnClickListener(new MenuMapController(context, (TerraMobileMenuLayerItem) child));
//                    break;
//                }
//            }
//        }catch (Exception e){
//            String l=e.getMessage();
//        }

        return convertView;
    }

    @Override
	public int getChildrenCount(int groupPosition) {
		return ((ArrayList<GpkgLayer>) ChildItem.get(groupPosition)).size();
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
