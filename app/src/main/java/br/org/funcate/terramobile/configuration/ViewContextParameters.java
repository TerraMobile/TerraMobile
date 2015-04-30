package br.org.funcate.terramobile.configuration;

import java.util.ArrayList;

import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by Andre Carvalho on 17/04/15.
 */
public class ViewContextParameters {

    private ArrayList<GpkgLayer> selectedLayers;

    public ViewContextParameters(){
        selectedLayers=new ArrayList<GpkgLayer>();
    }

    public void setSelectedLayers(ArrayList<GpkgLayer> selectedLayers) {

        this.selectedLayers = selectedLayers;
    }

    public ArrayList<GpkgLayer> getSelectedLayers() {
        return selectedLayers;
    }

    public boolean addLayer(GpkgLayer layer){
        return this.selectedLayers.add(layer);
    }

    public boolean removeLayer(GpkgLayer layer){
        if(!this.selectedLayers.isEmpty())
            return this.selectedLayers.remove(layer);
        return false;
    }
}
