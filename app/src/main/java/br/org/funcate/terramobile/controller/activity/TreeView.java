package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.widget.ExpandableListView;
import android.widget.Toast;

import android.content.res.Resources;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.util.ResourceUtil;
import br.org.funcate.terramobile.view.MenuAdapter;

/**
 * Created by Andre Carvalho on 28/04/15.
 */
public class TreeView {

    private ExpandableListView mDrawerList;
    private ArrayList<String> groupItem = new ArrayList<String>();
    private ArrayList<Object> childItem = new ArrayList<Object>();
    private Context context;
    private Resources resources;

    public TreeView(Context context){
        this.context=context;
        this.resources=context.getResources();
        setGroupData();
        setChildGroupData();
        initTreeView();
    }

    private void initTreeView() {

        try {
            mDrawerList = (ExpandableListView) ((MainActivity) this.context).findViewById(R.id.expandable_tree_view);
        }catch (Exception e){
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if(mDrawerList==null) return;

        MenuAdapter mMenuAdapter = new MenuAdapter(this.context, groupItem, childItem);
        mDrawerList.setAdapter(mMenuAdapter);

    }

    private void setGroupData() {
        String[] grp= ResourceUtil.getStringArrayResource(this.resources, R.array.menu_groups);
        int l=grp.length;
        for (int i = 0; i < l; i++) {
            groupItem.add(grp[i]);
        }
    }

    private void setChildGroupData() {
        ArrayList<String> child = new ArrayList<String>();
        /**
         * Add menu items from strings resource file.
         */
        String[] items=ResourceUtil.getStringArrayResource(this.resources,R.array.menu_items);
        int l=items.length;
        for (int i = 0; i < l; i++) {
            child.add(items[i]);
        }
        childItem.add(child);

        /**
         * Insert items into menu group loaded from database configuration in run time.
         */
        child = new ArrayList<String>();
        child.add("Base layer");
        child.add("Collect layer");
        child.add("Online layer");
        childItem.add(child);
    }

    public ExpandableListView getUIComponent(){
        return mDrawerList;
    }
}
