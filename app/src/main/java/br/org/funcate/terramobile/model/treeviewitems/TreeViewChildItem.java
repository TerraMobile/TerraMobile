package br.org.funcate.terramobile.model.treeviewitems;

import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by marcelo on 5/21/15.
 */
public class TreeViewChildItem extends TreeViewItem{
    private GpkgLayer layer;

    public GpkgLayer getLayer() {
        return layer;
    }

    public void setLayer(GpkgLayer layer) {
        this.layer = layer;
    }
}