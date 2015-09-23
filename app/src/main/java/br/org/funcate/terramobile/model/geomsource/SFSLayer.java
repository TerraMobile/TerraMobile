package br.org.funcate.terramobile.model.geomsource;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;

import java.util.List;

import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSLayer extends KmlFolder {


    public SFSLayer(List<SimpleFeature> features, GpkgLayer layer) {
        super();
        if (features != null) {
            for (SimpleFeature sfsFeature : features) {
                KmlFeature feature = SFSFeature.parseSFS(sfsFeature, layer);
                add(feature);
            }

        }
    }
}
