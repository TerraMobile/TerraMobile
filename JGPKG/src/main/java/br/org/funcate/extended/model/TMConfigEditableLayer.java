package br.org.funcate.extended.model;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;

/**
 * Created by Andre Carvalho on 28/05/15.
 */
public class TMConfigEditableLayer {

    private ArrayMap<String,String> configLayer;

    public TMConfigEditableLayer() {
        this.configLayer=new ArrayMap<String,String>();
    }

    public void addConfigLayer(String identify, String JSON) {
        this.configLayer.put(identify,JSON);
    }

    public String getConfigLayer(String identify) {
        this.configLayer.get(identify);
        return null;
    }

}
