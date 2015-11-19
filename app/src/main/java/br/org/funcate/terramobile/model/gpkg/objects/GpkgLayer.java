package br.org.funcate.terramobile.model.gpkg.objects;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgField;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.overlay.Overlay;

import java.util.ArrayList;

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

    public GpkgLayer() {
    }

    public GpkgLayer(GeoPackage geoPackage) {
        this.geoPackage=geoPackage;
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
}
