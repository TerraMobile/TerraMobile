package br.org.funcate.terramobile.view;

import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by Andre Carvalho on 30/04/15.
 */
public class TerraMobileMenuLayerItem extends TerraMobileMenuItem {

    private GpkgLayer layer;

    public TerraMobileMenuLayerItem(GpkgLayer l) {
        this.layer=l;
        this.setLabel(l.getLayerName());
        this.setType(LAYER_ITEM);
    }

    public GpkgLayer getLayer() {
        return layer;
    }

    public void setLayer(GpkgLayer layer) {
        this.layer = layer;
    }
}

