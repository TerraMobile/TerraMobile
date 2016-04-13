package br.org.funcate.terramobile.model.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgTable;
import com.augtech.geoapi.geopackage.table.FeaturesTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import br.org.funcate.dynamicforms.images.ImageUtilities;
import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.MediaDAO;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;

/**
 * Created by Andre Carvalho on 19/11/15.
 */
public class MediaService {

    /**
     *
     * @param context, The mainActivity
     * @param geoPackage, The GeoPackage reference
     * @param mediaTable, The media table name
     * @param featureID, The identify of the feature
     * @return A HashMap that contains the key and binary data to the pictures. The binary data are divide in thumbnail binary data and binary data in display size.
     * The structure contents: mediaIdentify=>{binaryData:{thumbnailBinary, displayBinary}}
     * @throws QueryException
     * @throws DAOException
     * @throws InvalidAppConfigException
     */
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
     * Upgrade pictures on database to one feature.
     * @param context the application context.
     * @param geoPackage, The Database representing the GeoPackage.
     * @param mediaTable, The name of the media table.
     * @param featureID, The identify of one feature.
     * @param imagesKeep, The media's list to be kept on database. If no medias to keep, use null.
     * @param insertImages, The media's list to be inserted. If no medias to insert, use null.
     * @throws Exception, DAOException, InvalidAppConfigException
     */
    public static void upgradePictures(Context context,
                                       GeoPackage geoPackage,
                                       String mediaTable,
                                       ArrayList<String> imagesKeep,
                                       ArrayList<String> insertImages,
                                       long featureID) throws Exception, DAOException, InvalidAppConfigException {
        long[] insertedMediaIDs;
        int removedRows = 0;

        MediaDAO dao = new MediaDAO(DatabaseFactory.getDatabase(context, geoPackage.getDatabaseFileName()));

        if (mediaTable != null && !mediaTable.isEmpty()) {

            removedRows = dao.removeMedias(mediaTable, imagesKeep, featureID);// TODO: write the number of removed medias on log

            if (insertImages!=null && !insertImages.isEmpty()) {

                Map<String, Integer> mediaDimensions = new HashMap<String, Integer>(4);

                /*
                DisplayMetrics displayMetrics = Util.getDisplayDimension((MainActivity) context);
                mediaDimensions.put("displayWidth", displayMetrics.widthPixels);
                mediaDimensions.put("displayHeight", displayMetrics.heightPixels);
                */
                // Using the dimension 1024x768 for reduce the size image to minus that 2MB
                mediaDimensions.put("displayWidth", 1024);
                mediaDimensions.put("displayHeight", 768);

                mediaDimensions.put("thumbnailWidth", context.getResources().getInteger(br.org.funcate.dynamicforms.R.integer.thumbnail_width));
                mediaDimensions.put("thumbnailHeight", context.getResources().getInteger(br.org.funcate.dynamicforms.R.integer.thumbnail_height));


                insertedMediaIDs = dao.insertMedias(mediaTable, featureID, insertImages, mediaDimensions);// TODO: write the number of inserted medias on log. Are used media identifiers??
            }
        }
    }

    public static boolean dropTable(Context context,
                                    GeoPackage geoPackage,
                                    String mediaTable) throws DAOException, InvalidAppConfigException {

        if(context==null || geoPackage==null || mediaTable==null || mediaTable.isEmpty()) {
            return false;
        }

        MediaDAO dao = new MediaDAO(DatabaseFactory.getDatabase(context, geoPackage.getDatabaseFileName()));
        return dao.dropTable(mediaTable);
    }

    public static boolean createMediaTable(Context context, GeoPackage geoPackage,
                                           String layerName, String mediaTable) throws Exception, DAOException, InvalidAppConfigException {

        if(context==null || geoPackage==null || layerName==null || layerName.isEmpty() || mediaTable==null || mediaTable.isEmpty()) {
            return false;
        }
        FeaturesTable userTable = (FeaturesTable) geoPackage.getUserTable(layerName, GpkgTable.TABLE_TYPE_FEATURES);
        String layerPK = userTable.getPrimaryKey(geoPackage);

        MediaDAO dao = new MediaDAO(DatabaseFactory.getDatabase(context, geoPackage.getDatabaseFileName()));
        return dao.createTable(layerName, mediaTable, layerPK);
    }

    public static byte[] getBinaryData(String imagePath) throws IOException {

        if(imagePath==null || imagePath.isEmpty()) return null;

        if (ImageUtilities.isImagePath(imagePath)) {
            BitmapFactory.Options options = ImageUtilities.getImageDimensions(imagePath);
            return MediaService.getBinaryData(options.outWidth, options.outHeight, imagePath);
        }
        return null;
    }

    public static byte[] getBinaryData(int width, int height, String imagePath) throws IOException {

        if(imagePath==null || imagePath.isEmpty()) return null;
        int quality = 100;// it is jpeg quality

        byte[] image=null;
        if (ImageUtilities.isImagePath(imagePath)) {
            Bitmap bitmap = ImageUtilities.getPic(width, height, imagePath);
            image = ImageUtilities.getBlobFromBitmap(bitmap, quality);
        }
        return image;

    }
}
