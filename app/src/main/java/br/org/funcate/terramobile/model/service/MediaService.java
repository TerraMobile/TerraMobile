package br.org.funcate.terramobile.model.service;

import android.content.Context;

import com.augtech.geoapi.geopackage.GeoPackage;

import java.util.ArrayList;
import java.util.Map;

import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.MediaDAO;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;

/**
 * Created by Andre Carvalho on 19/11/15.
 */
public class MediaService {

    public static Map<String, Object> getMedias(Context context,
                                                GeoPackage geoPackage,
                                                String mediaTable,
                                                long featureID) throws QueryException, DAOException, InvalidAppConfigException {
        Map<String, Object> medias = null;
        try {

            if(featureID>=0) {

                if(mediaTable!=null && !mediaTable.isEmpty()) {
                    MediaDAO dao = new MediaDAO(DatabaseFactory.getDatabase(context, geoPackage.getDatabaseFileName()));
                    medias = dao.getMedias(mediaTable, featureID);
                }
            }

        }catch (Exception e){
            throw new QueryException(e.getMessage());
        }
        return medias;
    }

    /**
     * Write pictures on database.
     * @param context the application context.
     * @param geoPackage, The Database representing the GeoPackage.
     * @param mediaTable, The name of the media table.
     * @param featureID, The identify of one feature.
     * @param imagesKeep, The media's list to be kept on database. If no medias to keep, use null.
     * @param insertImages, The media's list to be inserted. If no medias to insert, use null.
     * @throws QueryException
     */
    public static void updatePictures(Context context,
                                      GeoPackage geoPackage,
                                      String mediaTable,
                                      ArrayList<String> imagesKeep,
                                      ArrayList<Object> insertImages,
                                      long featureID) throws DAOException, InvalidAppConfigException, Exception {
        long[] insertedMediaIDs;
        int removedRows = 0;

        MediaDAO dao = new MediaDAO(DatabaseFactory.getDatabase(context, geoPackage.getDatabaseFileName()));

        if (mediaTable != null && !mediaTable.isEmpty()) {

            removedRows = dao.removeMedias(mediaTable, imagesKeep, featureID);// TODO: write the number of removed medias on log

            if (insertImages!=null && !insertImages.isEmpty()) {
                insertedMediaIDs = dao.insertMedias(mediaTable, featureID, insertImages);// TODO: write the number of inserted medias on log. Are used media identifiers??
            }
        }
        return;
    }

    /**
     * Write pictures on database.
     * @param context the application context.
     * @param geoPackage, The Database representing the GeoPackage.
     * @param mediaTable, The name of the media table.
     * @param featureID, The identify of one feature.
     * @param insertImages, The media's list to be inserted. If no medias to insert, use null.
     * @return true on success or false otherwise
     * @throws QueryException
     */
    public static boolean insertPictures(Context context,
                                     GeoPackage geoPackage,
                                     String mediaTable,
                                     ArrayList<Object> insertImages,
                                     long featureID) throws DAOException, InvalidAppConfigException, Exception {
        long[] insertedMediaIDs=null;

        MediaDAO dao = new MediaDAO(DatabaseFactory.getDatabase(context, geoPackage.getDatabaseFileName()));

        if (mediaTable != null && !mediaTable.isEmpty()) {

            if (insertImages!=null && !insertImages.isEmpty()) {
                // TODO: write the number of inserted medias on log. Are used media identifiers??
                insertedMediaIDs = dao.insertMedias(mediaTable, featureID, insertImages);
            }
        }
        if(insertedMediaIDs!=null && insertedMediaIDs.length>0)
            return true;
        else
            return false;
    }


    public static boolean existMediasOnTable(Context context,
                                             GeoPackage geoPackage,
                                             String mediaTable) throws DAOException, InvalidAppConfigException {

        MediaDAO dao = new MediaDAO(DatabaseFactory.getDatabase(context, geoPackage.getDatabaseFileName()));

        if (mediaTable != null && !mediaTable.isEmpty()) {

        }
        return true;
    }

    public static boolean dropTable(Context context,
                                    GeoPackage geoPackage,
                                    String mediaTable) throws DAOException, InvalidAppConfigException {
        if (mediaTable != null && !mediaTable.isEmpty()) {
            MediaDAO dao = new MediaDAO(DatabaseFactory.getDatabase(context, geoPackage.getDatabaseFileName()));
            return dao.dropTable(mediaTable);
        }
        return false;
    }
}
