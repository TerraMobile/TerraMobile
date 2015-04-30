package br.org.funcate.terramobile.view;

/**
 * Created by Andre Carvalho on 30/04/15.
 */
public class TerraMobileMenuToolItem extends TerraMobileMenuItem {

    public static final int DOWNLOAD_GPKG=0;
    public static final int CREATE_GPKG=1;
    public static final int READ_GEOM=2;
    public static final int INSERT_DATA=3;

    public TerraMobileMenuToolItem(String label) {
        this.setLabel(label);
        this.setType(TOOL_ITEM);
    }
}
