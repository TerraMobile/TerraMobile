package br.org.funcate.terramobile.configuration;

import java.util.ArrayList;

/**
 * Created by Andre Carvalho on 17/04/15.
 */
public class ViewContextParameters {

    private ArrayList<String> selectedLayers;

    public ViewContextParameters(){
        selectedLayers=new ArrayList<String>();
    }

    public void setSelectedLayers(ArrayList<String> selectedLayers) {

        this.selectedLayers = selectedLayers;
    }

    public ArrayList<String> getSelectedLayers() {
        return selectedLayers;
    }

    public boolean addLayerName(String layerName){
        return this.selectedLayers.add(layerName);
    }

    public boolean removeLayerName(String layerName){
        if(!this.selectedLayers.isEmpty())
            return this.selectedLayers.remove(layerName);
        return false;
    }
}
