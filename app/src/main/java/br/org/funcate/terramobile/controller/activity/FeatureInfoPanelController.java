package br.org.funcate.terramobile.controller.activity;

import android.content.Intent;
import android.os.Bundle;

import org.opengis.feature.simple.SimpleFeature;

import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.LowMemoryException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.model.service.FeatureService;

/**
 * Created by Andre Carvalho on 27/08/15.
 */
public class FeatureInfoPanelController {

    private MainActivity mainActivity;

    public FeatureInfoPanelController(MainActivity activity) {
        this.mainActivity = activity;
    }

    public void startFeatureInfoPanel(GpkgLayer layer, long featureID) {

        SimpleFeature feature=null;
        try {
            feature = AppGeoPackageService.getFeature(layer, featureID);
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
        } catch (LowMemoryException e) {
            e.printStackTrace();
        } catch (TerraMobileException e) {
            e.printStackTrace();
        }

        String jsonForm = layer.getJSON();
        Bundle featureDataValues = FeatureService.featureAttrsToBundle(feature, jsonForm);
        Intent intent = new Intent(mainActivity.getApplicationContext(), FeatureInfoPanelActivity.class);
        intent.putExtra(FeatureService.FEATURE_DATA_CONTENT, featureDataValues);
        mainActivity.startActivity(intent);
    }

}
