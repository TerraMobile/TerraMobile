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
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.controller.activity.MenuMapController;
import br.org.funcate.terramobile.controller.activity.MenuToolController;

@SuppressWarnings("unchecked")
public class MenuAdapter extends BaseExpandableListAdapter {

	public ArrayList<String> groupItem;
	public ArrayList<Object> ChildItem = new ArrayList<Object>();
	private final Context context;

	public MenuAdapter(Context context, ArrayList<String> grList, ArrayList<Object> childItem) {
		this.context = context;
		this.groupItem = grList;
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

        if (convertView == null) {
            convertView = new TextView(context);
        }
        convertView.setClickable(false);

        TextView text = null;
        text = (TextView) convertView;
        text.setText(groupItem.get(groupPosition));
        text.setTextColor(Color.WHITE);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.grptextsize));
        convertView.setTag(groupItem.get(groupPosition));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, final ViewGroup parent) {

        TextView text = null;
        if (convertView == null) {
            convertView = new TextView(context);
        }
        text = (TextView) convertView;
        ArrayList<String> children = (ArrayList<String>) ChildItem.get(groupPosition);
        final String child = children.get(childPosition);
        text.setTextColor(Color.WHITE);
        text.setText("  "+child);
        text.setTag(child);
        text.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimension(R.dimen.childtextsize));

        switch (groupPosition){
            case 0:{// Tools
                text.setOnClickListener(new MenuToolController(context, childPosition));
                break;
            }
            case 1:{// Layers
                text.setOnClickListener(new MenuMapController(context, childPosition));
                break;
            }
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

        String s=this.context.getString(R.string.app_name);
        ((MainActivity) this.context).setTitle(s.subSequence(0,s.length()));
        super.onGroupCollapsed(groupPosition);
	}

	@Override
	public void onGroupExpanded(int groupPosition) {

        String s=groupItem.get(groupPosition);
        ((MainActivity) this.context).setTitle(s.subSequence(0,s.length()));
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
