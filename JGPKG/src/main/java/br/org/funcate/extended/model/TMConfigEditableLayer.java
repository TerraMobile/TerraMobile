package br.org.funcate.extended.model;

import android.support.v4.util.ArrayMap;

/**
 * Created by Andre Carvalho on 28/05/15.
 */
public class TMConfigEditableLayer {

    private ArrayMap<String,String> config;

    public TMConfigEditableLayer() {
        this.config =new ArrayMap<String,String>();
    }

    public void addConfig(String identify, String JSON) {
        this.config.put(identify, JSON);
    }

    public String getConfig(String identify) {
        return this.config.get(identify);
    }

    public boolean isEditable(String identify) {
        return this.config.containsKey(identify);
    }
}
