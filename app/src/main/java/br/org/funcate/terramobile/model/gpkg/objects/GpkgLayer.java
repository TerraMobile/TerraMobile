package br.org.funcate.terramobile.model.gpkg.objects;

import com.augtech.geoapi.geopackage.GeoPackage;

import org.osmdroid.util.BoundingBoxE6;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
public class GpkgLayer{

    public enum Type {
        // Editable: layer
        // TilES: base layer
        // Features: gathering layer
        FEATURES, TILES, EDITABLE, ONLINE, INVALID
    }

    private String name;
    private Type type;
    private GeoPackage geoPackage;
    private int indexOverlay;
    private BoundingBoxE6 box;
    private Integer srsId;

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

    public int getIndexOverlay() {
        return indexOverlay;
    }

    public void setIndexOverlay(int indexOverlay) {
        this.indexOverlay = indexOverlay;
    }

    public BoundingBoxE6 getBox()
    {
        return box;
    }

    public void setBox(BoundingBoxE6 box) {
        this.box = box;
    }

    public Integer getSrsId() {
        return srsId;
    }

    public void setSrsId(Integer srsId) {
        this.srsId = srsId;
    }
}
