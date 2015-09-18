package br.org.funcate.terramobile.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.controller.activity.MenuMapController;
import br.org.funcate.terramobile.controller.activity.TreeViewController;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.LayersService;
import br.org.funcate.terramobile.util.Message;

public class TreeViewAdapter extends BaseExpandableListAdapter implements View.OnClickListener {

    public ArrayList<GpkgLayer> groupItem;
    public ArrayList<ArrayList<GpkgLayer>> childItem;
    private final Context context;
    private MenuMapController menuMapController;
    private ArrayList<RadioButton> editableLayerRBList;

    public TreeViewAdapter(Context context, ArrayList<GpkgLayer> grpList, ArrayList<ArrayList<GpkgLayer>> childItem) {
        this.context = context;
        this.groupItem = grpList;
        this.childItem = childItem;
        this.menuMapController = ((MainActivity) context).getMainController().getMenuMapController();
        editableLayerRBList = new ArrayList<RadioButton>();
    }

    /**
     * Call when one radio button or checkbox is selected on list layer.
     * @param v, selected element radio or checkbox
     */
    @Override
    public void onClick(View v) {
        exec(v);
        return;
    }

    private void exec(View v) {
        GpkgLayer child = (GpkgLayer) v.getTag();
        try{
            switch (child.getType()) {
                case TILES:
                case FEATURES:{// vectors and images

                    if (((CheckBox) v).isChecked()) {
                        menuMapController.enableLayer(child);
                        child.setEnabled(true);
                    } else {
                        menuMapController.disableLayer(child);
                        child.setEnabled(false);
                    }
                    break;
                }
                case EDITABLE:{// editable (vector points)
                    this.unselectAllRadioButtons(editableLayerRBList);

                    TreeViewController treeView=((MainActivity) this.context).getMainController().getTreeViewController();
                    GpkgLayer ed = treeView.getSelectedEditableLayer();
                    if(ed!=null)
                        menuMapController.disableLayer(ed);
                    treeView.setSelectedEditableLayer(child);

                    RadioButton rBChildGatheringLayer = (RadioButton) v;
                    if (!rBChildGatheringLayer.isChecked()) {
                        menuMapController.enableLayer(child);
                        rBChildGatheringLayer.setChecked(true);
                        child.setEnabled(true);
                    }
                    break;
                }
                case ONLINE:{// online

                    break;
                }
            }

            MainActivity mainActivity = (MainActivity)context;
            boolean hasGPSLayer = mainActivity.getMainController().getGpsOverlayController().isOverlayAdded();
            // remove GPS Overlay
            if(hasGPSLayer) mainActivity.getMainController().getGpsOverlayController().removeGPSTrackerLayer();
            // re adding GPS Overlay
            if(hasGPSLayer) mainActivity.getMainController().getGpsOverlayController().addGPSTrackerLayer();


       }
        catch (LowMemoryException e) {
            e.printStackTrace();
            Message.showErrorMessage(((MainActivity) context), R.string.error, e.getMessage());
        }
        catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(((MainActivity) context), R.string.error, e.getMessage());
        }
        catch (TerraMobileException e) {
            e.printStackTrace();
            Message.showErrorMessage(((MainActivity) context), R.string.error, e.getMessage());
        } catch (StyleException e) {
            e.printStackTrace();
            Message.showErrorMessage(((MainActivity) context), R.string.error, e.getMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            Message.showErrorMessage(((MainActivity) context), R.string.error, context.getResources().getString(R.string.unexpected_exception));
        }
    }

