package br.org.funcate.terramobile.view;

/**
 * Created by Andre Carvalho on 30/04/15.
 */
public class TerraMobileMenuItem implements TerraMobileMenu {

    private String label="";
    private int type;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
