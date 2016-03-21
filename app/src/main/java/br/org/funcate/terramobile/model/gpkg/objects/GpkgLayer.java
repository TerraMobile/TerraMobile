package br.org.funcate.terramobile.model.gpkg.objects;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;
import java.util.Iterator;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.util.ResourceHelper;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
public class GpkgLayer{

    public enum Type {
        // Editable: layer
        // Tiles: base layer
        // Features: gathering layer
        FEATURES, TILES, EDITABLE, ONLINE, INVALID
    }

    private String name;
    private Type type;
    private GeoPackage geoPackage;
    private BoundingBox box;
    private Integer srsId;
    private String JSON;
    private ArrayList<GpkgField> fields;
    private SimpleFeatureType featureType;
    private Overlay osmOverLayer;
    private String mediaTable;// the name of the media table to store medias outside of the SFS spec.
    private Style style;
    private boolean enabled;
    private int position;
    private boolean modified;

    public GpkgLayer() {
    }

    public GpkgLayer(GeoPackage geoPackage) {
        this.geoPackage=geoPackage;
    }

    /**
     * Build a filter that select all features that state is not removed.
     * It is used on normal operations.
     * See the file "gatheringconfig.xml" to more info.
     * @return a where clause based on gathering configuration to all editable layers or empty to others.
     */
    public String defaultFilter() {

        String filter="";
        if(this.isEditable()){
            String statusKey;
            int statusValue;
            try {
                statusKey = ResourceHelper.getStringResource(R.string.point_status_column);
                statusValue = ResourceHelper.getIntResource(R.integer.point_status_removed);
            } catch (InvalidAppConfigException e) {
                e.printStackTrace();
                statusKey="tm_status";
                statusValue=2;
            }
            GpkgField gpkgField = getAttributeByName(statusKey);
            if(gpkgField!=null) {
                filter=statusKey + "!=" + statusValue;
            }
        }
        return filter;
    }

    /**
     * Build a filter that select all features that state is removed.
     * It is used on remove operation.
     * See the file "gatheringconfig.xml" to more info.
     * @return a where clause based on gathering configuration to all editable layers or empty to others.
     */
    public String toRemoveFilter() {

        String filter="";
        if(this.isEditable()){
            String statusKey;
            int statusValue;
            try {
                statusKey = ResourceHelper.getStringResource(R.string.point_status_column);
                statusValue = ResourceHelper.getIntResource(R.integer.point_status_removed);
            } catch (InvalidAppConfigException e) {
                e.printStackTrace();
                statusKey="tm_status";
                statusValue=2;
            }
            GpkgField gpkgField = getAttributeByName(statusKey);
            if(gpkgField!=null) {
                filter=statusKey + "=" + statusValue;
            }
        }
        return filter;
    }

    /**
     * Build a statement used to write a SQL to update the feature state as send.
     * It is used after build a Geopackage to send to server.
     * See the file "gatheringconfig.xml" to more info.
     * @return a set clause based on gathering configuration to all editable layers or empty to others.
     */
    public String statementToSetSend() {

        String stmt="";
        if(this.isEditable()){
            String statusKey;
            int statusValue;
            try {
                statusKey = ResourceHelper.getStringResource(R.string.point_status_column);
                statusValue = ResourceHelper.getIntResource(R.integer.point_status_send);
            } catch (InvalidAppConfigException e) {
                e.printStackTrace();
                statusKey="tm_status";
                statusValue=3;
            }
            GpkgField gpkgField = getAttributeByName(statusKey);
            if(gpkgField!=null) {
                stmt=statusKey + "=" + statusValue;
            }
        }
        return stmt;
    }

