package br.org.funcate.terramobile.view;

/**
 * Created by Andre Carvalho on 30/04/15.
 */
public class TerraMobileMenuToolItem extends TerraMobileMenuItem {

    // this values should be the same order on string.xml
    public static final int DOWNLOAD_GPKG=0;
    public static final int CREATE_GPKG=1;
    public static final int READ_GEOM=2;
    public static final int INSERT_DATA=3;
    public static final int BTN_TEST=4;

    private int toolID;

    public TerraMobileMenuToolItem(String label,int tool) {
        this.setLabel(label);
        this.setType(TOOL_ITEM);
        this.toolID=tool;
    }

    public int getToolID() {
        return toolID;
    }

    public void setToolID(int toolID) {
        this.toolID = toolID;
    }
}
