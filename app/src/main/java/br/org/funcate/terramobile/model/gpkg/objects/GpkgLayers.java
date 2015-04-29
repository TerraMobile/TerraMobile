package br.org.funcate.terramobile.model.gpkg.objects;

/**
 * Created by Andre Carvalho on 29/04/15.
 */
public class GpkgLayers {

    private String layerName;
    private String layerType;

    public GpkgLayers() {

    }

    public String getLayerName() {
        return layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public String getLayerType() {
        return layerType;
    }

    public void setLayerType(String layerType) {
        this.layerType = layerType;
    }
}
