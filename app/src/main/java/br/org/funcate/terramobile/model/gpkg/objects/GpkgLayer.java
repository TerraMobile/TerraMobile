package br.org.funcate.terramobile.model.gpkg.objects;

import com.augtech.geoapi.geopackage.GeoPackage;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
public class GpkgLayer implements AppLayer {

    private String layerName;
    private int layerType;
    private GeoPackage geoPackage;

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

    public int getLayerType() {
        return layerType;
    }

    public void setLayerType(int layerType) {
        this.layerType = layerType;
    }

    public GeoPackage getGeoPackage() {
        return geoPackage;
    }

    public void setGeoPackage(GeoPackage geoPackage) {
        this.geoPackage = geoPackage;
    }
}
