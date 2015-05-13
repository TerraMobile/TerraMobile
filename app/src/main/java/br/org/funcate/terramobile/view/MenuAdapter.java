package br.org.funcate.terramobile.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MenuMapController;
import br.org.funcate.terramobile.controller.activity.MenuToolController;

@SuppressWarnings("unchecked")
public class MenuAdapter extends BaseExpandableListAdapter {

	public ArrayList<TerraMobileMenuGroupItem> groupItem;
	public ArrayList<Object> ChildItem = new ArrayList<Object>();
	private final Context context;

	public MenuAdapter(Context context, ArrayList<TerraMobileMenuGroupItem> grpList, ArrayList<Object> childItem) {
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
                convertView = new TextView(context);
            }
            convertView.setClickable(false);

            TextView text;
            text = (TextView) convertView;
            text.setText(groupItem.get(groupPosition).getLabel());
            text.setTextColor(Color.WHITE);
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
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

        TextView text;
        if (convertView == null) {
            convertView = new TextView(context);
        }
        text = (TextView) convertView;
        ArrayList<TerraMobileMenuItem> children = (ArrayList<TerraMobileMenuItem>) ChildItem.get(groupPosition);
        final TerraMobileMenuItem child = children.get(childPosition);
        text.setTextColor(Color.WHITE);
        text.setText("  " + child.getLabel());
        text.setTag(child);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.child_text_size));
        try {
            switch (child.getType()) {
                case TerraMobileMenuItem.TOOL_ITEM: {// Tools
                    text.setOnClickListener(new MenuToolController(context, (TerraMobileMenuToolItem) child));
                    break;
                }
                case TerraMobileMenuItem.LAYER_ITEM: {// Layers
                    text.setOnClickListener(new MenuMapController(context, (TerraMobileMenuLayerItem) child));
                    break;
                }
            }
        }catch (Exception e){
            String l=e.getMessage();
        }

        return convertView;
    }

    @Override
	public int getChildrenCount(int groupPosition) {
		return ((ArrayList<String>) ChildItem.get(groupPosition)).size();
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
