package br.org.funcate.terramobile.model.gpkg.objects;

import com.augtech.geoapi.geopackage.GeoPackage;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
public class GpkgLayer{

    private String layerName;
    private AppLayer layerType;
    private GeoPackage geoPackage;
    private int indexOverlay;

    public GpkgLayer() {
    }

    public GpkgLayer(GeoPackage geoPackage) {
        this.geoPackage=geoPackage;
    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public AppLayer getLayerType() {
        return layerType;
    }

    public void setLayerType(AppLayer layerType) {
        this.layerType = layerType;
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
}
