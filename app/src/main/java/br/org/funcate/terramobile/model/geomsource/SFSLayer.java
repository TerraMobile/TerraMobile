package br.org.funcate.terramobile.model.geomsource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;

import java.util.List;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSLayer extends KmlFolder {


    public SFSLayer(List<SimpleFeature> features)
    {
        super();
        if(features!=null)
        {
            for (SimpleFeature sfsFeature:features) {
                KmlFeature feature = SFSFeature.parseSFS(sfsFeature);
                add(feature);
            }

        }
    }
}
