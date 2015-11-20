package br.org.funcate.terramobile.model.service;

import com.augtech.geoapi.geopackage.GeoPackage;

import java.util.Map;

import br.org.funcate.jgpkg.exception.QueryException;

/**
 * Created by Andre Carvalho on 19/11/15.
 */
public class MediaService {

    public static Map<String, Object> getMedias(GeoPackage gpkg, String mediaTable, long featureID) throws QueryException {
        Map<String, Object> medias = null;
        try {
            if(featureID>=0) {

                if(mediaTable!=null && !mediaTable.isEmpty()) {
                    medias = gpkg.getMedias(mediaTable, featureID);
                }
            }

        }catch (Exception e){
            throw new QueryException(e.getMessage());
        }
        return medias;
    }
}