    private void unselectAllRadioButtons(ArrayList<RadioButton> radioButtons) {
        for (int i = 0,len=radioButtons.size(); i < len; i++) {
            RadioButton radioButton = radioButtons.get(i);
            GpkgLayer child = (GpkgLayer) radioButton.getTag();
            radioButton.setChecked(false);
            child.setEnabled(false);
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
        }catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ArrayList<GpkgLayer> children = childItem.get(groupPosition);
        final GpkgLayer child = children.get(childPosition);

        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView childLabel;
        CheckBox checkBox;
        RadioButton radioButton;
        ImageView extentImage;
        ImageView layerUpImage;
        ImageView layerDownImage;
        switch (child.getType()) {
            case TILES:
            case FEATURES:
                convertView = layoutInflater.inflate(R.layout.child_item_layers, null);
                checkBox = (CheckBox)convertView.findViewById(R.id.cBChildLayer);
                checkBox.setTextColor(Color.BLACK);
                checkBox.setText(child.getName());
                checkBox.setTag(child);
                checkBox.setChecked(child.isEnabled());
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
                radioButton.setChecked(child.isEnabled());
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

        //Adding the listener to the zoom to extent image
        extentImage = (ImageView)convertView.findViewById(R.id.zoomExtent);
        if(extentImage!=null)
        {
            extentImage.setOnClickListener(extentImageListener);
            extentImage.setTag(child);
        }

        //Adding the listener to the layer up button
        layerUpImage = (ImageView)convertView.findViewById(R.id.layerUp);
        if(layerUpImage!=null)
        {
            layerUpImage.setOnClickListener(layerUpImageListener);
            layerUpImage.setTag(child);
        }

        //Adding the listener to the layer down button
        layerDownImage = (ImageView)convertView.findViewById(R.id.layerDown);
        if(layerDownImage!=null)
        {
            layerDownImage.setOnClickListener(layerDownImageListener);
            layerDownImage.setTag(child);
        }

        return convertView;
    }

    private View.OnClickListener extentImageListener = new View.OnClickListener() {
        //@Override
        public void onClick(View v) {
        if(v.getTag()!=null)
        {
            if(v.getTag() instanceof GpkgLayer)
            {
                GpkgLayer layer = (GpkgLayer) v.getTag();
                zoomToLayerExtent(layer);
            }
        }
        }
    };

    private View.OnClickListener layerUpImageListener = new View.OnClickListener() {
        //@Override
        public void onClick(View v) {
            GpkgLayer layer = (GpkgLayer)v.getTag();
            for (int i = 0; i < childItem.size(); i++) {
                ArrayList<GpkgLayer> layers = childItem.get(i);
                for (int j = 0; j < layers.size(); j++) {
                    if(layers.get(j)==layer)
                    {
                        moveLayerUp(i,j);
                        break;
                    }
                }
           }
        }
    };

    private View.OnClickListener layerDownImageListener = new View.OnClickListener() {
        //@Override
        public void onClick(View v) {
            GpkgLayer layer = (GpkgLayer)v.getTag();
            for (int i = 0; i < childItem.size(); i++) {
                ArrayList<GpkgLayer> layers = childItem.get(i);
                for (int j = 0; j < layers.size(); j++) {
                    if(layers.get(j)==layer)
                    {
                        moveLayerDown(i,j);
                        break;
                    }
                }
            }
        }
    };

    private void moveLayerUp(int currentGroupPos, int currentPos)
    {
        ArrayList<GpkgLayer> layers = childItem.get(currentGroupPos);

        if(currentPos!=0) //This condition keeps the layer inside his group
        {
            GpkgLayer currentLayer = layers.get(currentPos);
            GpkgLayer upLayer = layers.get(currentPos-1);

            //Changing GpkgLayer index overlay
            int currLayerPos = currentLayer.getIndexOverlay();
            currentLayer.setIndexOverlay(upLayer.getIndexOverlay());
            upLayer.setIndexOverlay(currLayerPos);

            //Correct the layer order by the GPKGLayer index.
            LayersService.sortLayersByIndex(layers);
            menuMapController.updateOverlaysOrder(LayersService.composeLinearLayerList(childItem));

            notifyDataSetChanged();
        }
    }

    private void moveLayerDown(int currentGroupPos, int currentPos)
    {
        ArrayList<GpkgLayer> layers = childItem.get(currentGroupPos);
        if(currentPos!=(layers.size()-1)) //This condition keeps the layer inside his group
        {
            GpkgLayer currentLayer = layers.get(currentPos);
            GpkgLayer downLayer = layers.get(currentPos+1);
/*            layers.set(currentPos, downLayer);
            layers.set(currentPos+1, currentLayer);*/

            //Changing GpkgLayer index overlay
            int currLayerPos = currentLayer.getIndexOverlay();
            currentLayer.setIndexOverlay(downLayer.getIndexOverlay());
            downLayer.setIndexOverlay(currLayerPos);

            //Correct the layer order by the GPKGLayer index.
            LayersService.sortLayersByIndex(layers);
            menuMapController.updateOverlaysOrder(LayersService.composeLinearLayerList(childItem));

            notifyDataSetChanged();
        }
    }

    private void zoomToLayerExtent(GpkgLayer layer)
    {
        this.menuMapController.panTo(layer.getBox());
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return (childItem.get(groupPosition)).size();
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