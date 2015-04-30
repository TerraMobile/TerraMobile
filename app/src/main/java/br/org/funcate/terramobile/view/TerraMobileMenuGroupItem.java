package br.org.funcate.terramobile.view;

/**
 * Created by Andre Carvalho on 30/04/15.
 */
public class TerraMobileMenuGroupItem extends TerraMobileMenuItem {

    public TerraMobileMenuGroupItem(String label) {
        this.setLabel(label);
        this.setType(GROUP_ITEM);
    }
}
