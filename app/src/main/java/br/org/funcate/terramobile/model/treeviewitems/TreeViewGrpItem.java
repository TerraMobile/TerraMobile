package br.org.funcate.terramobile.model.treeviewitems;

import br.org.funcate.terramobile.model.gpkg.objects.AppLayer;

/**
 * Created by marcelo on 5/21/15.
 */
public class TreeViewGrpItem extends TreeViewItem {
    private String name;
    private AppLayer type;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}