    /**
     * This filters get all features to send: created, changed and removed.
     * This exclude unchanged and send features.
     * It is used on select feature to upload data.
     * See the file "gatheringconfig.xml" to more info.
     * @return a where clause based on gathering configuration to all editable layers or empty to others.
     */
    public String toSendFilter() {
        String filter="";
        if(this.isEditable()){
            String statusKey;
            String objectIdKey;
            int statusChanged;
            int statusRemoved;
            int statusSend;
            try {
                statusKey = ResourceHelper.getStringResource(R.string.point_status_column);
                objectIdKey = ResourceHelper.getStringResource(R.string.point_obj_id_column);
                statusChanged = ResourceHelper.getIntResource(R.integer.point_status_changed);
                statusRemoved = ResourceHelper.getIntResource(R.integer.point_status_removed);
                statusSend = ResourceHelper.getIntResource(R.integer.point_status_send);
            } catch (InvalidAppConfigException e) {
                e.printStackTrace();
                statusKey="tm_status";
                objectIdKey="object_id";
                statusChanged=1;
                statusRemoved=2;
                statusSend=3;
            }
            GpkgField gpkgFieldStatus = getAttributeByName(statusKey);
            GpkgField gpkgFieldObjId = getAttributeByName(objectIdKey);
            if(gpkgFieldStatus!=null && gpkgFieldObjId!=null) {
                filter="("+objectIdKey+" is null AND "+statusKey+"!="+statusSend+") OR ("+objectIdKey+" is not null AND "+
                        statusKey+" between "+statusChanged+" and "+statusRemoved+")";
            }
        }
        return filter;
    }

    public String toUnchangeFilter() {
        String filter="";
        if(this.isEditable()){
            String statusKey;
            String objectIdKey;
            int statusUnchanged;
            try {
                statusKey = ResourceHelper.getStringResource(R.string.point_status_column);
                objectIdKey = ResourceHelper.getStringResource(R.string.point_obj_id_column);
                statusUnchanged = ResourceHelper.getIntResource(R.integer.point_status_unchanged);
            } catch (InvalidAppConfigException e) {
                e.printStackTrace();
                statusKey="tm_status";
                objectIdKey="object_id";
                statusUnchanged=1;
            }
            GpkgField gpkgFieldStatus = getAttributeByName(statusKey);
            GpkgField gpkgFieldObjId = getAttributeByName(objectIdKey);
            if(gpkgFieldStatus!=null && gpkgFieldObjId!=null) {
                filter=objectIdKey+" is not null AND "+statusKey+" = "+statusUnchanged;
            }
        }
        return filter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isEditable() {
        return Type.EDITABLE.equals(this.type);
    }

    public GeoPackage getGeoPackage() {
        return geoPackage;
    }

    public void setGeoPackage(GeoPackage geoPackage) {
        this.geoPackage = geoPackage;
    }

    public BoundingBox getBox()
    {
        return box;
    }

    public void setBox(BoundingBox box) {
        this.box = box;
    }

    public Integer getSrsId() {
        return srsId;
    }

    public void setSrsId(Integer srsId) {
        this.srsId = srsId;
    }

    public String getJSON() {
        return JSON;
    }

    public void setJSON(String JSON) {
        this.JSON = JSON;
    }

    public ArrayList<GpkgField> getFields() {
        return fields;
    }

    public void setFields(ArrayList<GpkgField> fields) {
        this.fields = fields;
    }

    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    public void setFeatureType(SimpleFeatureType featureType) {
        this.featureType = featureType;
    }

    public Overlay getOsmOverLayer() {
        return osmOverLayer;
    }

    public void setOsmOverLayer(Overlay osmOverLayer) {
        this.osmOverLayer = osmOverLayer;
    }

    public String getMediaTable() {
        return mediaTable;
    }

    public void setMediaTable(String mediaTable) {
        this.mediaTable = mediaTable;
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private GpkgField getAttributeByName(String name) {

        Iterator<GpkgField> it = fields.iterator();
        GpkgField gpkgField=null;
        while(it.hasNext()) {
            gpkgField = it.next();
            if(gpkgField.getFieldName().equals(name)) {
                break;
            }
        }
        return gpkgField;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
